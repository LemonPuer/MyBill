package org.lemon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收支分类表 实体类。
 *
 * @author Lemon
 * @since 2025-05-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tt_email_remain")
public class EmailRemain implements Serializable {

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
     * 接收邮箱
     */
    private String email;

    /**
     * 提醒名称
     */
    private String title;

    /**
     * 金额
     */
    private String amount;

    /**
     * 提醒类型；EmailRemainEnum
     */
    private Integer type;

    /**
     * 提醒时间
     */
    private Date remainDate;

    /**
     * 备注
     */
    private String remark;

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
