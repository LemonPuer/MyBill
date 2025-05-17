package org.lemon.entity.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/17 15:49:53
 */
@Data
public class UserTokenVO {

    /**
     * token
     */
    private String accessToken;

    /**
     * 刷新token
     */
    private String refreshToken;

    /**
     * accessToken过期时间
     */
    private LocalDateTime expireTime;
}
