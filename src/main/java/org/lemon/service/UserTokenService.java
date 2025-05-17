package org.lemon.service;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.lemon.entity.UserToken;
import org.lemon.mapper.UserTokenMapper;
import org.springframework.stereotype.Service;

/**
 * 用户token表 服务层实现。
 *
 * @author Lemon
 * @since 2025-05-17
 */
@Service
public class UserTokenService extends ServiceImpl<UserTokenMapper, UserToken> {

}
