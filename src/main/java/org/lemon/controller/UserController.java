package org.lemon.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.lemon.entity.common.ApiReq;
import org.lemon.entity.common.ApiResp;
import org.lemon.entity.common.StringReq;
import org.lemon.entity.req.UserLoginReq;
import org.lemon.entity.req.UserRegisterReq;
import org.lemon.entity.req.UserTokenFreshReq;
import org.lemon.entity.req.UserUpdateReq;
import org.lemon.entity.resp.UserInfoVO;
import org.lemon.entity.resp.UserTokenVO;
import org.lemon.service.UserService;
import org.lemon.utils.UserUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 用户信息 控制层。
 *
 * @author Lemon
 * @since 2024-10-01
 */
@RestController
@RequestMapping("user")
@AllArgsConstructor
public class UserController {

    private UserService userService;

    /**
     * 登录
     *
     * @param req
     * @return
     */
    @PostMapping("login")
    public ApiResp<UserTokenVO> login(@Validated @RequestBody ApiReq<UserLoginReq> req, HttpServletResponse response) {
        UserTokenVO vo = userService.login(req.getData());
        response.setHeader("Authorization", "Bearer " + vo.getAccessToken());
        return ApiResp.ok(vo);
    }

    /**
     * 注册
     *
     * @param req 用户信息表
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("register")
    public ApiResp<Boolean> register(@Validated @RequestBody ApiReq<UserRegisterReq> req) {
        return ApiResp.ok(userService.register(req.getData()));
    }

    /**
     * 刷新token
     *
     * @return
     */
    @PostMapping("refreshToken")
    public ApiResp<UserTokenVO> refreshToken(@Validated @RequestBody ApiReq<UserTokenFreshReq> req, HttpServletResponse response) {
        UserTokenVO vo = userService.refreshToken(req.getData());
        response.setHeader("Authorization", "Bearer " + vo.getAccessToken());
        return ApiResp.ok(vo);
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    @PostMapping("getUserInfo")
    public ApiResp<UserInfoVO> getUserInfo() {
        return ApiResp.ok(UserInfoVO.fromUser(userService.getById(UserUtil.getCurrentUserId())));
    }

    /**
     * 更新用户信息
     *
     * @param req
     * @return
     */
    @PostMapping("updateInfo")
    public ApiResp<Boolean> updateInfo(@Validated @RequestBody ApiReq<UserUpdateReq> req) {
        return ApiResp.ok(userService.updateInfo(req.getData()));
    }


    // /**
    //  * 注销账号
    //  *
    //  * @return {@code true} 删除成功，{@code false} 删除失败
    //  */
    // @PostMapping("remove")
    // public ApiResp<Boolean> remove() {
    //     return ApiResp.ok(userService.removeById(UserUtil.getCurrentUserId()));
    // }

}
