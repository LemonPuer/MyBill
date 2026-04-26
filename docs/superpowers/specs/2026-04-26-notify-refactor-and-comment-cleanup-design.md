# 通知调度分层重构与注释整理设计

## 背景

当前本地分支尚未推送到远程的改动已经补齐了通知中心、找回密码、月度摘要等功能，但在代码评审视角下还有两类明显问题：

- 一批新增或修改的 `controller`、`dto`、`req`、`resp`、`entity`、`service`、`enum`、`mapper` 注释不完整，仍存在 `description: add a description` 这类占位内容
- `ScheduleController` 内聚合了通知编排、payload 组装、回流链接拼接、单条通知发送等业务逻辑，职责超出控制层边界

如果直接以当前形态推送，短期内虽然能工作，但会继续放大通知相关代码的维护成本，也不利于后续代码评审和扩展。

## 目标

- 将通知调度代码从控制层下沉到清晰分层的 service 结构中
- 保持当前通知业务规则不变，只做职责调整，不顺手改业务语义
- 仅在“尚未推送到远程的改动范围”内补齐明显缺失的注释
- 让这批代码达到更符合当前仓库习惯的可读性和分层规范

## 范围边界

本次纳入：

- `ScheduleController` 的通知相关职责下沉
- 通知调度链路的分层拆分
- 未推送提交范围内已新增或修改文件的注释整理
- 与上述重构直接相关的依赖注入和方法迁移调整

本次不纳入：

- 全仓库历史注释清理
- 通知中心数据库结构调整
- 找回密码、登录通知、账单查询等与本次结构问题无关的额外重构
- 新增新的通知渠道、管理后台或复杂重试策略

## 设计原则

- `controller` 只保留请求入口和调度入口，不承载通知业务编排
- 编排逻辑与底层能力分离：谁该发、发什么、如何渲染、如何发送分别归属不同 service
- 尽量复用当前已经存在的 `NotifyPreferenceService`、`NotifyRecordService`、`NotifyTemplateService`、`EmailSendService`
- 新增类数量保持克制，只为明确职责边界而拆，不为“形式上分层”制造过多空壳类
- 注释整理仅覆盖这次未推送改动涉及的文件，不扩散到无关历史代码

## 总体方案

采用“入口层 + 编排层 + 业务准备层 + 基础能力层”的四层结构：

1. 入口层：`ScheduleController`
2. 编排层：`NotifyScheduleService`
3. 业务准备层：`DailyBookkeepingNotifyService`、`MonthlySummaryNotifyService`
4. 基础能力层：`NotifyPreferenceService`、`NotifyRecordService`、`NotifyTemplateService`、`EmailSendService`

### 入口层

`ScheduleController` 调整后仅负责：

- 定时任务触发 `monthStatistics`
- 定时任务触发“生成每日记账提醒记录”
- 定时任务触发“生成月度摘要记录”
- 定时任务触发“发送到期通知记录”
- 手动触发月度统计接口

`ScheduleController` 不再负责：

- 组装通知 payload
- 拼接跳转链接
- 渲染和发送单条通知
- 决定通知记录如何创建和如何更新状态

### 编排层

新增 `NotifyScheduleService`，作为通知调度的总编排入口。

职责：

- 对外暴露“生成每日记账提醒记录”入口
- 对外暴露“生成月度摘要记录”入口
- 对外暴露“发送到期通知记录”入口
- 统一编排“读取通知记录 -> 渲染模板 -> 发送邮件 -> 回写状态”链路
- 承担少量通知调度共用逻辑，如回流链接拼装

`NotifyScheduleService` 不负责：

- 自己查询哪类用户该收每日提醒
- 自己计算月度摘要数据
- 自己存储通知偏好或通知记录

### 业务准备层

新增 `DailyBookkeepingNotifyService`。

职责：

- 查询当前小时应接收每日记账提醒的用户
- 为单个用户构建每日提醒 payload
- 创建每日提醒待发送记录

新增 `MonthlySummaryNotifyService`。

职责：

- 查询开启月度摘要的用户
- 读取上个月月度汇总数据
- 为单个用户构建月度摘要 payload
- 创建月度摘要待发送记录

这一层只负责“准备某类通知”，不直接发邮件。

### 基础能力层

以下 service 保持既有职责，不做横向膨胀：

- `NotifyPreferenceService`：通知偏好读取与更新
- `NotifyRecordService`：通知记录创建、查询、状态流转、幂等控制
- `NotifyTemplateService`：按模板编码渲染标题与正文
- `EmailSendService`：通过 API 或 SMTP 发送邮件

## 目标调用链

### 每日记账提醒

1. `ScheduleController` 的定时任务方法触发
2. 调用 `NotifyScheduleService.generateDailyBookkeepingNotifyRecords()`
3. `NotifyScheduleService` 调用 `DailyBookkeepingNotifyService`
4. `DailyBookkeepingNotifyService` 查询偏好用户、构建 payload、创建 `PENDING` 记录

### 月度摘要通知

1. `ScheduleController` 的定时任务方法触发
2. 调用 `NotifyScheduleService.generateMonthlySummaryNotifyRecords()`
3. `NotifyScheduleService` 调用 `MonthlySummaryNotifyService`
4. `MonthlySummaryNotifyService` 查询偏好用户、读取上月汇总、构建 payload、创建 `PENDING` 记录

### 发送补偿

1. `ScheduleController` 的定时任务方法触发
2. 调用 `NotifyScheduleService.sendDueNotifyRecords()`
3. `NotifyScheduleService` 从 `NotifyRecordService` 查询到期可发送记录
4. 按记录模板编码调用 `NotifyTemplateService` 渲染邮件内容
5. 调用 `EmailSendService` 发送
6. 通过 `NotifyRecordService` 标记成功或失败

## 类与方法落点

### ScheduleController

保留：

- `monthStatistics()` 定时任务
- `generateDailyBookkeepingNotifyRecords()` 定时任务入口
- `generateMonthlySummaryNotifyRecords()` 定时任务入口
- `sendDailyBookkeepingNotifyRecords()` 发送补偿入口
- `monthStatistics(ApiReq<IdReq>)` 手动接口

移除出去的逻辑：

- `buildDailyBookkeepingPayload`
- `buildMonthlySummaryPayload`
- `buildBillsActionUrl`
- `buildDashboardActionUrl`
- `sendNotifyRecord`

### NotifyScheduleService

建议对外方法：

- `generateDailyBookkeepingNotifyRecords()`
- `generateMonthlySummaryNotifyRecords()`
- `sendDueNotifyRecords()`

内部方法可保留少量私有辅助，例如：

- 构建 bills / dashboard 回流链接
- 发送单条通知记录

### DailyBookkeepingNotifyService

建议对外方法：

- `generatePendingRecords(LocalDateTime now)`

内部行为：

- 根据 `sendHour` 查询用户
- 组装 `userName`、`todayLabel`、`encouragement`、`actionUrl`
- 调用 `NotifyRecordService.createDailyBookkeepingPendingRecord(...)`

### MonthlySummaryNotifyService

建议对外方法：

- `generatePendingRecords(LocalDateTime now)`

内部行为：

- 查询开启月度摘要的用户
- 查询上月 `MonthTotalRecord`
- 组装 `monthLabel`、`totalIncome`、`totalExpense`、`totalBalance`、`actionUrl`
- 调用 `NotifyRecordService.createMonthlySummaryPendingRecord(...)`

## 注释整理策略

只处理未推送到远程的改动范围内新增或修改过的文件，重点覆盖：

- `controller`
- `service`
- `entity`
- `dto`
- `req`
- `resp` / `vo`
- `enum`
- `mapper`

### 类注释

要求：

- 去掉 `description: add a description`
- 改为与当前仓库一致的简洁说明，如“用户信息 控制层。”、“通知发送记录表 实体类。”、“通知模板表 服务层实现。”

### 方法注释

重点补齐：

- controller 对外接口
- service 对外公开方法
- 重构后新增的调度编排入口

对纯内部私有方法，仅在逻辑不直观时加简短注释，不机械补齐。

### 字段注释

重点补齐：

- DTO / Req / Resp / VO 中新增或改名后不够直观的字段
- entity / enum 中这次新增且语义容易依赖上下文理解的字段

不要求把每个显然字段都写成长段注释，保持简洁。

## 受影响文件范围

预期至少涉及：

- `src/main/java/org/lemon/controller/ScheduleController.java`
- `src/main/java/org/lemon/service/NotifyPreferenceService.java`
- `src/main/java/org/lemon/service/NotifyRecordService.java`
- `src/main/java/org/lemon/service/NotifyTemplateService.java`
- `src/main/java/org/lemon/service/EmailSendService.java`
- `src/main/java/org/lemon/service/MonthTotalRecordService.java`
- 通知相关新增 `dto / req / resp / entity / enum / mapper`
- 这次未推送改动范围内存在占位注释或缺失注释的其他相关文件

本次允许新增的主要 service 文件：

- `src/main/java/org/lemon/service/NotifyScheduleService.java`
- `src/main/java/org/lemon/service/DailyBookkeepingNotifyService.java`
- `src/main/java/org/lemon/service/MonthlySummaryNotifyService.java`

## 风险与控制

### 风险 1：逻辑迁移后行为不一致

表现：

- payload 字段缺失或字段名变化
- 发送状态未正确回写
- 定时任务不再覆盖原有链路

控制方式：

- 迁移时保持现有业务规则和字段命名不变
- 优先做“搬迁和分层”，不混入额外行为修改
- 逐段对照迁移前后调用链

### 风险 2：类拆分后注入关系出错

表现：

- Spring 启动失败
- 依赖循环
- 漏注入导致空指针

控制方式：

- 新增 service 仅依赖现有明确下层能力
- 避免让基础能力层反向依赖编排层

### 风险 3：注释整理扩大改动面

表现：

- 顺手改到未推送范围外的历史文件
- 本次提交被注释噪音淹没

控制方式：

- 严格限制在本地未推送提交涉及的文件内整理
- 只替换占位注释和补齐明显缺失注释，不做大规模措辞统一

## 验收口径

- `ScheduleController` 不再承载通知业务编排、payload 组装和单条通知发送逻辑
- 通知调度链路能清晰分为入口层、编排层、业务准备层、基础能力层
- 每日记账提醒和月度摘要的业务规则保持与重构前一致
- 发送补偿链路仍能完成“查询记录 -> 渲染模板 -> 发送邮件 -> 回写状态”
- 未推送改动范围内明显缺失或占位的注释得到补齐
- 整体改动不扩散到无关模块和全仓库历史整理

## 实施说明

本设计只描述本次整理的目标结构与边界，不要求一次性把通知系统抽象成更完整的平台。

如果后续继续扩展站内信、短信、通知模板管理后台，再以当前分层为基础继续演进，而不是在本次提交里一并完成。
