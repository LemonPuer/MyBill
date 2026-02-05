package org.lemon.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.User;
import org.lemon.entity.UserInfo;
import org.lemon.entity.UserToken;
import org.lemon.entity.dto.SystemEmailDTO;
import org.lemon.entity.exception.BusinessException;
import org.lemon.entity.req.UserLoginReq;
import org.lemon.entity.req.UserRegisterReq;
import org.lemon.entity.req.UserTokenFreshReq;
import org.lemon.entity.req.UserUpdateReq;
import org.lemon.entity.resp.UserTokenVO;
import org.lemon.mapper.UserMapper;
import org.lemon.mapper.UserTokenMapper;
import org.lemon.utils.JwtUtil;
import org.lemon.utils.UserUtil;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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


    private final String USER_INFO_INTERVAL = ";";

    public boolean register(UserRegisterReq req) {
        if (queryChain().eq(User::getUsername, req.getUsername()).exists()) {
            throw new BusinessException("用户名【" + req.getUsername() + "】已存在！");
        }
        User result = User.builder().username(req.getUsername()).email(req.getEmail()).build();
        String encode = passwordEncoder.encode(req.getPassword());
        result.setPassword(encode);
        return save(result);
    }

    public UserTokenVO login(UserLoginReq data) {
        User user = queryChain().eq(User::getUsername, data.getUsername()).one();
        if (user == null) {
            throw new BusinessException("用户名不存在！");
        }
        if (!passwordEncoder.matches(data.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误！");
        }
        String userInfo = user.getId() + USER_INFO_INTERVAL + user.getUsername();
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


    public Boolean updateInfo(UserUpdateReq req) {
        Integer userId = UserUtil.getCurrentUserId();
        if (StrUtil.isNotBlank(req.getUsername())) {
            if (queryChain().eq(User::getUsername, req.getUsername()).ne(User::getId, userId).exists()) {
                throw new BusinessException("用户名【" + req.getUsername() + "】已存在，不支持修改！");
            }
        }
        return updateChain().eq(User::getId, userId)
                .set(User::getUsername, req.getUsername(), StrUtil.isNotBlank(req.getUsername()))
                .set(User::getAvatarUrl, req.getAvatarUrl(), StrUtil.isNotBlank(req.getAvatarUrl()))
                .set(User::getDescription, req.getDescription(), StrUtil.isNotBlank(req.getDescription()))
                .update();
    }

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
        String userInfoStr = userInfo.getId() + USER_INFO_INTERVAL + userInfo.getUsername();
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
