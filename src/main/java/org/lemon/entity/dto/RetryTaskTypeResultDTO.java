package org.lemon.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2026/02/07 13:15:19
 */
@Data
@AllArgsConstructor
public class RetryTaskTypeResultDTO {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 数据
     */
    private String data;
}
