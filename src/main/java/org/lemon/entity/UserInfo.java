package org.lemon.entity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/10/02 11:59:17
 */
@Getter
public class UserInfo implements UserDetails {
    /**
     * 邮件
     */
    private String email;
    /**
     * 密码
     */
    private String password;
    /**
     * 用户名
     */
    private final String username;
    /**
     * 角色
     */
    private final Set<GrantedAuthority> roles;
    /**
     * 权限
     */
    private final Set<GrantedAuthority> authorities;

    public UserInfo(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        this(null, username, password, new ArrayList<>(), authorities);
    }

    public UserInfo(String email, String username, String password, Collection<? extends GrantedAuthority> roles, Collection<? extends GrantedAuthority> authorities) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.roles = Collections.unmodifiableSet(sortAuthorities(roles));
        this.authorities = Collections.unmodifiableSet(sortAuthorities(authorities));
    }

    private Set<? extends GrantedAuthority> sortAuthorities(Collection<? extends GrantedAuthority> authorities) {
        if (CollectionUtils.isEmpty(authorities)) {
            return Collections.emptySet();
        }
        Set<GrantedAuthority> sortedAuthorities = new HashSet<>();
        for (GrantedAuthority grantedAuthority : authorities) {
            Assert.notNull(grantedAuthority, "角色/权限不能为空！");
            sortedAuthorities.add(grantedAuthority);
        }

        return sortedAuthorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
