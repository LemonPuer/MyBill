CREATE TABLE ai_prompt_template (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    theme VARCHAR(50) NOT NULL COMMENT '提示词主题',
    content TEXT NOT NULL COMMENT '提示词内容',
    version INT DEFAULT 1 NOT NULL COMMENT '版本号',
    status TINYINT DEFAULT 1 NOT NULL COMMENT '状态：1-启用，0-禁用',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    UNIQUE KEY uniq_code(theme)
) COMMENT='AI提示词模板表';

CREATE TABLE `tt_retry_task` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_type` VARCHAR(50) NOT NULL COMMENT '任务类型（如：TELEGRAM_BILL_PARSE）',
    `task_data` TEXT NOT NULL COMMENT '任务数据（JSON格式）',
    `user_id` INT NOT NULL COMMENT '关联用户ID',
    `retry_count` INT DEFAULT 0 NOT NULL COMMENT '已重试次数',
    `max_retry_count` INT DEFAULT 3 NOT NULL COMMENT '最大重试次数',
    `status` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '状态：0-待重试，1-重试中，2-重试成功，3-重试失败',
    `error_message` TEXT COMMENT '错误信息',
    `success_result` TEXT COMMENT '成功结果（JSON格式）',
    `notify_status` TINYINT(1) DEFAULT 0 COMMENT '通知状态：0-未通知，1-已通知',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_status` (`user_id`, `status`),
    KEY `idx_task_type` (`task_type`),
    KEY `idx_status_notify` (`status`, `notify_status`)
)COMMENT='任务失败重试表';
