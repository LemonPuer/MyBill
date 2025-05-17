package org.lemon.entity.resp;

import lombok.Data;
import org.lemon.entity.User;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/17 18:24:14
 */
@Data
public class UserInfoVO {

    /**
     * 头像
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

    /**
     * 邮箱
     */
    private String email;

    public static UserInfoVO fromUser(User user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setDescription(user.getDescription());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        return vo;
    }

}
