package org.lemon.entity.resp;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/17 23:19:56
 */
@Data
@Builder
public class ConsumerTrendsVO {

    /**
     * 收入金额
     */
    private BigDecimal totalIncome;

    /**
     * 支出金额
     */
    private BigDecimal totalExpense;

    /**
     * 月度结余
     */
    private BigDecimal totalBalance;

    /**
     * 月份
     */
    private String month;
}
