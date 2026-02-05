package org.lemon.entity.req;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/06/07 16:42:33
 */
@Data
public class CategorySaveReq {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 分类（如餐饮/工资）
     */
    @NotBlank(message = "分类名称不能为空!")
    private String category;

    /**
     * 图标
     */
    @NotNull(message = "图标不能为空!")
    private String icon;
}
