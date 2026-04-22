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

    DAILY_BOOKKEEPING("每日记账提醒"),
    MONTHLY_SUMMARY("月度摘要");

    private final String desc;

    NotifyBizTypeEnum(String desc) {
        this.desc = desc;
    }
}
