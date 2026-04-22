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

    private String code;

    private LocalDateTime expireAt;

    private LocalDateTime lastSendAt;

    private Integer failedCount;
}
