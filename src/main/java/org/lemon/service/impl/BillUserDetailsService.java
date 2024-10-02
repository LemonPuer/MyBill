package org.lemon.service.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.User;
import org.lemon.entity.UserInfo;
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
public class BillUserDetailsService implements UserDetailsService {

    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1.根据用户名查询用户信息
        User user = userService.queryChain().eq(User::getUsername, username).one();
        // 2.给Admin设置角色权限信息
        List<GrantedAuthority> authorities = new ArrayList<>();
        // authorities内部没有元素也能正常运行
        // 3.把admin对象和authorities封装到UserDetails中
        String userpswd = user.getPassword();
        // 这个User类不完整，我们可以用自己的
        return new UserInfo(username, userpswd, authorities);
    }
}
