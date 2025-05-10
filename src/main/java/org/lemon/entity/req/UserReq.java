package org.lemon.entity.req;

import lombok.Data;
import org.lemon.entity.common.BasePage;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/10/01 22:46:17
 */
@Data
public class UserReq extends BasePage {
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
     * 密码
     */
    private String password;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 描述
     */
    private String description;
}
