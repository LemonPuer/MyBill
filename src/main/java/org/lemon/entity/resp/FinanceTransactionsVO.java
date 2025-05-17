package org.lemon.entity.resp;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/17 21:37:59
 */
@Data
@Builder
public class FinanceTransactionsVO {

    /**
     * 金额（支持百万级精度）
     */
    private BigDecimal amount;

    /**
     * 收支类型;1收入2支出
     */
    private String typeStr;

    /**
     * 分类
     */
    private String category;

    /**
     * 图标
     */
    private String icon;

    /**
     * 交易时间
     */
    private String transactionDate;

    /**
     * 账户
     */
    private String account;

    /**
     * 备注详情
     */
    private String note;

}
