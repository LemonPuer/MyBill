package org.lemon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账单信息表 实体类。
 *
 * @author Lemon
 * @since 2025-05-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tt_finance_transactions")
public class FinanceTransactions implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Auto)
    private Integer id;

    /**
     * 关联用户ID
     */
    private Integer userId;

    /**
     * 金额（支持百万级精度）
     */
    private BigDecimal amount;

    /**
     * 收支类型;1收入2支出
     */
    private Integer type;

    /**
     * 分类id
     */
    private Integer categoryId;

    /**
     * 交易时间
     */
    private LocalDateTime transactionDate;

    /**
     * 关联账户ID
     */
    private Integer accountId;

    /**
     * 备注详情
     */
    private String note;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 创建人
     */
    private Integer createNo;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 更新人
     */
    private Integer updateNo;

}
