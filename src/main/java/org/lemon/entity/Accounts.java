package org.lemon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 账户表 实体类。
 *
 * @author Lemon
 * @since 2024-11-03
 */
@Data
@Builder
@Table("tt_accounts")
public class Accounts implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 父id
     */
    private Long pid;

    /**
     * 账户类型，支付平台/银行等
     */
    private String accountType;

    /**
     * 账户名称，xx银行卡/微信等
     */
    private String accountName;

    /**
     * 分类，余额/理财/活期等
     */
    private String accountCategory;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 创建人
     */
    private Long createNo;

}
