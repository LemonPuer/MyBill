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
