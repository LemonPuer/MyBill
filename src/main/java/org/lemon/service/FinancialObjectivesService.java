package org.lemon.service;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.Accounts;
import org.lemon.entity.FinancialObjectives;
import org.lemon.entity.resp.FinancialObjectivesVO;
import org.lemon.mapper.FinancialObjectivesMapper;
import org.lemon.utils.UserUtil;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
@AllArgsConstructor
public class FinancialObjectivesService extends ServiceImpl<FinancialObjectivesMapper, FinancialObjectives> {

    @Resource(name = "commonExecutor")
    private final ThreadPoolTaskExecutor executor;
    private final AccountsService accountsService;

    public List<FinancialObjectivesVO> getFinancialObjectives() {
        Integer userId = UserUtil.getCurrentUserId();
        // 查询账户计算余额
        CompletableFuture<Double> future = CompletableFuture.supplyAsync(() -> accountsService.queryChain()
                .eq(Accounts::getUserId, userId).list()
                .stream().mapToDouble(o -> o.getAmount().doubleValue()).sum(), executor);
        List<FinancialObjectives> list = queryChain().eq(FinancialObjectives::getUserId, userId)
                .orderBy(FinancialObjectives::getId, false)
                .list();
        Double balance = future.join();
        return list.stream().map(o -> FinancialObjectivesVO.builder()
                .icon(o.getIcon()).finished(o.getFinished()).objective(o.getObjective())
                .amount(o.getAmount().doubleValue()).balance(balance).build()).collect(Collectors.toList());
    }
}
