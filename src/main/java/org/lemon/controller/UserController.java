package org.lemon.controller;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import org.lemon.entity.User;
import org.lemon.entity.req.UserReq;
import org.lemon.entity.resp.ApiResp;
import org.lemon.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public ApiResp<Boolean> remove(@PathVariable Long id) {
        return ApiResp.ok(userService.removeById(id));
    }


    /**
     * 根据用户信息表主键获取详细信息。
     *
     * @param id 用户信息表主键
     * @return 用户信息表详情
     */
    @GetMapping("detail/{id}")
    public ApiResp<UserReq> getInfo(@PathVariable Integer id) {
        return ApiResp.ok(userService.getOneAs(userService.queryChain().eq(User::getId, id), UserReq.class));
    }

    /**
     * 分页查询用户信息表。
     *
     * @param req 分页对象
     * @return 分页对象
     */
    @PostMapping("page")
    public ApiResp<Page<User>> page(@RequestBody UserReq req) {
        return ApiResp.ok(userService.queryChain().like(User::getUsername, req.getUsername(), StrUtil.isNotBlank(req.getUsername()))
                .or(chain -> {
                    chain.like(User::getEmail, req.getEmail(), StrUtil.isNotBlank(req.getEmail()));
                }).or(chain -> {
                    chain.like(User::getDescription, req.getDescription(), StrUtil.isNotBlank(req.getDescription()));
                }).orderBy(User::getId).asc()
                .page(new Page<>(req.getPageNum(), req.getPageSize())));
    }

}
