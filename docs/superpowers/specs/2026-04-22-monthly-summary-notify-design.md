# 月度摘要通知设计

## 背景

当前系统已经具备：

- `tt_month_total_record` 月度汇总数据
- `monthStatistics()` 月度统计任务
- 基于数据库模板与发送记录的通知中心

任务 8 目标是在此基础上补齐月度摘要邮件，不再读取文件模板，统一走 `tt_notify_template` 模板表与 `tt_notify_record` 发送记录表。

## 目标

- 每月 1 号早上 9 点，向开启月度摘要通知的用户发送上个月摘要
- 数据来源以 `tt_month_total_record` 为准
- 使用 `tt_notify_template` 中的 `MONTHLY_SUMMARY` 模板
- 通过 `tt_notify_record` 记录生成、发送、失败补偿

## 范围

本次纳入：

- 月度摘要模板初始化数据
- 月度摘要记录生成定时任务
- 月度摘要记录发送复用现有发送补偿器

本次不纳入：

- Top 支出分类
- 预算执行摘要
- 图表型邮件内容
- 用户自定义月度摘要发送时间

## 业务规则

- 发送时间：每月 1 号 09:00
- 发送内容：上个月摘要
- 用户条件：
  - `tt_notify_preference.biz_type = MONTHLY_SUMMARY`
  - `channel = EMAIL`
  - `enabled = true`
  - 用户邮箱非空
- 幂等：同一用户同一月份仅生成一条记录

## 数据来源

从 `tt_month_total_record` 读取上个月数据：

- `month`
- `totalIncome`
- `totalExpense`
- `totalBalance`

当用户没有对应月份记录时，不生成月度摘要通知。

## 模板设计

模板编码：`MONTHLY_SUMMARY`

标题模板示例：

- `${monthLabel} 月度收支摘要`

正文模板变量：

- `userName`
- `monthLabel`
- `totalIncome`
- `totalExpense`
- `totalBalance`
- `actionUrl`

模板来源：

- 仅从 `tt_notify_template` 读取
- 不读取 `src/main/resources/email/*.html`

## 记录生成

新增月度摘要记录生成定时器：

- cron：`0 0 9 1 * ?`
- 读取开启月度摘要的用户偏好
- 查询上个月月汇总
- 生成 `PENDING` 记录

`biz_key` 格式：

- `monthly-summary:{userId}:{yyyy-MM}`

## 发送链路

- 复用现有发送补偿定时器
- 发送器按 `templateCode` 渲染 `MONTHLY_SUMMARY`
- 成功标记 `SENT`
- 失败标记 `FAILED`

## 回流链接

- 月度摘要按钮跳转到 `${app.web-base-url}/dashboard`
- 若未配置 `app.web-base-url`，则按钮链接留空

## 影响文件

- `src/main/java/org/lemon/controller/ScheduleController.java`
- `src/main/java/org/lemon/service/NotifyRecordService.java`
- `src/main/java/org/lemon/service/NotifyPreferenceService.java`
- `src/main/java/org/lemon/service/PropertiesService.java`
- `src/main/java/org/lemon/service/MonthTotalRecordService.java`
- `checkList/20260422/DDL.sql`
- `docs/superpowers/2026-04-21-backend-task-document.md`

## 验收口径

- 每月 1 号 9 点为符合条件用户生成月度摘要记录
- 邮件内容来自模板表中的 `MONTHLY_SUMMARY`
- 同一用户同一月份不会重复生成记录
- 摘要金额与 `tt_month_total_record` 一致
