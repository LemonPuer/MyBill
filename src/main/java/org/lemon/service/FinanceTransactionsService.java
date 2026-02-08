package org.lemon.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lemon.chat.BillAssistantService;
import org.lemon.entity.*;
import org.lemon.entity.common.TelegramBillMessageParam;
import org.lemon.entity.dto.ChatFinanceTransactionsDTO;
import org.lemon.entity.dto.RetryTaskTypeResultDTO;
import org.lemon.entity.dto.UserPromptInfoDTO;
import org.lemon.entity.exception.BusinessException;
import org.lemon.entity.req.ConsumerTrendsReq;
import org.lemon.entity.req.FinanceTransactionsQueryReq;
import org.lemon.entity.req.FinanceTransactionsReq;
import org.lemon.entity.req.TimeFrameReq;
import org.lemon.entity.resp.*;
import org.lemon.enumeration.AmountTypeEnum;
import org.lemon.enumeration.RetryTaskTypeEnum;
import org.lemon.mapper.CategoryMapper;
import org.lemon.mapper.FinanceTransactionsMapper;
import org.lemon.mapper.MonthTotalRecordMapper;
import org.lemon.mapper.UserMapper;
import org.lemon.service.definition.RetryTaskDefinition;
import org.lemon.utils.UserUtil;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final CategoryMapper categoryMapper;
    private final MonthTotalRecordMapper monthTotalRecordMapper;

    private final BillAssistantService billAssistantService;
    private final AiPromptTemplateService aiPromptTemplateService;
    private final RetryTaskService retryTaskService;

    @Override
    public RetryTaskTypeEnum getType() {
        return TELEGRAM_BILL_PARSE;
    }

    @Override
    public Map<Long, RetryTaskTypeResultDTO> execute(List<RetryTask> taskList) {
        if (CollUtil.isEmpty(taskList)) {
            return Collections.emptyMap();
        }
        Map<Integer, List<RetryTask>> userTaskMap = taskList.stream().collect(Collectors.groupingBy(RetryTask::getUserId));
        List<User> users = userMapper.selectListByIds(userTaskMap.keySet());
        if (CollUtil.isEmpty(users)) {
            return Collections.emptyMap();
        }
        Boolean execute;
        Map<Integer, String> promptMap;
        Map<Integer, UserPromptInfoDTO> userPromptInfo = getUserPromptInfo(users);
        try {
            promptMap = aiPromptTemplateService.getPromptDetail(CONSUMPTION_RECORDS_ASSISTANT, userPromptInfo.values());
        } catch (Exception e) {
            log.error("Telegram ai bill message error:{}", e.getMessage(), e);
            return Collections.emptyMap();
        }
        if (CollUtil.isEmpty(promptMap)) {
            return Collections.emptyMap();
        }
        Map<Long, RetryTaskTypeResultDTO> result = new HashMap<>(taskList.size());
        List<FinanceTransactions> list = new ArrayList<>();
        userTaskMap.forEach((userId, tasks) -> {
            String prompt = promptMap.get(userId);
            tasks.forEach(task -> {
                try {
                    ChatFinanceTransactionsDTO dto = billAssistantService.billMessageChat(prompt, task.getTaskData());
                    FinanceTransactions financeTransactions = checkChatData(dto, userPromptInfo.get(userId), task.getCreatedTime());
                    if (financeTransactions == null) {
                        throw new BusinessException("账单信息校验失败！");
                    }
                    list.add(financeTransactions);
                    result.computeIfAbsent(task.getId(), o -> new RetryTaskTypeResultDTO(true, ""));
                } catch (Exception e) {
                    log.error("Telegram ai bill message error:{}", e.getMessage(), e);
                    result.computeIfAbsent(task.getId(),
                            o -> new RetryTaskTypeResultDTO(false, "解析失败，" + e.getClass().getSimpleName() + ":" + e.getMessage()));
                }
            });
        });
        if (CollUtil.isNotEmpty(list)) {
            saveBatch(list);
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
        // 查询所有账单分类
        CompletableFuture<Map<Integer, Category>> future1 = CompletableFuture
                .supplyAsync(() -> QueryChain.of(categoryMapper).eq(Category::getUserId, userId).list()
                        .stream().collect(Collectors.toMap(Category::getId, Function.identity())), executor);
        // 查询收支信息
        Page<FinanceTransactions> page = queryChain().eq(FinanceTransactions::getUserId, userId)
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
        Map<Integer, Category> categoryMap = future1.join();
        List<FinanceTransactionsVO> list = new ArrayList<>();
        for (FinanceTransactions temp : page.getRecords()) {
            FinanceTransactionsVO vo = FinanceTransactionsVO.builder()
                    .note(temp.getNote()).type(temp.getType())
                    .transactionDate(temp.getTransactionDate())
                    .amount(temp.getAmount().doubleValue())
                    .build();
            vo.setCategory(Optional.ofNullable(categoryMap.get(temp.getCategoryId())).map(Category::getCategory).orElse(""));
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
                .transactionDate(data.getTransactionDate())
                .note(data.getNote())
                .build();
        if (data.getId() != null && data.getId() > 0) {
            result.setUpdateNo(userId);
        } else {
            result.setCreateNo(userId);
        }
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

    public void saveMessageBill(TelegramBillMessageParam param) {
        String text = param.getText();
        String chatId = param.getChatId();
        if (StrUtil.isBlank(text)) {
            log.error("消息不能为空");
            return;
        }
        List<User> list = QueryChain.of(userMapper).eq(User::getChannelId, chatId).list();
        if (CollUtil.isEmpty(list)) {
            log.error("频道id不存在：{}", chatId);
            return;
        }
        Map<Integer, UserPromptInfoDTO> userPromptInfo = getUserPromptInfo(list);
        Map<Integer, String> promptMap = aiPromptTemplateService.getPromptDetail(CONSUMPTION_RECORDS_ASSISTANT, userPromptInfo.values());
        List<FinanceTransactions> result = new ArrayList<>();
        promptMap.forEach((key, value) -> {
            FinanceTransactions po = null;
            try {
                ChatFinanceTransactionsDTO dto = billAssistantService.billMessageChat(value, text);
                po = checkChatData(dto, userPromptInfo.get(key), param.getDate());
            } catch (Exception e) {
                log.error("Telegram ai bill message error:{}", e.getMessage(), e);
                retryTaskService.createRetryTask(TELEGRAM_BILL_PARSE, text, key);
            }
            if (Objects.isNull(po)) {
                // 校验不通过
                retryTaskService.createRetryTask(TELEGRAM_BILL_PARSE, text, key);
            }
            result.add(po);
        });
        if (CollUtil.isNotEmpty(result)) {
            saveBatch(result);
        }
    }

    private Map<Integer, UserPromptInfoDTO> getUserPromptInfo(List<User> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyMap();
        }
        Map<Integer, UserPromptInfoDTO> result = new HashMap<>(list.size());
        Set<Integer> userSet = list.stream().map(User::getId).collect(Collectors.toSet());
        Map<Integer, List<SimpleEnumVO>> categoryMap = QueryChain.of(categoryMapper)
                .in(Category::getUserId, userSet).list()
                .stream().collect(Collectors.groupingBy(Category::getUserId,
                        Collectors.mapping(category -> new SimpleEnumVO(category.getId(), category.getCategory()), Collectors.toList())
                ));
        Map<Integer, List<MonthTotalRecordVO>> mothRecordMap = QueryChain.of(monthTotalRecordMapper)
                .in(MonthTotalRecord::getUserId, userSet).listAs(MonthTotalRecordVO.class)
                .stream().collect(Collectors.groupingBy(MonthTotalRecordVO::getUserId, Collectors.collectingAndThen(
                        Collectors.toList(), o -> o.stream().sorted(Comparator.comparing(MonthTotalRecordVO::getMonth).reversed())
                                .limit(3).collect(Collectors.toList())
                )));
        for (User user : list) {
            UserPromptInfoDTO dto = new UserPromptInfoDTO();
            dto.setUserId(user.getId());
            dto.setCategories(categoryMap.get(user.getId()));
            // 近三月的月度总收支记录
            dto.setMonthTotalRecords(mothRecordMap.get(user.getId()));
            result.put(user.getId(), dto);
        }
        return result;
    }

    /**
     * 验证收支类型是否有效
     *
     * @param amountType 收支类型
     * @return 是否有效
     */
    private boolean isValidAmountType(Integer amountType) {
        return amountType != null &&
                (amountType.equals(AmountTypeEnum.INCOME.getCode()) ||
                        amountType.equals(AmountTypeEnum.EXPENSE.getCode()));
    }

    private FinanceTransactions checkChatData(ChatFinanceTransactionsDTO billMessage, UserPromptInfoDTO userInfo, LocalDateTime dateTime) {
        if (Objects.isNull(billMessage)) {
            log.warn("AI解析结果为空");
            return null;
        }
        // 基础字段校验
        if (Objects.isNull(billMessage.getAmountType())) {
            log.warn("收支类型不能为空");
            return null;
        }
        if (Objects.isNull(billMessage.getAmount()) || billMessage.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("金额必须大于0");
            return null;
        }
        if (Objects.isNull(billMessage.getCategoryId())) {
            log.warn("分类ID不能为空");
            return null;
        }
        // 验证收支类型有效性
        if (!isValidAmountType(billMessage.getAmountType())) {
            log.warn("无效的收支类型: {}", billMessage.getAmountType());
            return null;
        }

        List<SimpleEnumVO> categories = userInfo.getCategories();
        SimpleEnumVO simpleEnumVO = categories.stream().filter(category -> Objects.equals(category.getKey(), billMessage.getCategoryId())).findFirst().orElse(null);
        // 验证分类是否存在且属于当前用户
        if (Objects.isNull(simpleEnumVO)) {
            log.warn("分类不存在或无权限访问，分类ID: {}，用户ID: {}", billMessage.getCategoryId(), userInfo.getUserId());
            return null;
        }
        // 构建财务交易实体
        return FinanceTransactions.builder()
                .userId(userInfo.getUserId())
                .amount(billMessage.getAmount())
                .type(billMessage.getAmountType())
                .categoryId(billMessage.getCategoryId())
                .transactionDate(dateTime)
                .note(billMessage.getNote())
                .createTime(LocalDateTime.now())
                .createNo(userInfo.getUserId())
                .build();
    }
}
