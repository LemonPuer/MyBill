package org.lemon.entity;

import com.mybatisflex.annotation.Column;
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
 * AI提示词模板表 实体类。
 *
 * @author Lemon
 * @since 2026-02-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("ai_prompt_template")
public class AiPromptTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Auto)
    private Integer id;

    /**
     * 提示词主题
     */
    private String theme;

    /**
     * 提示词内容
     */
    private String content;

    /**
     * 版本号
     */
    @Column(version = true)
    private Integer version;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

}
