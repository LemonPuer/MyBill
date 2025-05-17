package org.lemon.service;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.Budget;
import org.lemon.entity.Category;
import org.lemon.entity.FinanceTransactions;
import org.lemon.entity.req.TimeFrameReq;
import org.lemon.entity.resp.BudgetInfoVO;
import org.lemon.enumeration.AmountTypeEnum;
import org.lemon.mapper.BudgetMapper;
import org.lemon.utils.UserUtil;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 预算管理表 服务层实现。
 *
 * @author Lemon
 * @since 2025-05-17
 */
@Slf4j
@Service
@AllArgsConstructor
public class BudgetService extends ServiceImpl<BudgetMapper, Budget> {

    private final CategoryService categoryService;
    private final FinanceTransactionsService financeTransactionsService;
    @Resource(name = "commonExecutor")
    private final ThreadPoolTaskExecutor executor;

    public List<BudgetInfoVO> getBudgetInfo(TimeFrameReq data) {
        Integer userId = UserUtil.getCurrentUserId();
        List<Budget> list = queryChain().eq(Budget::getUserId, userId)
                .between(Budget::getStartTime, data.getStartTime(), data.getEndTime())
                .list();
        List<Integer> categoryIds = list.stream().map(Budget::getCategoryId).collect(Collectors.toList());
        CompletableFuture<Map<Integer, Category>> future1 = CompletableFuture
                .supplyAsync(() -> categoryService.queryChain().eq(Category::getUserId, userId)
                        .in(Category::getId, categoryIds).list()
                        .stream().collect(Collectors.toMap(Category::getId, Function.identity())), executor);
        // 查询花费
        CompletableFuture<Map<Integer, Double>> future2 = CompletableFuture.supplyAsync(() -> financeTransactionsService.queryChain()
                .eq(FinanceTransactions::getUserId, userId)
                .eq(FinanceTransactions::getType, AmountTypeEnum.EXPENSE.getCode())
                .between(FinanceTransactions::getTransactionDate, data.getStartTime(), data.getEndTime())
                .in(FinanceTransactions::getCategoryId, categoryIds).list()
                .stream()
                .collect(Collectors.groupingBy(FinanceTransactions::getCategoryId,
                        Collectors.summingDouble(o -> o.getAmount().doubleValue()))), executor);
        Map<Integer, Category> categoryMap = future1.join();
        Map<Integer, Double> expenseMap = future2.join();
        return list.stream().map(o -> {
            Category category = categoryMap.get(o.getId());
            return BudgetInfoVO.builder().icon(category.getIcon()).amount(o.getAmount().toString())
                    .category(category.getCategory()).cost(expenseMap.getOrDefault(o.getId(), 0.0).toString())
                    .build();
        }).collect(Collectors.toList());
    }
}
