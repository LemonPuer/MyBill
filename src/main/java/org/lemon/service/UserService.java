package org.lemon.service;

import com.mybatisflex.core.service.IService;
import org.lemon.entity.User;
import org.lemon.entity.req.UserReq;

/**
 * 用户信息表 服务层。
 *
 * @author Lemon
 * @since 2024-10-01
 */
public interface UserService extends IService<User> {
    /**
     * 新增或更新
     *
     * @param user
     * @return
     */
    boolean saveOrUpdate(UserReq user);
}
