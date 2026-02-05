package org.lemon.enumeration;

import lombok.Getter;

/**
 * 金额类型
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/16 23:22:22
 */
@Getter
public enum AmountTypeEnum implements IBaseEnum {

    INCOME(1, "收入"),

    EXPENSE(2, "支出"),

    BALANCE(3, "结余");

    private final int code;

    private final String message;

    AmountTypeEnum(int code, String message) {
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
