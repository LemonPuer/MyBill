package org.lemon.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.lemon.entity.Accounts;
import org.lemon.entity.Category;
import org.lemon.entity.FinanceTransactions;
import org.lemon.entity.MonthTotalRecord;
import org.lemon.entity.exception.BusinessException;
import org.lemon.entity.req.FinanceTransactionsQueryReq;
import org.lemon.entity.req.FinanceTransactionsReq;
import org.lemon.entity.req.TimeFrameReq;
import org.lemon.entity.resp.CashFlowCardVO;
import org.lemon.entity.resp.FinanceTransactionsVO;
import org.lemon.enumeration.AmountTypeEnum;
import org.lemon.enumeration.IBaseEnum;
import org.lemon.mapper.AccountsMapper;
import org.lemon.mapper.CategoryMapper;
import org.lemon.mapper.FinanceTransactionsMapper;
import org.lemon.mapper.MonthTotalRecordMapper;
import org.lemon.utils.UserUtil;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
@Service
@AllArgsConstructor
public class FinanceTransactionsService extends ServiceImpl<FinanceTransactionsMapper, FinanceTransactions> {

    @Resource(name = "commonExecutor")
    private final ThreadPoolTaskExecutor executor;
    private final AccountsMapper accountsMapper;
    private final CategoryMapper categoryMapper;
    private final MonthTotalRecordMapper monthTotalRecordMapper;


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
                .supplyAsync(() -> QueryChain.of(categoryMapper).eq(FinanceTransactions::getUserId, userId).list()
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
}
