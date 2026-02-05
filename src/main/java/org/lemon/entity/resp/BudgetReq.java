package org.lemon.entity.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/18 11:45:18
 */

@Data
public class BudgetReq {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 分类id
     */
    private Integer categoryId;

    /**
     * 预算金额
     */
    private Double amount;

    /**
     * 预算开始时间
     */
    private LocalDateTime startTime;

    /**
     * 预算结束时间
     */
    private LocalDateTime endTime;
}
