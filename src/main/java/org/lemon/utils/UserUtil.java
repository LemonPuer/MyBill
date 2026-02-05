package org.lemon.utils;

import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.UserInfo;
import org.lemon.entity.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

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
    public static Optional<UserInfo> getCurrentUserInfo() {
        // 获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object details = authentication.getPrincipal();
        if (!(details instanceof UserInfo)) {
            return Optional.empty();
        }
        return Optional.of((UserInfo) details);
    }

    /**
     * 获取当前登录用户名
     *
     * @return
     */
    public static String getCurrentUsername() {
        return getCurrentUserInfo().map(UserInfo::getUsername).orElse("");
    }

    /**
     * 获取当前登录用户id
     *
     * @return
     */
    public static Integer getCurrentUserId() {
        return getCurrentUserInfo().map(UserInfo::getId).orElseThrow(() -> new BusinessException("用户信息不存在！"));
    }

}
