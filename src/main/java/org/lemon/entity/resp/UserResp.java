package org.lemon.entity.resp;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/10/04 23:09:30
 */
@Data
@Accessors(chain = true)
public class UserResp {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 描述
     */
    private String description;
}
