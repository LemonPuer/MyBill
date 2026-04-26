package org.lemon.entity.req;

import lombok.Data;

/**
 * 忘记密码发送验证码请求
 */
@Data
public class UserSendResetCodeReq {

    /**
     * 找回密码邮箱
     */
    private String email;
}
