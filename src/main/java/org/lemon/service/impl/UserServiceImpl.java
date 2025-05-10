package org.lemon.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.User;
import org.lemon.entity.exception.BusinessException;
import org.lemon.entity.req.UserReq;
import org.lemon.mapper.UserMapper;
import org.lemon.service.UserService;
import org.lemon.utils.CheckUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户信息表 服务层实现。
 *
 * @author Lemon
 * @since 2024-10-01
 */
@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private PasswordEncoder passwordEncoder;


    @Override
    public boolean saveOrUpdate(UserReq user) {
        if (user == null) {
            throw new BusinessException("参数异常！");
        }
        User result;
        if (user.getId() == null) {
            result = checkSave(user);
        } else {
            result = checkUpdate(user);
        }
        return saveOrUpdate(result);
    }

    private User checkUpdate(UserReq user) {
        CheckUtil.checkField(null, user.getUsername(), user.getEmail(), user.getPassword());
        if (queryChain().eq(User::getUsername, user.getUsername()).ne(User::getId, user.getId()).exists()) {
            throw new BusinessException("用户名【" + user.getUsername() + "】已存在，不支持修改！");
        }
        String encode = passwordEncoder.encode(user.getPassword());
        return User.builder().username(user.getUsername()).email(user.getEmail()).password(encode).build();
    }

    private User checkSave(UserReq user) {
        CheckUtil.checkField(null, user.getEmail());
        User result = User.builder().email(user.getEmail()).description(user.getDescription()).avatarUrl(user.getAvatarUrl()).build();
        if (StrUtil.isNotBlank(user.getPassword())) {
            String encode = passwordEncoder.encode(user.getPassword());
            result.setPassword(encode);
        }
        return result;
    }
}
