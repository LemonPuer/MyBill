package org.lemon.entity.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/17 20:20:03
 */
@Data
public class TimeFrameReq {

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
}
