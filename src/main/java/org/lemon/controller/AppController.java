package org.lemon.controller;

import lombok.AllArgsConstructor;
import org.lemon.entity.common.ApiReq;
import org.lemon.entity.common.ApiResp;
import org.lemon.entity.common.PageResp;
import org.lemon.entity.req.AccountReq;
import org.lemon.entity.req.ConsumerTrendsReq;
import org.lemon.entity.req.FinanceTransactionsQueryReq;
import org.lemon.entity.req.TimeFrameReq;
import org.lemon.entity.resp.*;
import org.lemon.service.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/10 11:15:44
 */
@RestController
@RequestMapping("app")
@AllArgsConstructor
public class AppController {

    private final BudgetService budgetService;
    private final AccountsService accountsService;
    private final EmailRemainService emailRemainService;
    private final MonthTotalRecordService monthTotalRecordService;
    private final FinanceTransactionsService financeTransactionsService;
    private final FinancialObjectivesService financialObjectivesService;

    /**
     * 获取收支卡片
     *
     * @param req
     * @return
     */
    @PostMapping("getCashFlowCard")
    public ApiResp<List<CashFlowCardVO>> getCashFlowCard(@Validated @RequestBody ApiReq<TimeFrameReq> req) {
        return ApiResp.ok(financeTransactionsService.getCashFlowCard(req.getData()));
    }

    /**
     * 查询账单列表
     *
     * @param req
     * @return
     */
    @PostMapping("getFinanceTransactionsList")
    public PageResp<FinanceTransactionsVO> getFinanceTransactionsList(@Validated @RequestBody ApiReq<FinanceTransactionsQueryReq> req) {
        return PageResp.ok(financeTransactionsService.getFinanceTransactionsList(req.getData()));
    }

    /**
     * 获取预算信息
     *
     * @return
     */
    @PostMapping("getBudgetInfo")
    public ApiResp<List<BudgetInfoVO>> getBudgetInfo(@Validated @RequestBody ApiReq<TimeFrameReq> req) {
        return ApiResp.ok(budgetService.getBudgetInfo(req.getData()));
    }

    /**
     * 获取消费趋势
     *
     * @param req
     * @return
     */
    @PostMapping("consumerTrends")
    public PageResp<ConsumerTrendsVO> getConsumerTrends(@Validated @RequestBody ApiReq<ConsumerTrendsReq> req) {
        return PageResp.ok(monthTotalRecordService.getConsumerTrends(req.getData()));
    }

    /**
     * 获取财务目标
     *
     * @return
     */
    @PostMapping("financialObjectives")
    public ApiResp<List<FinancialObjectivesVO>> getFinancialObjectives() {
        return ApiResp.ok(financialObjectivesService.getFinancialObjectives());
    }

    /**
     * 获取最近提醒
     *
     * @return
     */
    @PostMapping("recentReminder")
    public ApiResp<List<EmailRemainVO>> getRecentReminder() {
        return ApiResp.ok(emailRemainService.getRecentReminder());
    }

    /**
     * 获取账户列表
     *
     * @return
     */
    @PostMapping("getAccounts")
    public ApiResp<List<AccountVO>> getAccounts() {
        return ApiResp.ok(accountsService.getAccounts());
    }

    /**
     * 保存/编辑账户
     *
     * @param req
     * @return
     */
    @PostMapping("saveAccount")
    public ApiResp<Boolean> saveAccount(@Validated @RequestBody ApiReq<AccountReq> req) {
        return ApiResp.ok(accountsService.saveOrUpdateAccount(req.getData()));
    }

}
