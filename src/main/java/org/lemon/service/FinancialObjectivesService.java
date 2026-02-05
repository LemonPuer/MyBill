package org.lemon.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.Accounts;
import org.lemon.entity.FinancialObjectives;
import org.lemon.entity.common.BasePage;
import org.lemon.entity.resp.FinancialObjectivesVO;
import org.lemon.mapper.AccountsMapper;
import org.lemon.mapper.FinancialObjectivesMapper;
import org.lemon.utils.UserUtil;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 财务目标表 服务层实现。
 *
 * @author Lemon
 * @since 2025-05-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialObjectivesService extends ServiceImpl<FinancialObjectivesMapper, FinancialObjectives> {

    @Resource(name = "commonExecutor")
    private ThreadPoolTaskExecutor executor;
    private final AccountsMapper accountsMapper;

    public Page<FinancialObjectivesVO> getFinancialObjectives(BasePage basePage) {
        Integer userId = UserUtil.getCurrentUserId();
        // 查询账户计算余额
        CompletableFuture<Double> future = CompletableFuture.supplyAsync(() -> QueryChain.of(accountsMapper)
                .eq(Accounts::getUserId, userId).list()
                .stream().mapToDouble(o -> o.getAmount().doubleValue()).sum(), executor);
        Page<FinancialObjectives> page = queryChain().eq(FinancialObjectives::getUserId, userId)
                .orderBy(FinancialObjectives::getId, false)
                .page(new Page<>(basePage.getPageNum(), basePage.getPageSize()));
        Double balance = future.join();
        Page<FinancialObjectivesVO> result = new Page<>();
        result.setTotalRow(page.getTotalRow());
        List<FinancialObjectivesVO> list = page.getRecords().stream().map(o -> FinancialObjectivesVO.builder()
                .id(o.getId()).icon(o.getIcon()).finished(o.getFinished()).objective(o.getObjective())
                .amount(o.getAmount().doubleValue()).balance(balance).build()).collect(Collectors.toList());
        result.setRecords(list);
        return result;
    }
}
