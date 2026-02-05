package org.lemon.entity.resp;

import lombok.Data;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/06/07 16:49:45
 */
@Data
public class CategoryVO {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 分类（如餐饮/工资）
     */
    private String category;

    /**
     * 图标
     */
    private String icon;
}
