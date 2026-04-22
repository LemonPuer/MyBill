ALTER TABLE `tt_user`
    ADD COLUMN `password_update_time` DATETIME DEFAULT NULL COMMENT '密码更新时间' AFTER `description`;
