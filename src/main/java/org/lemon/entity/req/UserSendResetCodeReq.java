package org.lemon.entity.req;

import lombok.Data;

/**
 * 忘记密码发送验证码请求
 */
@Data
public class UserSendResetCodeReq {

    private String email;
}
