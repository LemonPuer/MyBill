package org.lemon.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.User;
import org.lemon.entity.UserInfo;
import org.lemon.entity.UserToken;
import org.lemon.entity.dto.SystemEmailDTO;
import org.lemon.entity.dto.UserResetCodeDTO;
import org.lemon.entity.exception.BusinessException;
import org.lemon.entity.req.*;
import org.lemon.entity.resp.UserTokenVO;
import org.lemon.enumeration.SystemConstants;
import org.lemon.mapper.UserMapper;
import org.lemon.mapper.UserTokenMapper;
import org.lemon.utils.JwtUtil;
import org.lemon.utils.UserUtil;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 用户信息表 服务层实现。
 *
 * @author Lemon
 * @since 2024-10-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    @Resource(name = "commonExecutor")
    private ThreadPoolTaskExecutor executor;
    private final PasswordEncoder passwordEncoder;
    private final EmailSendService emailSendService;
    private final PropertiesService propertiesService;

    private final UserTokenMapper userTokenMapper;

    private static final int RESET_CODE_LENGTH = 6;
    private static final long RESET_CODE_EXPIRE_MINUTES = 10L;
    private static final long RESET_CODE_RESEND_SECONDS = 60L;
    private static final int RESET_CODE_MAX_FAILED_COUNT = 5;

    private final Cache<String, UserResetCodeDTO> resetCodeCache = Caffeine.newBuilder()
            .expireAfterWrite(RESET_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    private final Cache<String, LocalDateTime> resetCodeThrottleCache = Caffeine.newBuilder()
            .expireAfterWrite(RESET_CODE_RESEND_SECONDS, TimeUnit.SECONDS)
            .maximumSize(1000)
            .build();


    /**
     * 注册新用户
     *
     * @param req 注册参数
     * @return 是否注册成功
     */
    public boolean register(UserRegisterReq req) {
        if (queryChain().eq(User::getUsername, req.getUsername()).exists()) {
            throw new BusinessException("用户名【" + req.getUsername() + "】已存在！");
        }
        User result = User.builder().username(req.getUsername()).email(req.getEmail())
                .passwordUpdateTime(LocalDateTime.now())
                .build();
        String encode = passwordEncoder.encode(req.getPassword());
        result.setPassword(encode);
        return save(result);
    }

    /**
     * 发送找回密码验证码
     *
     * @param req 找回密码参数
     * @return 是否发送成功
     */
    public Boolean sendResetCode(UserSendResetCodeReq req) {
        String email = StrUtil.trim(req.getEmail());
        String cacheKey = getResetEmailCacheKey(email);
        if (StrUtil.isBlank(email)) {
            throw new BusinessException("邮箱不能为空！");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime throttleAt = resetCodeThrottleCache.getIfPresent(cacheKey);
        if (throttleAt != null && Duration.between(throttleAt, now).getSeconds() < RESET_CODE_RESEND_SECONDS) {
            throw new BusinessException("验证码发送过于频繁，请稍后再试！");
        }
        resetCodeThrottleCache.put(cacheKey, now);
        User user = getSingleUserByEmail(email);
        if (user == null) {
            return true;
        }
        String code = RandomUtil.randomNumbers(RESET_CODE_LENGTH);
        if (!sendResetCodeEmail(user, code)) {
            resetCodeThrottleCache.invalidate(cacheKey);
            throw new BusinessException("验证码发送失败，请稍后重试！");
        }
        resetCodeCache.put(cacheKey, UserResetCodeDTO.builder()
                .code(code)
                .expireAt(now.plusMinutes(RESET_CODE_EXPIRE_MINUTES))
                .lastSendAt(now)
                .failedCount(0)
                .build());
        return true;
    }

    /**
     * 校验验证码后重置密码
     *
     * @param req 重置密码参数
     * @return 是否重置成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean resetPassword(UserResetPasswordReq req) {
        String email = StrUtil.trim(req.getEmail());
        String cacheKey = getResetEmailCacheKey(email);
        String code = StrUtil.trim(req.getCode());
        if (StrUtil.isBlank(email)) {
            throw new BusinessException("邮箱不能为空！");
        }
        if (StrUtil.isBlank(code)) {
            throw new BusinessException("验证码不能为空！");
        }
        if (StrUtil.isBlank(req.getNewPassword())) {
            throw new BusinessException("新密码不能为空！");
        }
        User user = getSingleUserByEmail(email);
        UserResetCodeDTO cached = resetCodeCache.getIfPresent(cacheKey);
        LocalDateTime now = LocalDateTime.now();
        if (user == null || cached == null || cached.getExpireAt() == null || cached.getExpireAt().isBefore(now)) {
            resetCodeCache.invalidate(cacheKey);
            throw new BusinessException("验证码错误或已过期，请重新获取！");
        }
        if (!StrUtil.equals(cached.getCode(), code)) {
            int failedCount = Optional.ofNullable(cached.getFailedCount()).orElse(0) + 1;
            if (failedCount >= RESET_CODE_MAX_FAILED_COUNT) {
                cached.setExpireAt(now.minusSeconds(1));
                cached.setFailedCount(failedCount);
                resetCodeCache.put(cacheKey, cached);
                throw new BusinessException("验证码错误次数过多，请重新获取！");
            }
            cached.setFailedCount(failedCount);
            resetCodeCache.put(cacheKey, cached);
            throw new BusinessException("验证码错误！");
        }
        boolean updated = updateChain().eq(User::getId, user.getId())
                .set(User::getPassword, passwordEncoder.encode(req.getNewPassword()))
                .set(User::getPasswordUpdateTime, now)
                .update();
        if (!updated) {
            throw new BusinessException("密码重置失败，请稍后重试！");
        }
        userTokenMapper.deleteByQuery(QueryWrapper.create().where(UserToken::getUserId).eq(user.getId()));
        resetCodeCache.invalidate(cacheKey);
        return true;
    }

    /**
     * 校验访问令牌是否仍然有效
     *
     * @param token 访问令牌
     * @param userInfo 令牌中的用户信息
     * @return 是否有效
     */
    public boolean isAccessTokenValid(String token, String userInfo) {
        User user = getById(extractUserId(userInfo));
        if (user == null || user.getPasswordUpdateTime() == null) {
            return true;
        }
        LocalDateTime issuedAt = LocalDateTimeUtil.of(JwtUtil.getAllClaimsFromToken(token).getIssuedAt());
        return issuedAt != null && !issuedAt.isBefore(user.getPasswordUpdateTime());
    }

    /**
     * 用户登录
     *
     * @param data 登录参数
     * @return 访问令牌信息
     */
    public UserTokenVO login(UserLoginReq data) {
        User user = queryChain().eq(User::getUsername, data.getUsername()).one();
        if (user == null) {
            throw new BusinessException("用户名不存在！");
        }
        if (!passwordEncoder.matches(data.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误！");
        }
        String userInfo = user.getId() + SystemConstants.SEMICOLON + user.getUsername();
        UserTokenVO vo = JwtUtil.generateToken(userInfo);
        if (StrUtil.isBlank(data.getDeviceId())) {
            return vo;
        }
        LocalDateTime expireTime = LocalDateTime.now().plusDays(propertiesService.getTokenExpireDay());
        String refreshToken = JwtUtil.generateRefreshToken(userInfo, data.getDeviceId(), propertiesService.getTokenKey(), DateUtil.date(expireTime));
        vo.setRefreshToken(refreshToken);
        executor.execute(() -> disposalRefreshToken(user, data.getDeviceId(), refreshToken, expireTime));
        return vo;
    }

    private void disposalRefreshToken(User user, String deviceId, String refreshToken, LocalDateTime expireTime) {
        // 查询有没有相同设备的refreshToken
        Long tokenId = Optional.ofNullable(QueryChain.of(userTokenMapper)
                .select(UserToken::getId).eq(UserToken::getUserId, user.getId())
                .eq(UserToken::getDeviceId, deviceId).one()).map(UserToken::getId).orElse(null);
        if (tokenId == null) {
            userTokenMapper.insert(UserToken.builder().userId(user.getId()).deviceId(deviceId)
                    .refreshToken(refreshToken).expireTime(expireTime).build());
            return;
        }
        // 更新旧的refreshToken
        UpdateChain.of(userTokenMapper).set(UserToken::getDeviceId, deviceId)
                .set(UserToken::getRefreshToken, refreshToken).eq(UserToken::getId, tokenId).update();
        // 邮箱通知
        emailSendService.sendSystemEmail(getSystemEmailDTO(user, deviceId));
    }

    private SystemEmailDTO getSystemEmailDTO(User user, String deviceId) {
        SystemEmailDTO result = new SystemEmailDTO();
        result.setSubject("新设备登录账号通知");
        result.setTargetUser(user.getUsername());
        result.setTargetEmail(user.getEmail());
        result.setFileName("login_notification_email.html");
        Map<String, String> map = new HashMap<>();
        map.put("loginTime", DateUtil.now());
        map.put("userName", user.getUsername());
        map.put("deviceId", deviceId);
        result.setTemplateParams(map);
        return result;
    }

    private boolean sendResetCodeEmail(User user, String code) {
        SystemEmailDTO result = new SystemEmailDTO();
        result.setSubject("MyBill 找回密码验证码");
        result.setTargetUser(user.getUsername());
        result.setTargetEmail(user.getEmail());
        result.setFileName("reset_password_code.html");
        Map<String, String> map = new HashMap<>();
        map.put("email", user.getEmail());
        map.put("code", code);
        map.put("expireMinutes", String.valueOf(RESET_CODE_EXPIRE_MINUTES));
        result.setTemplateParams(map);
        return emailSendService.sendSystemEmailChecked(result);
    }

    private User getSingleUserByEmail(String email) {
        java.util.List<User> users = queryChain().eq(User::getEmail, email).list();
        if (users.size() == 1) {
            return users.getFirst();
        }
        if (users.size() > 1) {
            log.warn("邮箱:{} 对应多个账号，忽略找回密码请求", email);
        }
        return null;
    }

    private Long extractUserId(String userInfo) {
        if (StrUtil.isBlank(userInfo)) {
            return 0L;
        }
        String[] split = userInfo.split(SystemConstants.SEMICOLON);
        return Long.parseLong(split[0]);
    }

    private String getResetEmailCacheKey(String email) {
        return StrUtil.trim(email).toLowerCase(Locale.ROOT);
    }


    /**
     * 更新当前用户资料
     *
     * @param req 用户资料
     * @return 是否更新成功
     */
    public Boolean updateInfo(UserUpdateReq req) {
        Integer userId = UserUtil.getCurrentUserId();
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在！");
        }
        if (StrUtil.isNotBlank(req.getUsername())) {
            if (queryChain().eq(User::getUsername, req.getUsername()).ne(User::getId, userId).exists()) {
                throw new BusinessException("用户名【" + req.getUsername() + "】已存在，不支持修改！");
            }
        }
        validateUserInfoUpdate(user, req);
        return updateChain().eq(User::getId, userId)
                .set(User::getUsername, req.getUsername(), StrUtil.isNotBlank(req.getUsername()))
                .set(User::getAvatarUrl, req.getAvatarUrl(), StrUtil.isNotBlank(req.getAvatarUrl()))
                .set(User::getDescription, req.getDescription(), StrUtil.isNotBlank(req.getDescription()))
                .update();
    }

    static void validateUserInfoUpdate(User user, UserUpdateReq req) {
        if (req.getEmail() != null && !StrUtil.equals(req.getEmail(), user.getEmail())) {
            throw new BusinessException("邮箱暂不支持在此处修改！");
        }
    }

    /**
     * 刷新当前用户令牌
     *
     * @param req 刷新令牌参数
     * @return 新的令牌信息
     */
    @Transactional(rollbackFor = Exception.class)
    public UserTokenVO refreshToken(UserTokenFreshReq req) {
        UserInfo userInfo = UserUtil.getCurrentUserInfo().orElseThrow(() -> new BusinessException("用户信息不存在！"));
        UserToken tokenInfo = QueryChain.of(userTokenMapper).eq(UserToken::getUserId, userInfo.getId())
                .eq(UserToken::getRefreshToken, req.getRefreshToken())
                .eq(UserToken::getDeviceId, req.getDeviceId()).one();
        if (tokenInfo == null) {
            log.info("用户：{} refreshToken:{} 不存在", UserUtil.getCurrentUsername(), req.getRefreshToken());
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        if (tokenInfo.getExpireTime().isBefore(now)) {
            log.info("用户：{} refreshToken:{} 已过期", UserUtil.getCurrentUsername(), req.getRefreshToken());
            return null;
        }
        String userInfoStr = userInfo.getId() + SystemConstants.SEMICOLON + userInfo.getUsername();
        UserTokenVO userTokenVO = JwtUtil.generateToken(userInfoStr);
        long dayCount = LocalDateTimeUtil.between(now, tokenInfo.getExpireTime(), ChronoUnit.DAYS);
        int userRefreshTokenNeedFresh = 3;
        if (dayCount > userRefreshTokenNeedFresh) {
            return userTokenVO;
        }
        LocalDateTime expireTime = now.plusDays(propertiesService.getTokenExpireDay());
        String refreshToken = JwtUtil.generateRefreshToken(userInfoStr, req.getDeviceId(), propertiesService.getTokenKey(), DateUtil.date(expireTime));
        UpdateChain.of(userTokenMapper).set(UserToken::getRefreshToken, refreshToken).eq(UserToken::getId, tokenInfo.getId()).update();
        userTokenVO.setRefreshToken(refreshToken);
        return userTokenVO;
    }
}
