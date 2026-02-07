package org.lemon.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lemon.chat.BillAssistantService;
import org.lemon.entity.*;
import org.lemon.entity.dto.ChatFinanceTransactionsDTO;
import org.lemon.entity.dto.RetryTaskTypeResultDTO;
import org.lemon.entity.dto.UserPromptInfoDTO;
import org.lemon.entity.exception.BusinessException;
import org.lemon.entity.req.ConsumerTrendsReq;
import org.lemon.entity.req.FinanceTransactionsQueryReq;
import org.lemon.entity.req.FinanceTransactionsReq;
import org.lemon.entity.req.TimeFrameReq;
import org.lemon.entity.resp.*;
import org.lemon.entity.table.CategoryTableDef;
import org.lemon.enumeration.AmountTypeEnum;
import org.lemon.enumeration.RetryTaskTypeEnum;
import org.lemon.mapper.*;
import org.lemon.service.definition.RetryTaskDefinition;
import org.lemon.utils.UserUtil;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.lemon.enumeration.RetryTaskTypeEnum.TELEGRAM_BILL_PARSE;
import static org.lemon.service.definition.SystemConstants.CONSUMPTION_RECORDS_ASSISTANT;

/**
 * 账单信息表 服务层实现。
 *
 * @author Lemon
 * @since 2025-05-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceTransactionsService extends ServiceImpl<FinanceTransactionsMapper, FinanceTransactions> implements RetryTaskDefinition {

    @Resource(name = "commonExecutor")
    private ThreadPoolTaskExecutor executor;
    private final UserMapper userMapper;
    private final AccountsMapper accountsMapper;
    private final CategoryMapper categoryMapper;
    private final MonthTotalRecordMapper monthTotalRecordMapper;

    private final BillAssistantService billAssistantService;
    private final AiPromptTemplateService aiPromptTemplateService;
    private final RetryTaskService retryTaskService;

    private final TransactionTemplate transactionTemplate;

    @Override
    public RetryTaskTypeEnum getType() {
        return TELEGRAM_BILL_PARSE;
    }

    @Override
    public RetryTaskTypeResultDTO execute(Integer userId, String taskData) {
        RetryTaskTypeResultDTO result = new RetryTaskTypeResultDTO();
        User user = userMapper.selectOneById(userId);
        if (user == null) {
            result.setData("用户不存在！");
            return result;
        }
        Boolean execute;
        try {
            Map<Integer, String> promptMap = aiPromptTemplateService.getPromptDetail(CONSUMPTION_RECORDS_ASSISTANT, getUserPromptInfo(List.of(user)));
            if (CollUtil.isEmpty(promptMap)) {
                result.setData("用户信息不全！");
            }
            Map<Integer, ChatFinanceTransactionsDTO> userBillMap = HashMap.newHashMap(promptMap.size());
            promptMap.forEach((key, value) -> {
                try {
                    userBillMap.put(key, billAssistantService.billMessageChat(value, taskData));
                } catch (Exception e) {
                    log.error("Telegram ai bill message error:{}", e.getMessage(), e);
                }
            });
            String data = JSONObject.toJSONString(userBillMap.get(userId));
            execute = transactionTemplate.execute(status -> save(userBillMap));
            if (Boolean.TRUE.equals(execute)) {
                result.setSuccess(true);
                result.setData(data);
            } else {
                result.setData("保存失败，数据：" + data);
            }
        } catch (Exception e) {
            result.setData(e.getMessage());
        }
        return result;
    }


    public List<CashFlowCardVO> getCashFlowCard(TimeFrameReq data) {
        Integer userId = UserUtil.getCurrentUserId();
        Map<Integer, Double> amountMap = queryChain().select(FinanceTransactions::getAmount, FinanceTransactions::getType)
                .eq(FinanceTransactions::getUserId, userId)
                .between(FinanceTransactions::getTransactionDate, data.getStartTime(), data.getEndTime())
                .list().stream()
                .collect(Collectors.groupingBy(FinanceTransactions::getType, Collectors.summingDouble(o -> o.getAmount().doubleValue())));
        List<CashFlowCardVO> result = new ArrayList<>(AmountTypeEnum.values().length);
        Double income = amountMap.getOrDefault(AmountTypeEnum.INCOME.getCode(), 0D);
        Double expense = amountMap.getOrDefault(AmountTypeEnum.EXPENSE.getCode(), 0D);
        CashFlowCardVO vo1 = new CashFlowCardVO();
        vo1.setType(AmountTypeEnum.INCOME.getCode());
        vo1.setAmount(income);
        result.add(vo1);
        CashFlowCardVO vo2 = new CashFlowCardVO();
        vo2.setType(AmountTypeEnum.EXPENSE.getCode());
        vo2.setAmount(expense);
        result.add(vo2);
        CashFlowCardVO vo3 = new CashFlowCardVO();
        vo3.setType(AmountTypeEnum.BALANCE.getCode());
        vo3.setAmount(income - expense);
        result.add(vo3);
        return result;
    }

    public Page<FinanceTransactionsVO> getFinanceTransactionsList(FinanceTransactionsQueryReq data) {
        Integer userId = UserUtil.getCurrentUserId();
        // 查询所有账户列表
        CompletableFuture<Map<Integer, Accounts>> future1 = CompletableFuture
                .supplyAsync(() -> QueryChain.of(accountsMapper).eq(Accounts::getUserId, userId).list()
                        .stream().collect(Collectors.toMap(Accounts::getId, Function.identity())), executor);
        // 查询所有账单分类
        CompletableFuture<Map<Integer, Category>> future2 = CompletableFuture
                .supplyAsync(() -> QueryChain.of(categoryMapper).eq(Category::getUserId, userId).list()
                        .stream().collect(Collectors.toMap(Category::getId, Function.identity())), executor);
        // 查询收支信息
        Page<FinanceTransactions> page = queryChain().eq(FinanceTransactions::getUserId, userId)
                .eq(FinanceTransactions::getAccountId, data.getAccountId(), data.getAccountId() != null)
                .eq(FinanceTransactions::getCategoryId, data.getCategoryId(), data.getCategoryId() != null)
                .eq(FinanceTransactions::getType, data.getType(), data.getType() != null)
                .ge(FinanceTransactions::getTransactionDate, data.getStartTime(), data.getStartTime() != null)
                .lt(FinanceTransactions::getTransactionDate, data.getEndTime(), data.getEndTime() != null)
                .orderBy(MonthTotalRecord::getId, false)
                .page(new Page<>(data.getPageNum(), data.getPageSize()));
        Page<FinanceTransactionsVO> result = new Page<>();
        result.setTotalRow(page.getTotalRow());
        if (Objects.equals(page.getTotalRow(), 0)) {
            return result;
        }
        Map<Integer, Accounts> accountsMap = future1.join();
        Map<Integer, Category> categoryMap = future2.join();
        List<FinanceTransactionsVO> list = new ArrayList<>();
        for (FinanceTransactions temp : page.getRecords()) {
            FinanceTransactionsVO vo = FinanceTransactionsVO.builder()
                    .account(temp.getAmount().toString()).id(temp.getId())
                    .note(temp.getNote()).type(temp.getType())
                    .transactionDate(temp.getTransactionDate())
                    .amount(temp.getAmount().doubleValue())
                    .build();
            vo.setCategory(Optional.ofNullable(categoryMap.get(temp.getCategoryId())).map(Category::getCategory).orElse(""));
            vo.setAccount(Optional.ofNullable(accountsMap.get(temp.getAccountId())).map(Accounts::getAccountName).orElse(""));
            list.add(vo);
        }
        result.setRecords(list);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdateFinanceTransactions(FinanceTransactionsReq data) {
        Integer userId = UserUtil.getCurrentUserId();
        FinanceTransactions result = FinanceTransactions.builder()
                .id(data.getId()).userId(userId)
                .amount(BigDecimal.valueOf(data.getAmount()))
                .type(data.getType())
                .categoryId(data.getCategoryId())
                .accountId(data.getAccountId())
                .transactionDate(data.getTransactionDate())
                .note(data.getNote())
                .build();
        if (data.getId() != null && data.getId() > 0) {
            result.setUpdateNo(userId);
        } else {
            result.setCreateNo(userId);
        }
        Accounts accounts = Optional.ofNullable(accountsMapper.selectOneById(data.getAccountId())).orElseThrow(() -> new BusinessException("账户不存在！"));
        if (data.getType() == AmountTypeEnum.INCOME.getCode()) {
            accounts.setAmount(accounts.getAmount().add(result.getAmount()));
        } else {
            accounts.setAmount(accounts.getAmount().subtract(result.getAmount()));
        }
        accountsMapper.update(accounts);
        // 重复月份统计
        String month = data.getTransactionDate().format(DatePattern.NORM_MONTH_FORMATTER);
        UpdateChain.of(monthTotalRecordMapper).set(MonthTotalRecord::getRepeat, 1).eq(MonthTotalRecord::getUserId, userId)
                .eq(MonthTotalRecord::getMonth, month).update();
        return saveOrUpdate(result);
    }

    public Boolean delFinanceTransactions(Integer id) {
        Integer userId = UserUtil.getCurrentUserId();
        FinanceTransactions data = Optional.ofNullable(getById(id)).orElseThrow(() -> new BusinessException("数据不存在！"));
        // 重复月份统计
        String month = data.getTransactionDate().format(DatePattern.NORM_MONTH_FORMATTER);
        UpdateChain.of(monthTotalRecordMapper).set(MonthTotalRecord::getRepeat, 1).eq(MonthTotalRecord::getUserId, userId)
                .eq(MonthTotalRecord::getMonth, month).update();
        return removeById(id);
    }

    public List<ConsumptionStatisticsVO> getConsumptionStatistics(ConsumerTrendsReq data) {
        Map<Integer, BigDecimal> collect = queryChain().between(FinanceTransactions::getTransactionDate, data.getStartTime(), data.getEndTime())
                .eq(FinanceTransactions::getType, AmountTypeEnum.EXPENSE.getCode())
                .list().stream().collect(Collectors.groupingBy(FinanceTransactions::getCategoryId,
                        Collectors.mapping(FinanceTransactions::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
        List<ConsumptionStatisticsVO> result = new ArrayList<>();
        if (CollUtil.isEmpty(collect)) {
            return result;
        }
        Integer userId = UserUtil.getCurrentUserId();
        // 查询分类
        Map<Integer, String> categoryMap = QueryChain.of(categoryMapper).eq(Category::getUserId, userId)
                .in(Category::getId, collect.keySet())
                .list().stream().collect(Collectors.toMap(Category::getId, Category::getCategory));
        collect.forEach((key, value) -> {
            ConsumptionStatisticsVO vo = new ConsumptionStatisticsVO();
            vo.setCategory(categoryMap.getOrDefault(key, ""));
            vo.setConsumption(value.doubleValue());
            result.add(vo);
        });
        return result;
    }

    public Map<Integer, ChatFinanceTransactionsDTO> analysisMessage(String chatId, String text) {
        if (StrUtil.isBlank(text)) {
            log.error("消息不能为空");
        }
        List<User> list = QueryChain.of(userMapper).eq(User::getChannelId, chatId).list();
        if (CollUtil.isEmpty(list)) {
            log.error("频道id不存在：{}", chatId);
            return Collections.emptyMap();
        }
        Map<Integer, String> promptMap = aiPromptTemplateService.getPromptDetail(CONSUMPTION_RECORDS_ASSISTANT, getUserPromptInfo(list));
        Map<Integer, ChatFinanceTransactionsDTO> userBillMap = HashMap.newHashMap(promptMap.size());
        promptMap.forEach((key, value) -> {
            try {
                userBillMap.put(key, billAssistantService.billMessageChat(value, text));
            } catch (Exception e) {
                log.error("Telegram ai bill message error:{}", e.getMessage(), e);
                retryTaskService.createRetryTask(TELEGRAM_BILL_PARSE, text, key);
            }
        });
        return userBillMap;
    }

    private List<UserPromptInfoDTO> getUserPromptInfo(List<User> list) {
        List<UserPromptInfoDTO> result = new ArrayList<>();
        for (User user : list) {
            UserPromptInfoDTO dto = new UserPromptInfoDTO();
            dto.setUserId(user.getId());
            dto.setCategories(QueryChain.of(categoryMapper)
                    .select(CategoryTableDef.CATEGORY.ID.as("key"), CategoryTableDef.CATEGORY.CATEGORY_.as("value"))
                    .eq(Category::getUserId, user.getId()).listAs(SimpleEnumVO.class));
            // 近三月的月度总收支记录
            dto.setMonthTotalRecords(QueryChain.of(monthTotalRecordMapper).eq(MonthTotalRecord::getUserId, user.getId())
                    .orderBy(MonthTotalRecord::getMonth, false).limit(3)
                    .listAs(MonthTotalRecordVO.class));
        }
        return result;
    }

    public boolean save(Map<Integer, ChatFinanceTransactionsDTO> userMessage) {
        // ZFH TODO : 2026/2/7
        return true;
    }
}
