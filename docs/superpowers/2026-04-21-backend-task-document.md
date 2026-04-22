# 后端任务文档（执行版）

来源文档：`docs/superpowers/specs/2026-04-21-missing-features-priority-design.md`

后端仓库：`F:\workspace\JavaSpace\MyBill`

状态说明：
- `已具备`：当前后端已有接口或模型，可直接进入联调
- `部分具备`：已有基础接口，但字段、行为或鉴权范围仍需补齐
- `未开始`：当前代码中未看到对应接口或任务能力

## 当前代码基线

### 现有接口入口

- `src/main/java/org/lemon/controller/AppController.java`
  - 已有：`getCashFlowCard`、`getFinanceTransactionsList`、`getBudgetInfo`、`consumerTrends`、`consumptionStatistics`
  - 已有：`saveCategory`、`getCategory`、`delCategory`、`saveFinanceTransactions`、`delFinanceTransactions`、`saveBudget`、`delBudget`
- `src/main/java/org/lemon/controller/UserController.java`
  - 已有：`login`、`register`、`refreshToken`、`getUserInfo`、`updateInfo`、`sendResetCode`、`resetPassword`

### 当前字段现状

- `src/main/java/org/lemon/entity/req/UserUpdateReq.java`
  - 已支持：`avatarUrl`、`description`、`username`
  - 已支持：`email`
- `src/main/java/org/lemon/entity/resp/UserInfoVO.java`
  - 已支持：`avatarUrl`、`description`、`username`、`email`
- `src/main/java/org/lemon/entity/req/NotifyPreferenceUpdateReq.java`
  - 已支持：`emailReminderEnabled`、`monthlySummaryEnabled`、`reminderSendHour`
- `src/main/java/org/lemon/entity/resp/NotifyPreferenceVO.java`
  - 已支持：通知偏好查询返回
- `src/main/java/org/lemon/entity/req/FinanceTransactionsQueryReq.java`
  - 已支持：`type`、`categoryId`、`startTime`、`endTime`、`keyword`
- `src/main/java/org/lemon/entity/resp/BudgetInfoVO.java`
  - 当前字段定稿为：`categoryName`、`icon`、`amount`、`spent`
- `src/main/java/org/lemon/entity/resp/ConsumptionStatisticsVO.java`
  - 当前字段定稿为：`categoryName`、`consumption`

### 鉴权现状

- `src/main/java/org/lemon/config/SecurityConfig.java`
  - 当前放行：`/openApi/**`、`/user/register`、`/user/login`、`/user/sendResetCode`、`/user/resetPassword`

---

## P0

### 目标

补齐核心闭环，确保 Dashboard、账单整理、个人资料、通知设置、分类管理可以稳定联调。

### 任务 1：补齐分类删除接口

- 状态：`已具备`
- 现状依据：`AppController.java` 中没有 `delCategory`，前端已调用 `/app/delCategory`
- 建议落点：
  - Controller：`src/main/java/org/lemon/controller/AppController.java`
  - Request：复用 `org.lemon.entity.common.IdReq`
  - Service：`src/main/java/org/lemon/service/CategoryService.java`
- 执行动作：
  - 新增 `POST /app/delCategory`
  - 入参仅接收分类 `id`
  - 删除前校验分类是否被账单引用
  - 删除前校验分类是否被预算引用
  - 若被引用，返回明确业务失败提示，不做级联删除
- 联调要求：
  - 失败提示文案可直接给前端展示
  - 接口命名保持与前端当前 `deleteCategory` 服务一致
- 验收口径：
  - 未引用分类可删除
  - 已被账单或预算引用的分类不可删除
  - 前端可直接展示失败原因

### 任务 2：确认并稳定 Dashboard 统计接口字段

- 状态：`已具备`
- 现状依据：`AppController.java` 中已有 4 个统计接口，且预算/分类占比字段命名已完成定稿
- 涉及接口：
  - `POST /app/getCashFlowCard`
  - `POST /app/consumerTrends`
  - `POST /app/consumptionStatistics`
  - `POST /app/getBudgetInfo`
- 已定稿字段：
  - `BudgetInfoVO`：`categoryName`、`icon`、`amount`、`spent`
  - `ConsumptionStatisticsVO`：`categoryName`、`consumption`
  - `CashFlowCardVO`：保持 `amount`、`type`、`ratio`、`ratioType`
  - `ConsumerTrendsVO`：保持 `month`、`totalIncome`、`totalExpense`、`totalBalance`
- 当前口径说明：
  - `getBudgetInfo` 返回与查询区间有交集的预算记录
  - `spent` 表示查询时间范围内该预算分类的支出汇总
  - `consumptionStatistics` 按查询区间内支出账单做分类聚合
  - 空数据返回空集合，不返回异常结构
- 暂不纳入本次范围：
  - `consumptionStatistics` 补分类图标字段
  - 重写 Dashboard 时间范围统计逻辑
- 验收口径：
  - Dashboard 在本月、近 3 月、本年三种场景下都能正常取数
  - 预算、趋势、分类占比字段命名稳定且无需前端猜测

### 任务 3：确认并补齐用户资料字段

- 状态：`已具备`
- 现状依据：`UserController.java` 已有 `getUserInfo` 和 `updateInfo`；`UserInfoVO.java` 已返回 `username`、`email`、`avatarUrl`、`description`
- 建议动作：
  - 明确 `updateInfo` 对 `username`、`avatarUrl`、`description` 的更新行为
  - 确认 `getUserInfo` 在更新后立即返回最新数据
  - 明确邮箱是否允许在资料页修改；若不允许，需要与前端约定为只读
- 建议落点：
  - Controller：`UserController.java`
  - Req：`UserUpdateReq.java`
  - Resp：`UserInfoVO.java`
  - Service：`UserService.java`
- 验收口径：
  - 资料保存后重新进入页面能看到最新数据
  - 不允许修改的字段有稳定策略，不出现前后端认知不一致

### 任务 4：确认通知设置字段与保存方式

- 状态：`已具备`
- 现状依据：通知设置已从用户资料接口拆分，改为通知中心独立接口维护
- 建议落点：
  - Controller：`UserController.java`
  - Req：`NotifyPreferenceUpdateReq.java`
  - Resp：`NotifyPreferenceVO.java`
  - Service：`NotifyPreferenceService.java`
  - 表结构：`tt_notify_preference`
- 当前接口：
  - `POST /user/getNotifyPreference`
  - `POST /user/updateNotifyPreference`
- 联调要求：
  - 保存失败时返回明确业务信息
  - 查询接口返回完整通知配置，前端刷新后能回显
- 验收口径：
  - 用户修改通知设置后再次进入页面可看到最新配置
  - 未开启邮件提醒的用户不会被错误纳入任务发送范围

### P0 联调顺序

1. 先补 `delCategory`
2. 再定 Dashboard 统计字段命名和时间范围语义
3. 再确认 `updateInfo/getUserInfo` 是否承接通知设置
4. 最后与前端联调分类删除、资料更新、通知设置、Dashboard 时间切换

---

## P1

### 目标

补齐登录辅助流程、邮件回流能力和账单导出能力。

### 任务 5：新增忘记密码接口

- 状态：`已具备`
- 现状依据：`UserController.java` 中没有发送验证码、重置密码接口；`SecurityConfig.java` 也未放行对应公开接口
- 建议新增接口：
  - `POST /user/sendResetCode`
  - `POST /user/resetPassword`
- 建议落点：
  - Controller：`UserController.java`
  - Req：新增重置密码相关请求对象
  - Service：`UserService.java`
  - 邮件发送：`EmailSendService.java`
  - 重试或任务记录：`RetryTaskService.java` 或独立验证码记录能力
  - 鉴权：`SecurityConfig.java`
- 执行动作：
  - 发送验证码接口支持按邮箱发送
  - 重置密码接口校验邮箱、验证码、新密码
  - 明确验证码有效期
  - 明确发送频率限制
  - 明确错误提示文案
  - 把这两个接口加入匿名放行列表
- 验收口径：
  - 未登录用户可完整走通忘记密码流程
  - 验证码过期、错误、超频均返回明确提示

### 任务 6：增强账单查询能力

- 状态：`已具备`
- 现状依据：`FinanceTransactionsQueryReq.java` 目前已有 `categoryId`，但没有 `keyword`
- 建议落点：
  - Req：`FinanceTransactionsQueryReq.java`
  - Controller：`AppController.java`
  - Service：`FinanceTransactionsService.java`
- 执行动作：
  - 确认 `categoryId` 查询逻辑已生效
  - 新增 `keyword` 字段
  - `keyword` 至少支持备注模糊查询
  - 若可控，额外支持按分类名模糊查询
  - 保持当前分页参数语义不变
- 验收口径：
  - 账单列表可按分类筛选
  - 账单列表可按关键词搜索
  - 搜索和分页组合使用时结果稳定

### 任务 7：补齐催记账邮件任务

- 状态：`已具备`
- 现状依据：已新增统一通知中心底座、催记账模板、按小时生成记录任务、按分钟发送/补偿任务，并已完成与月度摘要的同链路复用验证
- 建议落点：
  - 通知模板：`tt_notify_template`、`NotifyTemplateService.java`
  - 通知偏好：`tt_notify_preference`、`NotifyPreferenceService.java`
  - 通知记录：`tt_notify_record`、`NotifyRecordService.java`
  - 发送器：`EmailSendService.java`
  - 任务调度：`ScheduleController.java`
- 必须明确：
  - 调度频率
  - 同一天是否重复发送
  - 当天已记账是否跳过
  - 用户关闭提醒后是否立即生效
  - 邮件中的回流链接格式
- 当前定稿：
  - 催记账邮件走统一通知中心，不复用 `tt_email_remain`
  - `tt_email_remain` 继续只承载用户手动提醒事项
  - 模板直接存库，使用 `FreeMarker` 渲染
  - 采用两个定时任务：一个按小时生成记录，一个按分钟发送/补偿
  - 当前业务规则为：到点即发，不判断当天是否已记账
  - 同一用户同一天同一业务类型通过 `biz_key` 保证只生成一条记录
- 验收口径：
  - 满足发送条件的用户能收到邮件
  - 不满足条件的用户不会误发
  - 邮件内容能稳定跳回 `Home` 或 `Bills`

### 任务 8：补齐月度摘要邮件任务

- 状态：`已具备`
- 现状依据：已在统一通知中心中补齐月度摘要模板、记录生成任务与发送复用链路
- 建议落点：
  - 定时调度：`ScheduleController.java`
  - 记录生成：`NotifyRecordService.java`
  - 汇总查询：`MonthTotalRecordService.java`
  - 模板种子：`checkList/20260422/DDL.sql`
- 必须明确：
  - 发送时间点：每月 1 号 `09:00`
  - 数据口径：以上个月 `tt_month_total_record` 为准，仅包含 `totalIncome`、`totalExpense`、`totalBalance`
  - 发送条件：`MONTHLY_SUMMARY` 偏好启用、用户邮箱非空
  - 幂等规则：`biz_key = monthly-summary:{userId}:{yyyy-MM}`
  - 跳转链接：`${app.web-base-url}/dashboard`，未配置时留空
- 当前定稿：
  - 月度摘要不新增独立发送器，复用通知中心现有发送/补偿任务
  - 模板仅存储在 `tt_notify_template`，不增加文件模板
  - 若用户上个月没有 `tt_month_total_record` 记录，则不生成通知记录
- 验收口径：
  - 邮件核心数据与 Dashboard 保持一致
  - 邮件在月度固定时间发送

### 任务 9：新增账单导出接口

- 状态：`未开始`
- 现状依据：当前控制器中未看到账单导出接口
- 建议新增接口：
  - `POST /app/exportFinanceTransactions` 或与前端协商后的唯一命名
- 建议落点：
  - Controller：`AppController.java`
  - Req：新增导出请求对象，包含时间范围等筛选条件
  - Service：`FinanceTransactionsService.java`
- 必须明确：
  - 导出格式，建议首期仅支持 `xlsx` 或 `csv`
  - 时间范围筛选规则
  - 文件命名规则
  - 空数据时的导出行为
  - 导出失败时的错误返回结构
- 验收口径：
  - 前端可发起导出并拿到文件
  - 失败时有明确错误提示，不是静默失败

### P1 联调顺序

1. 先补忘记密码接口和匿名放行配置
2. 再补账单关键词搜索能力
3. 再明确邮件发送规则与摘要口径
4. 最后补账单导出接口并与前端联调下载行为

---

## P2

### 目标

只做增强能力的接口设计，不作为当前主线交付阻塞项。

### 任务 10：批量整理接口设计

- 状态：`未开始`
- 建议内容：
  - 批量删除账单
  - 批量修改分类
  - 明确部分成功、部分失败时的返回结构

### 任务 11：默认记账偏好持久化设计

- 状态：`未开始`
- 建议内容：
  - 先决定放前端本地还是后端用户设置
  - 如果落后端，再补偏好字段和保存接口

### 任务 12：导出能力增强设计

- 状态：`未开始`
- 建议内容：
  - 多格式导出
  - 周期导出
  - 异步导出任务

---

## 不纳入本轮范围

- 账户管理
- 转账语义建模
- 独立提醒中心
- 独立统计页
- 多维高级分析

## 建议开发顺序

1. P0：`delCategory`、统计字段确认、通知设置字段确认
2. P0 联调：Dashboard、分类删除、个人资料、通知设置
3. P1：忘记密码、关键词搜索、邮件任务、账单导出
4. P2：只做接口设计，不抢主线排期

## 联调前检查清单

- `AppController` 是否已补齐 `delCategory`
- `UserUpdateReq/UserInfoVO` 是否已支持通知设置字段
- `FinanceTransactionsQueryReq` 是否已支持 `keyword`
- 忘记密码公开接口是否已加入 `SecurityConfig` 放行
- Dashboard 统计接口字段命名是否已定稿
- 导出接口命名、格式、失败返回是否已定稿

## 当前完成情况（2026-04-21）

- 已完成：任务 1 `补齐分类删除接口`
- 已完成：任务 2 `确认并稳定 Dashboard 统计接口字段`
- 已完成：任务 3 `确认并补齐用户资料字段`
- 已完成：任务 4 `确认通知设置字段与保存方式`
- 已完成：任务 5 `新增忘记密码接口`
- 已完成：任务 6 `增强账单查询能力`
- 未完成：任务 7 `补齐催记账邮件任务`
- 未完成：任务 8 `补齐月度摘要邮件任务`
- 未完成：任务 9 `新增账单导出接口`
- 未开始：任务 10 `批量整理接口设计`
- 未开始：任务 11 `默认记账偏好持久化设计`
- 未开始：任务 12 `导出能力增强设计`

## 当前最优先的后端项

- 补 `POST /app/delCategory`
- 扩 `UserUpdateReq` 与 `UserInfoVO` 的通知设置字段
- 明确 Dashboard 统计字段命名并固定口径
- 新增忘记密码接口并放行匿名访问
- 为账单查询补 `keyword` 搜索能力
