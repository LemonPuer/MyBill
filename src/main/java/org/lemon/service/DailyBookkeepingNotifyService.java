package org.lemon.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.lemon.entity.User;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DailyBookkeepingNotifyService {

    private final NotifyPreferenceService notifyPreferenceService;

    private final NotifyRecordService notifyRecordService;

    public void generatePendingRecords(LocalDateTime now, String actionUrl) {
        LocalDate bizDate = now.toLocalDate();
        List<User> users = notifyPreferenceService.queryDailyBookkeepingUsers(now.getHour());
        users.stream()
                .filter(user -> StrUtil.isNotBlank(user.getEmail()))
                .forEach(user -> notifyRecordService.createDailyBookkeepingPendingRecord(user, bizDate, now,
                        buildPayload(user, bizDate, actionUrl)));
    }

    Map<String, Object> buildPayload(User user, LocalDate bizDate, String actionUrl) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userName", user.getUsername());
        payload.put("todayLabel", DateUtil.formatDate(Date.valueOf(bizDate)));
        payload.put("encouragement", "花了什么、赚了什么，顺手记一笔，月底复盘更轻松。");
        payload.put("actionUrl", actionUrl);
        return payload;
    }
}
