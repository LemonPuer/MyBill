CREATE TABLE `tt_user`
(
    `id`           int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`     varchar(50)  NOT NULL COMMENT '用户名',
    `email`        varchar(255) NOT NULL COMMENT '邮件地址',
    `password`     varchar(255) NOT NULL COMMENT '密码',
    `avatar_url`   varchar(255)          DEFAULT NULL COMMENT '头像地址',
    `description`  varchar(255)          DEFAULT NULL COMMENT '描述',
    `created_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_name` (`username`)
) COMMENT='用户信息表';