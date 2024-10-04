package org.lemon.utils;

import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.UserInfo;
import org.lemon.entity.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/10/04 22:24:07
 */
@Slf4j
public class UserUtil {
    /**
     * 获取当前登录用户信息
     *
     * @return
     */
    public static UserInfo getCurrentUserInfo() {
        // 获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new BusinessException("用户未登录!");
        }

        Object details = authentication.getDetails();
        if (!(details instanceof UserInfo)) {
            log.error("用户信息存储异常，当前登录用户信息：{}", details.getClass().getName());
            return null;
        }

        return (UserInfo) details;
    }

}
