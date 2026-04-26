package org.lemon.entity.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 消费分类统计返回。
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
    private String categoryName;

    /**
     * 消费金额
     */
    private BigDecimal consumption;
}
