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
 * 账户表 实体类。
 *
 * @author Lemon
 * @since 2025-05-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tt_accounts")
public class Accounts implements Serializable {

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
     * 父id
     */
    private Integer pid;

    /**
     * 层级
     */
    private Integer hierarchy;

    /**
     * 账户类型，AccountTypeEnum
     */
    private Integer accountType;

    /**
     * 账户名称，xx银行卡
     */
    private String accountName;

    /**
     * 金额
     */
    private BigDecimal amount;

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
