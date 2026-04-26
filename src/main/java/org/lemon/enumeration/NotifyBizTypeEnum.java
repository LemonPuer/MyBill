package org.lemon.enumeration;

import lombok.Getter;

/**
 * 通知业务类型
 *
 * @author Lemon
 * @since 2026-04-22
 */
@Getter
public enum NotifyBizTypeEnum {

    /**
     * 每日记账提醒
     */
    DAILY_BOOKKEEPING("每日记账提醒"),

    /**
     * 月度摘要通知
     */
    MONTHLY_SUMMARY("月度摘要");

    /**
     * 类型说明
     */
    private final String desc;

    NotifyBizTypeEnum(String desc) {
        this.desc = desc;
    }
}
