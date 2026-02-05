package org.lemon.entity.resp;

import lombok.Builder;
import lombok.Data;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/17 22:57:30
 */
@Data
@Builder
public class BudgetInfoVO {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 分类
     */
    private String category;

    /**
     * 分类图标
     */
    private String icon;

    /**
     * 预算金额
     */
    private String amount;

    /**
     * 消费金额
     */
    private String cost;
}
