package org.lemon.entity.resp;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2026/02/07 11:06:30
 */
@Data
public class MonthTotalRecordVO {

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 月份
     */
    private String month;

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
}
