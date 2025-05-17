package org.lemon.entity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

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
     * 用户标识
     */
    private Integer id;
    /**
     * 用户名
     */
    private final String username;
    // /**
    //  * 角色
    //  */
    // private final Set<GrantedAuthority> roles;
    // /**
    //  * 权限
    //  */
    // private final Set<GrantedAuthority> authorities;

    public UserInfo(Integer id, String username) {
        this.id = id;
        this.username = username;
    }

    // private Set<? extends GrantedAuthority> sortAuthorities(Collection<? extends GrantedAuthority> authorities) {
    //     if (CollectionUtils.isEmpty(authorities)) {
    //         return Collections.emptySet();
    //     }
    //     Set<GrantedAuthority> sortedAuthorities = new HashSet<>();
    //     for (GrantedAuthority grantedAuthority : authorities) {
    //         Assert.notNull(grantedAuthority, "角色/权限不能为空！");
    //         sortedAuthorities.add(grantedAuthority);
    //     }
    //
    //     return sortedAuthorities;
    // }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return "";
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
