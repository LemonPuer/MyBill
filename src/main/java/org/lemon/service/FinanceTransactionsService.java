package org.lemon.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.lemon.entity.Accounts;
import org.lemon.entity.Category;
import org.lemon.entity.FinanceTransactions;
import org.lemon.entity.MonthTotalRecord;
import org.lemon.entity.req.FinanceTransactionsQueryReq;
import org.lemon.entity.req.TimeFrameReq;
import org.lemon.entity.resp.CashFlowCardVO;
import org.lemon.entity.resp.FinanceTransactionsVO;
import org.lemon.enumeration.AmountTypeEnum;
import org.lemon.enumeration.IBaseEnum;
import org.lemon.mapper.FinanceTransactionsMapper;
import org.lemon.utils.UserUtil;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
@Service
@AllArgsConstructor
public class FinanceTransactionsService extends ServiceImpl<FinanceTransactionsMapper, FinanceTransactions> {

    private final AccountsService accountsService;
    private final CategoryService categoryService;
    @Resource(name = "commonExecutor")
    private final ThreadPoolTaskExecutor executor;

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
                .supplyAsync(() -> accountsService.queryChain().eq(Accounts::getUserId, userId).list()
                        .stream().collect(Collectors.toMap(Accounts::getId, Function.identity())), executor);
        // 查询所有账单分类
        CompletableFuture<Map<Integer, Category>> future2 = CompletableFuture
                .supplyAsync(() -> categoryService.queryChain().eq(FinanceTransactions::getUserId, userId).list()
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
            FinanceTransactionsVO vo = FinanceTransactionsVO.builder().account(temp.getAmount().toString())
                    .note(temp.getNote()).typeStr(IBaseEnum.getValueByKey(AmountTypeEnum.class, temp.getType()))
                    .transactionDate(DateUtil.format(temp.getTransactionDate(), DatePattern.NORM_DATE_PATTERN))
                    .build();
            vo.setCategory(Optional.ofNullable(categoryMap.get(temp.getCategoryId())).map(Category::getCategory).orElse(""));
            vo.setAccount(Optional.ofNullable(accountsMap.get(temp.getAccountId())).map(Accounts::getAccountName).orElse(""));
            list.add(vo);
        }
        result.setRecords(list);
        return null;
    }
}
