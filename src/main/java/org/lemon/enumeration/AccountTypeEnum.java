package org.lemon.enumeration;

import lombok.Getter;

import java.util.Optional;

/**
 * 账户类型
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/11 21:52:43
 */
@Getter
public enum AccountTypeEnum implements IBaseEnum {
    /**
     * 现金
     */
    CASH(1, "现金"),
    /**
     * 银行卡
     */
    BANK_CARD(2, "银行卡"),
    /**
     * 微信
     */
    WECHAT(3, "微信"),
    /**
     * 支付宝
     */
    ALIPAY(4, "支付宝"),
    ;

    public final int code;

    public final String message;

    AccountTypeEnum(int code, String message) {
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
