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
import org.lemon.entity.Accounts;
import org.lemon.entity.Category;
import org.lemon.entity.FinanceTransactions;
import org.lemon.entity.MonthTotalRecord;
import org.lemon.entity.common.TelegramBillMessageParam;
import org.lemon.entity.dto.ChatFinanceTransactionsDTO;
import org.lemon.entity.exception.BusinessException;
import org.lemon.entity.req.ConsumerTrendsReq;
import org.lemon.entity.req.FinanceTransactionsQueryReq;
import org.lemon.entity.req.FinanceTransactionsReq;
import org.lemon.entity.req.TimeFrameReq;
import org.lemon.entity.resp.CashFlowCardVO;
import org.lemon.entity.resp.ConsumptionStatisticsVO;
import org.lemon.entity.resp.FinanceTransactionsVO;
import org.lemon.enumeration.AmountTypeEnum;
import org.lemon.mapper.AccountsMapper;
import org.lemon.mapper.CategoryMapper;
import org.lemon.mapper.FinanceTransactionsMapper;
import org.lemon.mapper.MonthTotalRecordMapper;
import org.lemon.utils.UserUtil;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 账单信息表 服务层实现。
 *
 * @author Lemon
 * @since 2025-05-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceTransactionsService extends ServiceImpl<FinanceTransactionsMapper, FinanceTransactions> {

    @Resource(name = "commonExecutor")
    private ThreadPoolTaskExecutor executor;
    private final AccountsMapper accountsMapper;
    private final CategoryMapper categoryMapper;
    private final MonthTotalRecordMapper monthTotalRecordMapper;

    private final BillAssistantService billAssistantService;


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

    public void saveBillAssistant(TelegramBillMessageParam param) {
        if (StrUtil.isBlank(param.getText())) {
            log.error("Telegram ai bill message is empty!");
        }
        ChatFinanceTransactionsDTO dto = billAssistantService.billMessageChat("""
                你是消费记录解析助手。将用户的自然语言消费描述解析为结构化数据。
                
                金额类型：{"AmountType":[{"key":1,"value":"收入"},{"key":2,"value":"支出"},{"key":3,"value":"结余"}]}
                支出分类：[{"id":4,"category":"交通","icon":"Failed"},{"id":3,"category":"娱乐","icon":"SwitchFilled"},{"id":2,"category":"餐饮","icon":"ForkSpoon"},{"id":1,"category":"购物","icon":"Goods"}]
                
                解析规则：
                1. amountType：判断消息表达的金钱流向
                   - 支出含义（用户花钱）→ "2"
                   - 收入含义（用户获得钱）→ "1"
                   - 结余含义（账户余额状态）→ "3"
                
                2. amount：提取数字金额，无则返回null
                
                3. categoryId：仅amountType="2"时填充
                   - 根据消息内容匹配分类
                   - 无法确定或多分类时返回null
                   - amountType="1"或"3"时返回null
                
                4. note：保留原始用户输入
                
                返回JSON：{"amountType":"","amount":,"categoryId":,"note":""}
                
                示例：
                - "吃饭花了25元" → {"amountType":"2","amount":25,"categoryId":2,"note":"吃饭花了25元"}
                - "买水果花了10元" → {"amountType":"2","amount":10,"categoryId":1,"note":"买水果花了10元"}
                - "赚了5000元" → {"amountType":"1","amount":5000,"categoryId":null,"note":"赚了5000元"}
                """, param.getText());
        log.info("Telegram ai bill message result: {}", JSONObject.toJSONString(dto));
    }
}
