package org.lemon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 月度总收支记录表 实体类。
 *
 * @author Lemon
 * @since 2025-05-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tt_month_total_record")
public class MonthTotalRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 关联用户ID
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

    /**
     * 是否需要重新计算
     */
    private Boolean repeat;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}
