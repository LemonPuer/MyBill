package org.lemon.entity.resp;

import lombok.Data;

/**
 * 本月收支卡片
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/16 23:19:55
 */
@Data
public class CashFlowCardVO {

    /**
     * 金额
     */
    private Double amount;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 比率
     */
    private String ratio;

    /**
     * 比率类型
     */
    private Integer ratioType;
}
