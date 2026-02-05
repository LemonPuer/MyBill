package org.lemon.entity.req;

import lombok.Data;
import org.lemon.entity.common.BasePage;

import java.time.LocalDateTime;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/17 21:43:03
 */
@Data
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
     * 账户id
     */
    private Integer accountId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
