package org.lemon.enumeration;

import lombok.Getter;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/16 23:31:57
 */
@Getter
public enum RatioTypeEnum implements IBaseEnum {

    GROWTH(1, "增长"),

    Decrease(2, "减少");

    private final int code;

    private final String message;

    RatioTypeEnum(int code, String message) {
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
