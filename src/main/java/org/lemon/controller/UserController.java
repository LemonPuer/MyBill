package org.lemon.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.lemon.entity.common.ApiReq;
import org.lemon.entity.common.ApiResp;
import org.lemon.entity.common.StringReq;
import org.lemon.entity.req.UserLoginReq;
import org.lemon.entity.req.NotifyPreferenceUpdateReq;
import org.lemon.entity.req.UserResetPasswordReq;
import org.lemon.entity.req.UserRegisterReq;
import org.lemon.entity.req.UserSendResetCodeReq;
import org.lemon.entity.req.UserTokenFreshReq;
import org.lemon.entity.req.UserUpdateReq;
import org.lemon.entity.resp.NotifyPreferenceVO;
import org.lemon.entity.resp.UserInfoVO;
import org.lemon.entity.resp.UserTokenVO;
import org.lemon.service.NotifyPreferenceService;
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
    private NotifyPreferenceService notifyPreferenceService;

    /**
     * 用户登录并返回令牌。
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
     * 发送找回密码验证码
     *
     * @param req 邮箱信息
     * @return 是否发送成功
     */
    @PostMapping("sendResetCode")
    public ApiResp<Boolean> sendResetCode(@Validated @RequestBody ApiReq<UserSendResetCodeReq> req) {
        return ApiResp.ok(userService.sendResetCode(req.getData()));
    }

    /**
     * 重置密码
     *
     * @param req 重置密码参数
     * @return 是否重置成功
     */
    @PostMapping("resetPassword")
    public ApiResp<Boolean> resetPassword(@Validated @RequestBody ApiReq<UserResetPasswordReq> req) {
        return ApiResp.ok(userService.resetPassword(req.getData()));
    }

    /**
     * 刷新访问令牌。
     */
    @PostMapping("refreshToken")
    public ApiResp<UserTokenVO> refreshToken(@Validated @RequestBody ApiReq<UserTokenFreshReq> req, HttpServletResponse response) {
        UserTokenVO vo = userService.refreshToken(req.getData());
        response.setHeader("Authorization", "Bearer " + vo.getAccessToken());
        return ApiResp.ok(vo);
    }

    /**
     * 获取当前用户信息。
     */
    @PostMapping("getUserInfo")
    public ApiResp<UserInfoVO> getUserInfo() {
        return ApiResp.ok(UserInfoVO.fromUser(userService.getById(UserUtil.getCurrentUserId())));
    }

    /**
     * 更新当前用户资料。
     */
    @PostMapping("updateInfo")
    public ApiResp<Boolean> updateInfo(@Validated @RequestBody ApiReq<UserUpdateReq> req) {
        return ApiResp.ok(userService.updateInfo(req.getData()));
    }

    /**
     * 获取当前用户通知偏好
     *
     * @return 当前通知偏好
     */
    @PostMapping("getNotifyPreference")
    public ApiResp<NotifyPreferenceVO> getNotifyPreference() {
        return ApiResp.ok(notifyPreferenceService.getCurrentUserNotifyPreference());
    }

    /**
     * 更新当前用户通知偏好
     *
     * @param req 通知偏好设置
     * @return 是否更新成功
     */
    @PostMapping("updateNotifyPreference")
    public ApiResp<Boolean> updateNotifyPreference(@Validated @RequestBody ApiReq<NotifyPreferenceUpdateReq> req) {
        return ApiResp.ok(notifyPreferenceService.updateCurrentUserNotifyPreference(req.getData()));
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
