# Dashboard 字段契约定稿

## 背景

当前后端 Dashboard 相关接口已可用，但部分返回字段命名仍存在联调歧义：

- `BudgetInfoVO.category` 实际表示分类名称
- `BudgetInfoVO.cost` 实际表示当前查询范围内的已花费金额
- `ConsumptionStatisticsVO.category` 实际表示分类名称

前端项目 `F:\workspace\JavaScript\mybill-app` 已经对旧字段和新字段做了兼容映射，因此本次可以直接将后端返回字段统一为更清晰的命名，而不需要保留双字段。

## 目标

- 统一 Dashboard 中“分类名称”类字段的命名
- 统一预算卡片中“已花费金额”字段的命名
- 不扩大改动范围，不顺带重构其他统计接口结构

## 本次定稿范围

涉及接口：

- `POST /app/getBudgetInfo`
- `POST /app/consumptionStatistics`

涉及返回对象：

- `src/main/java/org/lemon/entity/resp/BudgetInfoVO.java`
- `src/main/java/org/lemon/entity/resp/ConsumptionStatisticsVO.java`

## 定稿结论

### 1. BudgetInfoVO

字段命名统一为：

- `categoryName`：分类名称
- `icon`：分类图标
- `amount`：预算金额
- `spent`：当前查询时间范围内该预算分类的已花费金额

不再使用：

- `category`
- `cost`

### 2. ConsumptionStatisticsVO

字段命名统一为：

- `categoryName`：分类名称
- `consumption`：消费金额

不再使用：

- `category`

### 3. 其他 Dashboard 接口

本次不改：

- `CashFlowCardVO`
  - 保持 `amount`、`type`、`ratio`、`ratioType`
- `ConsumerTrendsVO`
  - 保持 `month`、`totalIncome`、`totalExpense`、`totalBalance`

原因：

- 这些字段当前语义已经清晰
- 当前联调矛盾集中在预算与分类占比模块
- 避免为了“形式统一”引入不必要的前后端变更

## 字段语义说明

### BudgetInfoVO.spent

`spent` 表示：

- 当前请求时间范围内
- 当前预算分类下
- 用户所有支出账单金额汇总

它不是：

- 预算生命周期内的历史累计值
- 单条预算记录自身保存的字段

### categoryName

`categoryName` 表示纯展示名称，不表示分类对象、编码、枚举值或分类 ID。

## 时间范围口径

本次不重写统计逻辑，只固定当前接口口径：

- `getBudgetInfo`
  - 返回与查询区间有交集的预算记录
  - `spent` 根据查询区间内的支出账单重新统计
- `consumptionStatistics`
  - 统计查询区间内的支出账单
  - 按分类聚合生成消费占比数据

## 空数据约定

- `getBudgetInfo` 无数据时返回空集合
- `consumptionStatistics` 无数据时返回空集合

不返回异常结构，不返回占位对象。

## 本次不纳入范围

- 不为 `consumptionStatistics` 补图标字段
- 不调整 `CashFlowCardVO` 字段结构
- 不调整 `ConsumerTrendsVO` 字段结构
- 不重做 Dashboard 时间范围计算逻辑

## 影响文件

- `src/main/java/org/lemon/entity/resp/BudgetInfoVO.java`
- `src/main/java/org/lemon/entity/resp/ConsumptionStatisticsVO.java`
- `src/main/java/org/lemon/service/BudgetService.java`
- `src/main/java/org/lemon/service/FinanceTransactionsService.java`
- `docs/superpowers/2026-04-21-backend-task-document.md`

## 风险与兼容性

- 前端当前已有兼容映射，因此直接切换后端字段名风险较低
- 本次不保留双字段，避免契约长期摇摆
- 若其他未检视调用方仍依赖旧字段名，需要在联调时一并确认
