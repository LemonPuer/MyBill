drop table if exists tt_accounts;

CREATE TABLE `tt_accounts`
(
    `id`               int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`          INT unsigned NOT NULL COMMENT '关联用户ID',
    `pid`              int unsigned NOT NULL default 0 COMMENT '父id',
    `hierarchy`        TINYINT(1) NOT NULL default 1 COMMENT '层级',
    `account_type`     TINYINT(1) NOT NULL COMMENT '账户类型，AccountTypeEnum',
    `account_name`     varchar(50) COMMENT '账户名称，xx银行卡',
    `account_category` TINYINT(1) COMMENT '分类，AccountCategoryEnum',
    `amount`           DECIMAL(12, 2) NOT NULL default 0.0 COMMENT '金额',
    `created_at`       datetime                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_no`        int unsigned COMMENT '创建人',
    `update_time`      datetime                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_no`        int unsigned COMMENT '更新人',
    PRIMARY KEY (`id`),
    unique key uniq_user_account(user_id,account_name)
) COMMENT='账户表';

CREATE TABLE tt_finance_transactions
(
    `id`               int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`          INT            NOT NULL COMMENT '关联用户ID',
    `amount`           DECIMAL(12, 2) NOT NULL COMMENT '金额',
    `type`             TINYINT(1) NOT NULL COMMENT '收支类型;1收入2支出',
    `category_id`      int COMMENT '分类id',
    `transaction_date` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
    `account_id`       INT COMMENT '关联账户ID',
    `note`             varchar(200) COMMENT '备注详情',
    `created_at`       datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_no`        int unsigned COMMENT '创建人',
    `update_time`      datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_no`        int unsigned COMMENT '更新人',
    PRIMARY KEY (id),
    key                idx_user(user_id)
) comment '账单信息表';

create table tt_category
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     INT          NOT NULL COMMENT '关联用户ID',
    `category`    VARCHAR(50)  NOT NULL tCOMMENT '分类（如餐饮/工资）',
    `icon`        VARCHAR(200) NOT NULL COMMENT '图标',
    `created_at`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_no`   int unsigned COMMENT '创建人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_no`   int unsigned COMMENT '更新人',
    PRIMARY KEY (id),
    key           idx_user(user_id)
)comment '收支分类表';

create table tt_budget
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     INT            NOT NULL COMMENT '关联用户ID',
    `category_id` int COMMENT '分类id',
    `amount`      DECIMAL(12, 2) NOT NULL COMMENT '预算金额',
    `start_time`  datetime       NOT NULL COMMENT '预算开始时间',
    `end_time`    datetime       NOT NULL COMMENT '预算结束时间',
    `created_at`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_no`   int unsigned COMMENT '创建人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_no`   int unsigned COMMENT '更新人',
    PRIMARY KEY (id),
    key           idx_user(user_id)
)comment '预算管理表';

create table tt_email_remain
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     INT         NOT NULL COMMENT '关联用户ID',
    `email`       VARCHAR(50) NOT NULL COMMENT '接收邮箱',
    `title`       VARCHAR(200) COMMENT '提醒名称',
    `amount`      VARCHAR(20) COMMENT '金额',
    `type`        TINYINT(1) NOT NULL COMMENT '提醒类型；EmailRemainEnum',
    `remain_date` date        NOT NULL COMMENT '提醒时间',
    `remark`      VARCHAR(200) COMMENT '备注',
    `created_at`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_no`   int unsigned COMMENT '创建人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_no`   int unsigned COMMENT '更新人',
    PRIMARY KEY (id),
    key           idx_user(user_id)
)comment '邮件提醒表';

create table tt_user_token
(
    `id`            int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       INT          NOT NULL COMMENT '关联用户ID',
    `device_id`     VARCHAR(200) NOT NULL COMMENT '设备ID',
    `refresh_token` VARCHAR(200) NOT NULL COMMENT 'token',
    `expire_time`   datetime     NOT NULL COMMENT '过期时间',
    `created_at`    datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_no`     int unsigned COMMENT '创建人',
    `update_time`   datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    unique key uniq_user_account(user_id,device_id)
)comment '用户token表';