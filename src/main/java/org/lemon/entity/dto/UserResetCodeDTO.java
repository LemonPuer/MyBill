package org.lemon.entity.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 忘记密码验证码缓存数据
 */
@Data
@Builder
public class UserResetCodeDTO {

    /**
     * 验证码
     */
    private String code;

    /**
     * 过期时间
     */
    private LocalDateTime expireAt;

    /**
     * 最近发送时间
     */
    private LocalDateTime lastSendAt;

    /**
     * 验证失败次数
     */
    private Integer failedCount;
}
