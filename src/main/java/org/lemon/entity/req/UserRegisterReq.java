package org.lemon.entity.req;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/10/01 22:46:17
 */
@Data
public class UserRegisterReq {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空!")
    private String username;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空!")
    private String email;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空!")
    private String password;

}
