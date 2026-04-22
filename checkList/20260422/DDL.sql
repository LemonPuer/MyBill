CREATE TABLE `tt_notify_template` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `template_code` VARCHAR(64) NOT NULL COMMENT '模板编码',
    `template_name` VARCHAR(128) NOT NULL COMMENT '模板名称',
    `biz_type` VARCHAR(64) NOT NULL COMMENT '业务类型',
    `channel` VARCHAR(32) NOT NULL COMMENT '发送渠道',
    `subject_template` VARCHAR(255) NOT NULL COMMENT '主题模板',
    `content_template` MEDIUMTEXT NOT NULL COMMENT '正文模板',
    `enabled` TINYINT(1) DEFAULT 1 NOT NULL COMMENT '状态：1-启用，0-禁用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_template_code` (`template_code`),
    KEY `idx_biz_channel_enabled` (`biz_type`, `channel`, `enabled`)
) COMMENT='通知模板表';

CREATE TABLE `tt_notify_preference` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `biz_type` VARCHAR(64) NOT NULL COMMENT '业务类型',
    `channel` VARCHAR(32) NOT NULL COMMENT '通知渠道',
    `enabled` TINYINT(1) DEFAULT 1 NOT NULL COMMENT '是否启用',
    `send_hour` TINYINT UNSIGNED DEFAULT NULL COMMENT '发送小时，0~23',
    `extra_config_json` JSON DEFAULT NULL COMMENT '扩展配置JSON',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_user_biz_channel` (`user_id`, `biz_type`, `channel`),
    KEY `idx_biz_channel_enabled_hour` (`biz_type`, `channel`, `enabled`, `send_hour`)
) COMMENT='通知偏好表';

CREATE TABLE `tt_notify_record` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `template_code` VARCHAR(64) NOT NULL COMMENT '模板编码',
    `biz_type` VARCHAR(64) NOT NULL COMMENT '业务类型',
    `channel` VARCHAR(32) NOT NULL COMMENT '通知渠道',
    `target` VARCHAR(255) NOT NULL COMMENT '目标地址',
    `biz_key` VARCHAR(128) NOT NULL COMMENT '业务幂等键',
    `biz_date` DATE DEFAULT NULL COMMENT '业务日期',
    `payload_json` JSON DEFAULT NULL COMMENT '模板参数JSON',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待发送，1-发送成功，2-发送失败，3-已取消',
    `scheduled_time` DATETIME DEFAULT NULL COMMENT '计划发送时间',
    `subject_snapshot` VARCHAR(255) DEFAULT NULL COMMENT '主题快照',
    `content_snapshot` MEDIUMTEXT DEFAULT NULL COMMENT '正文快照',
    `retry_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '重试次数',
    `error_message` VARCHAR(1000) DEFAULT NULL COMMENT '失败原因',
    `sent_time` DATETIME DEFAULT NULL COMMENT '发送时间',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_biz_key_channel` (`biz_key`, `channel`),
    KEY `idx_status_scheduled` (`status`, `scheduled_time`),
    KEY `idx_user_biz_date` (`user_id`, `biz_type`, `biz_date`),
    KEY `idx_template_code` (`template_code`)
) COMMENT='通知发送记录表';

INSERT INTO `tt_notify_template` (`template_code`, `template_name`, `biz_type`, `channel`, `subject_template`, `content_template`, `enabled`, `remark`)
VALUES (
    'DAILY_BOOKKEEPING_REMINDER',
    '每日记账提醒模板',
    'DAILY_BOOKKEEPING',
    'EMAIL',
    '${userName!''}，今天也记一笔吧',
    '<!DOCTYPE html><html lang="zh-CN"><body style="margin:0;padding:24px;background:#f5f7fb;font-family:Arial,\'PingFang SC\',sans-serif;color:#1f2937;"><div style="max-width:640px;margin:0 auto;background:#ffffff;border-radius:20px;padding:32px;box-shadow:0 12px 36px rgba(15,23,42,0.08);"><div style="font-size:14px;color:#64748b;margin-bottom:16px;">MyBill 记账提醒</div><h1 style="font-size:24px;line-height:1.4;margin:0 0 16px;">${userName!\'朋友\'}，记得记录一下今天的收支情况哦</h1><p style="font-size:15px;line-height:1.8;margin:0 0 12px;">今天是 ${todayLabel!\'今天\'}，花了什么、赚了什么，顺手记一笔，月底复盘会更轻松。</p><#if encouragement?? && encouragement?has_content><p style="font-size:15px;line-height:1.8;margin:0 0 24px;color:#475569;">${encouragement}</p></#if><#if actionUrl?? && actionUrl?has_content><a href="${actionUrl}" style="display:inline-block;padding:12px 22px;background:#4f46e5;color:#ffffff;text-decoration:none;border-radius:12px;font-size:14px;">立即记账</a></#if><p style="margin:28px 0 0;font-size:12px;color:#94a3b8;">这是一封系统提醒邮件，如你暂时不想接收，可在设置中关闭记账提醒。</p></div></body></html>',
    1,
    '用于每日催记账邮件通知'
);

INSERT INTO `tt_notify_template` (`template_code`, `template_name`, `biz_type`, `channel`, `subject_template`, `content_template`, `enabled`, `remark`)
VALUES (
    'MONTHLY_SUMMARY',
    '月度摘要模板',
    'MONTHLY_SUMMARY',
    'EMAIL',
    '${monthLabel!''} 月度收支摘要',
    '<!DOCTYPE html><html lang="zh-CN"><body style="margin:0;padding:24px;background:#f3f6fb;font-family:Arial,\'PingFang SC\',sans-serif;color:#1f2937;"><div style="max-width:640px;margin:0 auto;background:#ffffff;border-radius:20px;padding:32px;box-shadow:0 12px 36px rgba(15,23,42,0.08);"><div style="font-size:14px;color:#64748b;margin-bottom:16px;">MyBill 月度摘要</div><h1 style="font-size:24px;line-height:1.4;margin:0 0 16px;">${userName!\'朋友\'}，这是你的 ${monthLabel!\'上月\'} 收支摘要</h1><p style="font-size:15px;line-height:1.8;margin:0 0 20px;color:#475569;">收入、支出和结余均来自月度汇总记录，可直接与 Dashboard 对照查看。</p><table style="width:100%;border-collapse:collapse;margin:0 0 24px;"><tr><td style="padding:12px 0;border-bottom:1px solid #e2e8f0;color:#64748b;">总收入</td><td style="padding:12px 0;border-bottom:1px solid #e2e8f0;text-align:right;font-weight:600;">${totalIncome!\'0\'}</td></tr><tr><td style="padding:12px 0;border-bottom:1px solid #e2e8f0;color:#64748b;">总支出</td><td style="padding:12px 0;border-bottom:1px solid #e2e8f0;text-align:right;font-weight:600;">${totalExpense!\'0\'}</td></tr><tr><td style="padding:12px 0;color:#64748b;">月度结余</td><td style="padding:12px 0;text-align:right;font-weight:700;color:#0f766e;">${totalBalance!\'0\'}</td></tr></table><#if actionUrl?? && actionUrl?has_content><a href="${actionUrl}" style="display:inline-block;padding:12px 22px;background:#0f766e;color:#ffffff;text-decoration:none;border-radius:12px;font-size:14px;">查看 Dashboard</a></#if><p style="margin:28px 0 0;font-size:12px;color:#94a3b8;">这是一封系统月度摘要邮件，如你暂时不想接收，可在设置中关闭月度摘要通知。</p></div></body></html>',
    1,
    '用于每月 1 号发送上个月收支摘要邮件'
);
