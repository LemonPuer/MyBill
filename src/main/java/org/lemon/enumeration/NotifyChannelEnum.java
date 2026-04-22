package org.lemon.enumeration;

import lombok.Getter;

/**
 * 通知渠道
 *
 * @author Lemon
 * @since 2026-04-22
 */
@Getter
public enum NotifyChannelEnum {

    EMAIL("邮件");

    private final String desc;

    NotifyChannelEnum(String desc) {
        this.desc = desc;
    }
}
