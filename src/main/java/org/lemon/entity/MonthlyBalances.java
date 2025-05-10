package org.lemon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 月度收支记录表 实体类。
 *
 * @author Lemon
 * @since 2024-11-03
 */
@Data
@Builder
@Table("tt_monthly_balances")
public class MonthlyBalances implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Auto)
    private BigInteger id;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 账户余额
     */
    private BigDecimal balance;

    /**
     * 记录时间
     */
    private Date recordMonth;

    /**
     * 工资收入
     */
    private BigDecimal salaryIncome;

    /**
     * 和上个月的差值
     */
    private BigDecimal differenceFromLastMonth;

    /**
     * 额外收入/支出
     */
    private BigDecimal extra;

    /**
     * 备注
     */
    private String notes;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人id
     */
    private Long createNo;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
