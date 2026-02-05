package org.lemon.controller;

import lombok.AllArgsConstructor;
import org.lemon.entity.common.*;
import org.lemon.entity.req.*;
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
    private final CategoryService categoryService;
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
     * 获取预算列表
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
     * 消费分类占比
     *
     * @param req
     * @return
     */
    @PostMapping("consumptionStatistics")
    public ApiResp<List<ConsumptionStatisticsVO>> getConsumptionStatistics(@Validated @RequestBody ApiReq<ConsumerTrendsReq> req) {
        return ApiResp.ok(financeTransactionsService.getConsumptionStatistics(req.getData()));
    }

    /**
     * 新增/编辑分类
     *
     * @param req
     * @return
     */
    @PostMapping("saveCategory")
    public ApiResp<Boolean> saveCategory(@Validated @RequestBody ApiReq<CategorySaveReq> req) {
        return ApiResp.ok(categoryService.saveOrUpdateCategory(req.getData()));
    }

    /**
     * 查询分类
     *
     * @param req
     * @return
     */
    @PostMapping("getCategory")
    public PageResp<CategoryVO> getCategory(@Validated @RequestBody ApiReq<BasePage> req) {
        return PageResp.ok(categoryService.getCategory(req.getData()));
    }

    /**
     * 获取财务目标
     *
     * @return
     */
    @PostMapping("financialObjectives")
    public PageResp<FinancialObjectivesVO> getFinancialObjectives(@Validated @RequestBody ApiReq<BasePage> req) {
        return PageResp.ok(financialObjectivesService.getFinancialObjectives(req.getData()));
    }

    /**
     * 获取最近提醒
     *
     * @return
     */
    @PostMapping("recentReminder")
    public PageResp<EmailRemainVO> getRecentReminder(@Validated @RequestBody ApiReq<BasePage> req) {
        return PageResp.ok(emailRemainService.getRecentReminder(req.getData()));
    }

    /**
     * 新增/编辑账单
     *
     * @param req
     * @return
     */
    @PostMapping("saveFinanceTransactions")
    public ApiResp<Boolean> saveFinanceTransactions(@Validated @RequestBody ApiReq<FinanceTransactionsReq> req) {
        return ApiResp.ok(financeTransactionsService.saveOrUpdateFinanceTransactions(req.getData()));
    }

    /**
     * 删除账单
     *
     * @param req
     * @return
     */
    @PostMapping("delFinanceTransactions")
    public ApiResp<Boolean> delFinanceTransactions(@Validated @RequestBody ApiReq<IdReq> req) {
        return ApiResp.ok(financeTransactionsService.delFinanceTransactions(req.getData().getId()));
    }

    /**
     * 获取账户列表
     *
     * @return
     */
    @PostMapping("getAccounts")
    public ApiResp<List<AccountVO>> getAccounts(@Validated @RequestBody ApiReq<IdReq> req) {
        return ApiResp.ok(accountsService.getAccounts(req.getData().getId()));
    }

    /**
     * 获取账户树，用于级联选择
     *
     * @return
     */
    @PostMapping("getAccountTree")
    public ApiResp<List<CommonDicVO>> getAccountTree() {
        return ApiResp.ok(accountsService.getAccountTree());
    }

    /**
     * 新增/编辑账户
     *
     * @param req
     * @return
     */
    @PostMapping("saveAccount")
    public ApiResp<Boolean> saveAccount(@Validated @RequestBody ApiReq<AccountReq> req) {
        return ApiResp.ok(accountsService.saveOrUpdateAccount(req.getData()));
    }

    /**
     * 删除账户
     *
     * @param req
     * @return
     */
    @PostMapping("delAccount")
    public ApiResp<Boolean> delAccount(@Validated @RequestBody ApiReq<IdReq> req) {
        Integer id = req.getData().getId();
        return ApiResp.ok(accountsService.delAccount(req.getData().getId()));
    }

    /**
     * 新增/编辑预算
     *
     * @param req
     * @return
     */
    @PostMapping("saveBudget")
    public ApiResp<Boolean> saveBudget(@Validated @RequestBody ApiReq<BudgetReq> req) {
        return ApiResp.ok(budgetService.saveOrUpdateBudget(req.getData()));
    }

    /**
     * 删除预算
     *
     * @param req
     * @return
     */
    @PostMapping("delBudget")
    public ApiResp<Boolean> delBudget(@Validated @RequestBody ApiReq<IdReq> req) {
        return ApiResp.ok(budgetService.removeById(req.getData().getId()));
    }

    /**
     * 新增/编辑提醒
     *
     * @param req
     * @return
     */
    @PostMapping("saveEmailRemain")
    public ApiResp<Boolean> saveEmailRemain(@Validated @RequestBody ApiReq<EmailRemainReq> req) {
        return ApiResp.ok(emailRemainService.saveOrUpdateEmailRemain(req.getData()));
    }

    /**
     * 删除提醒
     *
     * @param req
     * @return
     */
    @PostMapping("delEmailRemain")
    public ApiResp<Boolean> delEmailRemain(@Validated @RequestBody ApiReq<IdReq> req) {
        return ApiResp.ok(emailRemainService.removeById(req.getData().getId()));
    }
}
