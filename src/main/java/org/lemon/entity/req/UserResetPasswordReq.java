package org.lemon.entity.req;

import lombok.Data;

/**
 * 忘记密码重置密码请求
 */
@Data
public class UserResetPasswordReq {

    private String email;

    private String code;

    private String newPassword;
}
