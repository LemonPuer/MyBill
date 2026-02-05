package org.lemon.entity.req;

import lombok.Data;
import org.lemon.entity.common.BasePage;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/17 23:31:19
 */
@Data
public class ConsumerTrendsReq extends BasePage {

    /**
     * 开始时间
     */
    @NotBlank(message = "开始时间不能为空！")
    private LocalDateTime startTime;

    /**
     * 截至时间
     */
    @NotBlank(message = "截至时间不能为空！")
    private LocalDateTime endTime;

    /**
     * 收支类型；AmountTypeEnum
     */
    private Integer amountType;
}
