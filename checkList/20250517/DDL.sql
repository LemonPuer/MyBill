CREATE TABLE `tt_month_total_record`
(
    `id`            int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       INT            NOT NULL COMMENT '关联用户ID',
    `month`         VARCHAR(16)    NOT NULL COMMENT '月份',
    `total_income`  DECIMAL(12, 2) NOT NULL default 0.0 COMMENT '收入金额',
    `total_expense` DECIMAL(12, 2) NOT NULL default 0.0 COMMENT '支出金额',
    `total_balance` DECIMAL(12, 2) NOT NULL default 0.0 COMMENT '月度结余',
    `repeat`        TINYINT(1) NOT NULL default 0 COMMENT '是否重复',
    `created_at`    datetime                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    primary key (id),
    unique key uniq_user_month(user_id, month)
) COMMENT='月度总收支记录表';

create table `tt_financial_objectives`
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     INT            NOT NULL COMMENT '关联用户ID',
    `objective`   VARCHAR(50)    NOT NULL COMMENT '目标名称',
    `icon`        VARCHAR(200)   NOT NULL COMMENT '图标',
    `amount`      DECIMAL(12, 2) NOT NULL COMMENT '目标金额',
    `finished`    TINYINT(1) NOT NULL COMMENT '是否完成',
    `create_no`   int unsigned NOT NULL COMMENT '创建人',
    `created_at`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_no`   int unsigned COMMENT '更新人',
    PRIMARY KEY (id),
    key           idx_user(user_id)
) COMMENT='财务目标表';