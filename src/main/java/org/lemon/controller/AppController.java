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
 * 账单相关接口。
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
    private final CategoryService categoryService;
    private final EmailRemainService emailRemainService;
    private final MonthTotalRecordService monthTotalRecordService;
    private final FinanceTransactionsService financeTransactionsService;
    private final FinancialObjectivesService financialObjectivesService;

    /**
     * 解析用户输入的账单描述。
     */
    @PostMapping("analysis")
    public ApiResp<FinanceTransactionsVO> analysisUserDesc(@Validated @RequestBody ApiReq<StringReq> req) {
        return ApiResp.ok(financeTransactionsService.analysisUserDesc(req.getData()));
    }

    /**
     * 获取指定时间范围内的收支卡片。
     */
    @PostMapping("getCashFlowCard")
    public ApiResp<List<CashFlowCardVO>> getCashFlowCard(@Validated @RequestBody ApiReq<TimeFrameReq> req) {
        return ApiResp.ok(financeTransactionsService.getCashFlowCard(req.getData()));
    }

    /**
     * 分页查询账单列表。
     */
    @PostMapping("getFinanceTransactionsList")
    public PageResp<FinanceTransactionsVO> getFinanceTransactionsList(@Validated @RequestBody ApiReq<FinanceTransactionsQueryReq> req) {
        return PageResp.ok(financeTransactionsService.getFinanceTransactionsList(req.getData()));
    }

    /**
     * 获取预算列表
     *
     * @param req 时间范围
     * @return 预算列表
     */
    @PostMapping("getBudgetInfo")
    public ApiResp<List<BudgetInfoVO>> getBudgetInfo(@Validated @RequestBody ApiReq<TimeFrameReq> req) {
        return ApiResp.ok(budgetService.getBudgetInfo(req.getData()));
    }

    /**
     * 获取消费趋势列表。
     */
    @PostMapping("consumerTrends")
    public PageResp<ConsumerTrendsVO> getConsumerTrends(@Validated @RequestBody ApiReq<ConsumerTrendsReq> req) {
        return PageResp.ok(monthTotalRecordService.getConsumerTrends(req.getData()));
    }

    /**
     * 统计消费分类占比。
     */
    @PostMapping("consumptionStatistics")
    public ApiResp<List<ConsumptionStatisticsVO>> getConsumptionStatistics(@Validated @RequestBody ApiReq<ConsumerTrendsReq> req) {
        return ApiResp.ok(financeTransactionsService.getConsumptionStatistics(req.getData()));
    }

    /**
     * 新增或编辑分类。
     */
    @PostMapping("saveCategory")
    public ApiResp<Boolean> saveCategory(@Validated @RequestBody ApiReq<CategorySaveReq> req) {
        return ApiResp.ok(categoryService.saveOrUpdateCategory(req.getData()));
    }

    /**
     * 分页查询分类。
     */
    @PostMapping("getCategory")
    public PageResp<CategoryVO> getCategory(@Validated @RequestBody ApiReq<BasePage> req) {
        return PageResp.ok(categoryService.getCategory(req.getData()));
    }

    /**
     * 删除分类。
     */
    @PostMapping("delCategory")
    public ApiResp<Boolean> delCategory(@Validated @RequestBody ApiReq<IdReq> req) {
        return ApiResp.ok(categoryService.delCategory(req.getData().getId()));
    }

    /**
     * 获取财务目标
     *
     * @param req 分页参数
     * @return 财务目标列表
     */
    @PostMapping("financialObjectives")
    public PageResp<FinancialObjectivesVO> getFinancialObjectives(@Validated @RequestBody ApiReq<BasePage> req) {
        return PageResp.ok(financialObjectivesService.getFinancialObjectives(req.getData()));
    }

    /**
     * 获取最近提醒
     *
     * @param req 分页参数
     * @return 最近提醒列表
     */
    @PostMapping("recentReminder")
    public PageResp<EmailRemainVO> getRecentReminder(@Validated @RequestBody ApiReq<BasePage> req) {
        return PageResp.ok(emailRemainService.getRecentReminder(req.getData()));
    }

    /**
     * 新增或编辑账单。
     */
    @PostMapping("saveFinanceTransactions")
    public ApiResp<Boolean> saveFinanceTransactions(@Validated @RequestBody ApiReq<FinanceTransactionsReq> req) {
        return ApiResp.ok(financeTransactionsService.saveOrUpdateFinanceTransactions(req.getData()));
    }

    /**
     * 删除账单。
     */
    @PostMapping("delFinanceTransactions")
    public ApiResp<Boolean> delFinanceTransactions(@Validated @RequestBody ApiReq<IdReq> req) {
        return ApiResp.ok(financeTransactionsService.delFinanceTransactions(req.getData().getId()));
    }

    /**
     * 新增或编辑预算。
     */
    @PostMapping("saveBudget")
    public ApiResp<Boolean> saveBudget(@Validated @RequestBody ApiReq<BudgetReq> req) {
        return ApiResp.ok(budgetService.saveOrUpdateBudget(req.getData()));
    }

    /**
     * 删除预算。
     */
    @PostMapping("delBudget")
    public ApiResp<Boolean> delBudget(@Validated @RequestBody ApiReq<IdReq> req) {
        return ApiResp.ok(budgetService.removeById(req.getData().getId()));
    }

    /**
     * 新增或编辑提醒。
     */
    @PostMapping("saveEmailRemain")
    public ApiResp<Boolean> saveEmailRemain(@Validated @RequestBody ApiReq<EmailRemainReq> req) {
        return ApiResp.ok(emailRemainService.saveOrUpdateEmailRemain(req.getData()));
    }

    /**
     * 删除提醒。
     */
    @PostMapping("delEmailRemain")
    public ApiResp<Boolean> delEmailRemain(@Validated @RequestBody ApiReq<IdReq> req) {
        return ApiResp.ok(emailRemainService.removeById(req.getData().getId()));
    }
}
