package org.lemon.entity.resp;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/17 23:47:19
 */
@Data
@Builder
public class FinancialObjectivesVO {

    private Integer id;

    /**
     * 目标名称
     */
    private String objective;

    /**
     * 图标
     */
    private String icon;

    /**
     * 目标金额
     */
    private Double amount;

    /**
     * 是否完成
     */
    private Boolean finished;

    /**
     * 余额
     */
    private Double balance;
}
