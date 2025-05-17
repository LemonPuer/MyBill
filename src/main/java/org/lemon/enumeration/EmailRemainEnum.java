package org.lemon.enumeration;

import lombok.Getter;

/**
 * 提醒类型
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/11 22:28:23
 */
@Getter
public enum EmailRemainEnum implements IBaseEnum {

    ONE_OFF(1, "一次性"),

    EVERY_WEEK(2, "每周"),

    MONTHLY(3, "每月"),

    EVERY_YEAR(4, "每年"),
    ;

    private int code;

    private String message;

    EmailRemainEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getKey() {
        return getCode();
    }

    @Override
    public String getValue() {
        return getMessage();
    }
}
