package org.lemon.entity.req;

import lombok.Data;

/**
 * 用户资料更新参数。
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/17 18:12:28
 */
@Data
public class UserUpdateReq {

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 描述
     */
    private String description;

    /**
     * 用户名
     */
    private String username;

}
