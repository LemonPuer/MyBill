package org.lemon.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/18 14:32:45
 */
@Data
public class FinanceTransactionsReq {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 金额（支持百万级精度）
     */
    @NotNull(message = "金额不能为空!")
    private BigDecimal amount;

    /**
     * 收支类型;1收入2支出
     */
    @NotNull(message = "收支类型不能为空!")
    private Integer type;

    /**
     * 分类id
     */
    @NotNull(message = "分类不能为空!")
    private Integer categoryId;

    /**
     * 交易时间
     */
    @NotNull(message = "交易时间不能为空!")
    private LocalDateTime transactionDate;

    /**
     * 备注详情
     */
    private String note;
}
