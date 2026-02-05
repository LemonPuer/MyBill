package org.lemon.entity.resp;

import lombok.Data;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/06/07 17:53:50
 */
@Data
public class ConsumptionStatisticsVO {

    /**
     * 分类名称
     */
    private String category;

    /**
     * 消费金额
     */
    private Double consumption;
}
