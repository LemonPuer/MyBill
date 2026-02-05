package org.lemon.entity.req;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/17 19:17:14
 */
@Data
public class UserTokenFreshReq {

    /**
     * refreshToken
     */
    @NotBlank(message = "refreshToken不能为空!")
    private String refreshToken;

    /**
     * 设备id
     */
    @NotBlank(message = "设备ID不能为空!")
    private String deviceId;
}
