package org.lemon.entity.req;

import lombok.Data;

/**
 * 忘记密码重置密码请求
 */
@Data
public class UserResetPasswordReq {

    /**
     * 找回密码邮箱
     */
    private String email;

    /**
     * 邮箱验证码
     */
    private String code;

    /**
     * 新密码
     */
    private String newPassword;
}
