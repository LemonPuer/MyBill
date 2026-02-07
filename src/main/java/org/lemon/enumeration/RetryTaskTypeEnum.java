package org.lemon.enumeration;

import lombok.Getter;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2026/02/07 12:21:26
 */
@Getter
public enum RetryTaskTypeEnum {
    TELEGRAM_BILL_PARSE("Telegram账单解析")
    ;

    private final String desc;

    RetryTaskTypeEnum(String desc) {
        this.desc = desc;
    }
}
