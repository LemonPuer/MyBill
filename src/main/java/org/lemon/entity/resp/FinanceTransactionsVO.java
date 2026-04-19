package org.lemon.entity.resp;

import lombok.Builder;
import lombok.Data;

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

    private Integer id;

    /**
     * 金额（支持百万级精度）
     */
    private BigDecimal amount;

    /**
     * 收支类型;1收入2支出
     */
    private Integer type;

    /**
     * 分类ID
     */
    private Integer categoryId;

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
    private LocalDateTime transactionDate;

    /**
     * 备注详情
     */
    private String note;

}
