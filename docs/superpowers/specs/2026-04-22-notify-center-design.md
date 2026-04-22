# 通知中心设计

## 背景

当前系统邮件能力分散在具体业务中：

- 找回密码直接走 `UserService + EmailSendService`
- 登录通知走固定文件模板
- `tt_email_remain` 承载的是用户手动提醒事项，不适合混入系统自动通知

任务 7 催记账邮件尚未起步，任务 8 月度摘要邮件也未落地。为避免后续维护困难，本次统一抽象一套轻量通知中心，用于系统邮件模板、用户通知偏好和发送记录。

## 目标

- 新增一套统一的系统通知中心基础设施
- 使用 `FreeMarker` 作为完整模板引擎，模板存库
- 将系统通知与用户手动提醒 `tt_email_remain` 职责分离
- 一期先支撑催记账邮件落地，同时为月度摘要复用同一套能力

## 范围边界

本次纳入：

- 系统通知模板管理（数据库存储）
- 用户通知偏好
- 系统通知发送记录 / 发送状态
- 催记账邮件任务生成与发送补偿

本次不纳入：

- 站内信、短信、Telegram 等非邮件渠道
- 模板管理后台页面
- 月度摘要完整业务实现
- 将找回密码/登录通知强制改造到统一发送记录链路

## 总体方案

采用三张核心表：

1. `tt_notify_template`
2. `tt_notify_preference`
3. `tt_notify_record`

其中：

- `tt_notify_template` 负责模板内容与模板元数据
- `tt_notify_preference` 负责用户级通知偏好
- `tt_notify_record` 兼任发送任务表和发送记录表，使用状态驱动发送流程

`tt_email_remain` 保持现状，仅用于用户手动提醒事项，不参与系统通知发送链路。

## 任务流

系统通知统一走两类定时任务：

### 1. 任务生成定时器

职责：

- 根据业务规则生成应发送的通知记录
- 例如催记账按小时扫描符合条件用户
- 只插入 `PENDING` 记录，不直接发邮件

### 2. 发送补偿定时器

职责：

- 扫描 `PENDING` 和允许重试的 `FAILED` 记录
- 按模板渲染邮件并发送
- 成功更新为 `SENT`
- 失败更新为 `FAILED`

这样系统只有一套统一的发送入口，避免“有的场景直接发，有的场景写任务”两套心智模型。

## 表结构

### tt_notify_template

用途：

- 存储模板编码、标题模板、正文模板、启停状态

关键字段：

- `code`：模板编码，如 `DAILY_BOOKKEEPING_REMINDER`
- `channel`：一期固定为 `EMAIL`
- `subject_template`：标题模板
- `body_template`：FreeMarker HTML 模板
- `enabled`：是否启用

### tt_notify_preference

用途：

- 存储用户在不同通知业务下的发送偏好

关键字段：

- `user_id`
- `biz_type`：如 `DAILY_BOOKKEEPING`、`MONTHLY_SUMMARY`
- `channel`
- `enabled`
- `send_hour`：发送小时，0~23
- `extra_config_json`：未来扩展配置

### tt_notify_record

用途：

- 存储系统通知的发送任务与发送结果

关键字段：

- `template_code`
- `biz_type`
- `channel`
- `target`
- `biz_key`：幂等键
- `biz_date`
- `payload_json`
- `status`
- `scheduled_time`
- `sent_time`
- `retry_count`
- `error_message`

推荐状态：

- `PENDING`
- `SENT`
- `FAILED`
- `CANCELLED`

## 模板引擎

采用 `FreeMarker`，模板内容直接从数据库读取。

支持能力：

- 变量替换
- 条件判断
- 列表循环
- 默认值
- 片段复用（若后续需要）

模板渲染输入来自 `tt_notify_record.payload_json`。

示例变量：

- `userName`
- `todayLabel`
- `actionUrl`
- `encouragement`

## 催记账邮件设计

### 业务规则

- 用户开启 `DAILY_BOOKKEEPING` 邮件通知
- 当前小时等于用户配置的 `send_hour`
- 用户邮箱非空
- 不判断当天是否已经记账
- 同一用户同一天同一业务类型只生成一次记录

### 模板风格

- 文案友好、轻提醒
- 强调“记得记录今天的收支情况哦”
- 模板可带主按钮，如“立即记账”

### 幂等策略

`biz_key` 建议格式：

- `daily-bookkeeping:{userId}:{yyyy-MM-dd}`

## 与现有能力的关系

### 保留不动

- `tt_email_remain`
- `EmailRemainService`

说明：

- 它们继续服务用户手动提醒事项
- 不复用来存系统通知

### 逐步接入

- 一期只让催记账邮件完整走通知中心
- 月度摘要后续直接复用
- 找回密码、登录提醒先保持现有业务入口，可后续逐步改为复用模板表

## 代码落点

- 新增实体、Mapper、Service：通知模板/偏好/记录
- 改造 `EmailSendService`：新增 FreeMarker 渲染与数据库模板发送入口
- 新增定时任务：
  - 生成催记账记录
  - 发送待处理通知
- 新增 SQL DDL 与模板初始化数据

## 风险与约束

- 当前仍是单点部署，定时任务按单实例执行设计
- 模板存库后，需要对模板启用状态和模板编码唯一性做约束
- 一期不做复杂失败重试策略，先以有限次数重试为主
