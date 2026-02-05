package org.lemon.entity.req;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/10 15:41:10
 */
@Data
public class UserLoginReq {

    /**
     * 用户名/邮箱
     */
    @NotBlank(message = "用户名!")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空!")
    private String password;

    /**
     * 设备ID
     */
    private String deviceId;
}
