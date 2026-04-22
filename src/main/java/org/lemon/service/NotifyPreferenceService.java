package org.lemon.service;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.lemon.entity.NotifyPreference;
import org.lemon.entity.User;
import org.lemon.entity.exception.BusinessException;
import org.lemon.entity.req.NotifyPreferenceUpdateReq;
import org.lemon.entity.resp.NotifyPreferenceVO;
import org.lemon.enumeration.NotifyBizTypeEnum;
import org.lemon.enumeration.NotifyChannelEnum;
import org.lemon.mapper.NotifyPreferenceMapper;
import org.lemon.utils.UserUtil;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 通知偏好表 服务层实现。
 *
 * @author Lemon
 * @since 2026-04-22
 */
@Service
@AllArgsConstructor
public class NotifyPreferenceService extends ServiceImpl<NotifyPreferenceMapper, NotifyPreference> {

    private final UserService userService;

    public List<NotifyPreference> queryActiveEmailPreferences(String bizType, Integer sendHour) {
        return queryChain()
                .eq(NotifyPreference::getBizType, bizType)
                .eq(NotifyPreference::getChannel, NotifyChannelEnum.EMAIL.name())
                .eq(NotifyPreference::getEnabled, Boolean.TRUE)
                .eq(NotifyPreference::getSendHour, sendHour)
                .list();
    }

    public NotifyPreferenceVO getCurrentUserNotifyPreference() {
        Integer userId = UserUtil.getCurrentUserId();
        NotifyPreference daily = getByUserIdAndBizType(userId, NotifyBizTypeEnum.DAILY_BOOKKEEPING.name());
        NotifyPreference monthly = getByUserIdAndBizType(userId, NotifyBizTypeEnum.MONTHLY_SUMMARY.name());
        Integer sendHour = daily != null ? daily.getSendHour() : (monthly == null ? null : monthly.getSendHour());
        return NotifyPreferenceVO.builder()
                .emailReminderEnabled(daily != null && Boolean.TRUE.equals(daily.getEnabled()))
                .monthlySummaryEnabled(monthly != null && Boolean.TRUE.equals(monthly.getEnabled()))
                .reminderSendHour(sendHour)
                .build();
    }

    public Boolean updateCurrentUserNotifyPreference(NotifyPreferenceUpdateReq req) {
        validateNotifyPreference(req);
        Integer userId = UserUtil.getCurrentUserId();
        upsertEmailPreference(userId, NotifyBizTypeEnum.DAILY_BOOKKEEPING.name(), req.getEmailReminderEnabled(), req.getReminderSendHour());
        upsertEmailPreference(userId, NotifyBizTypeEnum.MONTHLY_SUMMARY.name(), req.getMonthlySummaryEnabled(), req.getReminderSendHour());
        return true;
    }

    public List<User> queryDailyBookkeepingUsers(Integer sendHour) {
        List<NotifyPreference> preferences = queryActiveEmailPreferences("DAILY_BOOKKEEPING", sendHour);
        if (preferences.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Integer> userIds = preferences.stream().map(NotifyPreference::getUserId).collect(Collectors.toSet());
        return userService.listByIds(userIds).stream()
                .filter(user -> user.getId() != null)
                .toList();
    }

    public List<User> queryMonthlySummaryUsers() {
        List<NotifyPreference> preferences = queryChain()
                .eq(NotifyPreference::getBizType, NotifyBizTypeEnum.MONTHLY_SUMMARY.name())
                .eq(NotifyPreference::getChannel, NotifyChannelEnum.EMAIL.name())
                .eq(NotifyPreference::getEnabled, Boolean.TRUE)
                .list();
        if (preferences.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Integer> userIds = preferences.stream().map(NotifyPreference::getUserId).collect(Collectors.toSet());
        return userService.listByIds(userIds).stream()
                .filter(user -> user.getId() != null)
                .filter(user -> StrUtil.isNotBlank(user.getEmail()))
                .toList();
    }

    private void validateNotifyPreference(NotifyPreferenceUpdateReq req) {
        if (req.getReminderSendHour() != null && (req.getReminderSendHour() < 0 || req.getReminderSendHour() > 23)) {
            throw new BusinessException("提醒发送时间仅支持 0~23 点！");
        }
    }

    private void upsertEmailPreference(Integer userId, String bizType, Boolean enabled, Integer sendHour) {
        if (enabled == null && sendHour == null) {
            return;
        }
        NotifyPreference existing = getByUserIdAndBizType(userId, bizType);
        if (existing == null) {
            save(NotifyPreference.builder()
                    .userId(userId)
                    .bizType(bizType)
                    .channel(NotifyChannelEnum.EMAIL.name())
                    .enabled(Boolean.TRUE.equals(enabled))
                    .sendHour(sendHour)
                    .build());
            return;
        }
        updateChain().eq(NotifyPreference::getId, existing.getId())
                .set(NotifyPreference::getEnabled, enabled, enabled != null)
                .set(NotifyPreference::getSendHour, sendHour, sendHour != null)
                .update();
    }

    private NotifyPreference getByUserIdAndBizType(Integer userId, String bizType) {
        return queryChain()
                .eq(NotifyPreference::getUserId, userId)
                .eq(NotifyPreference::getBizType, bizType)
                .eq(NotifyPreference::getChannel, NotifyChannelEnum.EMAIL.name())
                .one();
    }
}
