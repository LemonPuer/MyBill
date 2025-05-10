package org.lemon.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.User;
import org.lemon.entity.UserInfo;
import org.lemon.entity.exception.BusinessException;
import org.lemon.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/10/02 11:34:22
 */
@Data
@Slf4j
@Component
public class BillUserDetailsServiceImpl implements UserDetailsService {

    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String userInfo) throws UsernameNotFoundException {
        // 1.根据用户名查询用户信息
        User user = userService.getById(getUserId(userInfo));
        if (user == null) {
            throw new BusinessException("用户不存在!");
        }
        // 2.给Admin设置角色权限信息
        List<GrantedAuthority> authorities = new ArrayList<>();
        // authorities内部没有元素也能正常运行
        // 3.把admin对象和authorities封装到UserDetails中
        String userpswd = user.getPassword();
        // 这个User类不完整，我们可以用自己的
        return new UserInfo(user.getId(), user.getUsername(), userpswd, authorities);
    }

    private Long getUserId(String userInfo) {
        if (StrUtil.isBlank(userInfo)) {
            return 0L;
        }
        String[] split = userInfo.split(";");
        if (NumberUtil.isNumber(split[0])) {
            return Long.parseLong(split[0]);
        }
        return 0L;
    }
}
