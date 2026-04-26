package org.lemon.enumeration;

import lombok.Getter;

/**
 * 通知发送记录状态
 *
 * @author Lemon
 * @since 2026-04-22
 */
@Getter
public enum NotifyRecordStatusEnum implements IBaseEnum {

    /**
     * 待发送
     */
    PENDING(0, "待发送"),

    /**
     * 发送成功
     */
    SENT(1, "发送成功"),

    /**
     * 发送失败
     */
    FAILED(2, "发送失败"),

    /**
     * 已取消
     */
    CANCELLED(3, "已取消");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态说明
     */
    private final String desc;

    NotifyRecordStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public Integer getKey() {
        return code;
    }

    @Override
    public String getValue() {
        return desc;
    }
}
