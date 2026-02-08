package org.lemon.enumeration;

import lombok.Getter;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2026/02/08 13:44:19
 */
@Getter
public enum RetryTaskStatusEnum implements IBaseEnum {
    PENDING(0, "待重试"),
    RETRYING(1, "重试中"),
    SUCCESS(2, "重试成功"),
    FAILURE(3, "重试失败"),
    ;
    private final Integer code;
    private final String desc;

    RetryTaskStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public Integer getKey() {
        return 0;
    }

    @Override
    public String getValue() {
        return "";
    }

}
