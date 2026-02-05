package org.lemon.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2026/02/05 23:11:05
 */
@Data
public class ChatFinanceTransactionsDTO {

    /**
     * 收支类型；AmountTypeEnum
     */
    private Integer amountType;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 分类id
     */
    private Integer categoryId;

    /**
     * 消息内容
     */
    private String note;
}
