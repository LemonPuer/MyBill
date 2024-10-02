package org.lemon.controller;

import com.mybatisflex.core.paginate.Page;
import org.lemon.entity.req.UserReq;
import org.lemon.entity.resp.ApiResp;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.lemon.entity.User;
import org.lemon.service.UserService;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 用户信息表 控制层。
 *
 * @author Lemon
 * @since 2024-10-01
 */
@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 添加用户信息表。
     *
     * @param user 用户信息表
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("register")
    public ApiResp<Boolean> saveOrUpdate(@RequestBody UserReq user) {
        return ApiResp.ok(userService.saveOrUpdate(user));
    }

    /**
     * 根据主键删除用户信息表。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Long id) {
        return userService.removeById(id);
    }


    /**
     * 根据用户信息表主键获取详细信息。
     *
     * @param id 用户信息表主键
     * @return 用户信息表详情
     */
    @GetMapping("getInfo/{id}")
    public User getInfo(@PathVariable Long id) {
        return userService.getById(id);
    }

    /**
     * 分页查询用户信息表。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @PostMapping("page")
    public Page<User> page(Page<User> page) {
        return userService.page(page);
    }

}
