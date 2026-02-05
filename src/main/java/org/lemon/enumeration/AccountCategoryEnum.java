package org.lemon.enumeration;

import lombok.Getter;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/17 22:08:01
 */
@Getter
public enum AccountCategoryEnum implements IBaseEnum {

    BALANCE(1, "余额"),

    CURRENT(2, "活期"),

    INVESTMENT(3, "投资"),

    LOANS(4, "贷款");

    private final int code;

    private final String message;

    AccountCategoryEnum(int code, String message) {
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
