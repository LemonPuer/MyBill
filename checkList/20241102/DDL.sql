CREATE TABLE tt_accounts
(
    id               INT UNSIGNED AUTO_INCREMENT COMMENT '主键',
    pid              INT UNSIGNED NOT NULL COMMENT '父id',
    account_type     VARCHAR(50) NOT NULL COMMENT '账户类型，支付平台/银行等',
    account_name     VARCHAR(50) NOT NULL COMMENT '账户名称，xx银行卡/微信等',
    account_category VARCHAR(50) NOT NULL COMMENT '分类，余额/理财/活期等',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_no        INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
    PRIMARY KEY (id)
) COMMENT '账户表';

CREATE TABLE tt_monthly_balances
(
    id                         BIGINT UNSIGNED AUTO_INCREMENT COMMENT '主键',
    account_id                 INT UNSIGNED NOT NULL COMMENT '账户ID，外键关联 accounts 表',
    balance                    DECIMAL(10, 2) DEFAULT 0.00 COMMENT '账户余额',
    record_month               DATE NOT NULL COMMENT '记录时间',
    salary_income              DECIMAL(10, 2) DEFAULT 0.00 COMMENT '工资收入',
    difference_from_last_month DECIMAL(10, 2) DEFAULT 0.00 COMMENT '和上个月的差值',
    extra                      DECIMAL(10, 2) DEFAULT 0.00 COMMENT '额外收入/支出',
    notes                      VARCHAR(255) COMMENT '备注',
    create_time                DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_no                  INT UNSIGNED NOT NULL COMMENT '创建人id',
    update_time                DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
)COMMENT '月度收支记录表';