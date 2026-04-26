package org.lemon.entity.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.lemon.entity.common.BasePage;

import java.time.LocalDateTime;

/**
 * 账单分页查询参数。
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/17 21:43:03
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FinanceTransactionsQueryReq extends BasePage {

    /**
     * 收支类型;1收入2支出
     */
    private Integer type;

    /**
     * 分类id
     */
    private Integer categoryId;

    /**
     * 关键字
     */
    private String keyword;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
