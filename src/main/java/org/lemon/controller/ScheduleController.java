package org.lemon.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.MonthTotalRecord;
import org.lemon.entity.NotifyRecord;
import org.lemon.entity.User;
import org.lemon.entity.common.ApiReq;
import org.lemon.entity.common.IdReq;
import org.lemon.entity.dto.NotifyRenderDTO;
import org.lemon.entity.dto.SystemEmailDTO;
import org.lemon.service.MonthTotalRecordService;
import org.lemon.service.EmailSendService;
import org.lemon.service.NotifyPreferenceService;
import org.lemon.service.NotifyRecordService;
import org.lemon.service.NotifyTemplateService;
import org.lemon.service.PropertiesService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/18 14:12:50
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("schedule")
public class ScheduleController {

    private final MonthTotalRecordService monthTotalRecordService;
    private final PropertiesService propertiesService;
    private final NotifyPreferenceService notifyPreferenceService;
    private final NotifyRecordService notifyRecordService;
    private final NotifyTemplateService notifyTemplateService;
    private final EmailSendService emailSendService;

    @Async
    @Scheduled(cron = "0 0 3 1 * ? ")
    public void monthStatistics() {
        monthTotalRecordService.monthStatistics(null);
    }

    @Async
    @Scheduled(cron = "0 0 * * * ?")
    public void generateDailyBookkeepingNotifyRecords() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate bizDate = now.toLocalDate();
        notifyPreferenceService.queryDailyBookkeepingUsers(now.getHour()).forEach(user ->
                notifyRecordService.createDailyBookkeepingPendingRecord(user, bizDate, now,
                        buildDailyBookkeepingPayload(user, bizDate)));
    }

    @Async
    @Scheduled(cron = "0 0 9 1 * ?")
    public void generateMonthlySummaryNotifyRecords() {
        LocalDateTime now = LocalDateTime.now();
        YearMonth previousMonth = YearMonth.from(now).minusMonths(1);
        List<User> users = notifyPreferenceService.queryMonthlySummaryUsers();
        if (users.isEmpty()) {
            return;
        }
        Set<Integer> userIds = users.stream().map(User::getId).collect(Collectors.toSet());
        Map<Integer, MonthTotalRecord> summaryMap = monthTotalRecordService.queryPreviousMonthSummaryByUserIds(userIds);
        users.forEach(user -> {
            MonthTotalRecord summary = summaryMap.get(user.getId());
            if (summary == null) {
                return;
            }
            notifyRecordService.createMonthlySummaryPendingRecord(user, summary.getMonth(), now,
                    buildMonthlySummaryPayload(user, summary, previousMonth));
        });
    }

    @Async
    @Scheduled(cron = "0 * * * * ?")
    public void sendDailyBookkeepingNotifyRecords() {
        List<NotifyRecord> records = notifyRecordService.queryDueSendableRecords(LocalDateTime.now());
        records.forEach(this::sendNotifyRecord);
    }

    /**
     * 统计月度账单
     *
     * @param req
     */
    @RequestMapping("monthStatistics")
    public void monthStatistics(@Validated @RequestBody ApiReq<IdReq> req) {
        monthTotalRecordService.monthStatistics(req.getData().getId());
    }

    private Map<String, Object> buildDailyBookkeepingPayload(User user, LocalDate bizDate) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userName", user.getUsername());
        payload.put("todayLabel", DateUtil.formatDate(java.sql.Date.valueOf(bizDate)));
        payload.put("encouragement", "花了什么、赚了什么，顺手记一笔，月底复盘更轻松。");
        payload.put("actionUrl", buildBillsActionUrl());
        return payload;
    }

    private String buildBillsActionUrl() {
        String baseUrl = StrUtil.trim(propertiesService.getAppWebBaseUrl());
        if (StrUtil.isBlank(baseUrl)) {
            return "";
        }
        return StrUtil.removeSuffix(baseUrl, "/") + "/bills";
    }

    private Map<String, Object> buildMonthlySummaryPayload(User user, MonthTotalRecord summary, YearMonth previousMonth) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userName", user.getUsername());
        payload.put("monthLabel", summary.getMonth() == null
                ? previousMonth.getYear() + "-" + String.format("%02d", previousMonth.getMonthValue())
                : summary.getMonth());
        payload.put("totalIncome", NotifyRecordService.formatAmount(summary.getTotalIncome()));
        payload.put("totalExpense", NotifyRecordService.formatAmount(summary.getTotalExpense()));
        payload.put("totalBalance", NotifyRecordService.formatAmount(summary.getTotalBalance()));
        payload.put("actionUrl", buildDashboardActionUrl());
        return payload;
    }

    private String buildDashboardActionUrl() {
        String baseUrl = StrUtil.trim(propertiesService.getAppWebBaseUrl());
        if (StrUtil.isBlank(baseUrl)) {
            return "";
        }
        return StrUtil.removeSuffix(baseUrl, "/") + "/dashboard";
    }

    private void sendNotifyRecord(NotifyRecord record) {
        String subject = null;
        String content = null;
        try {
            Map<String, Object> payload = notifyRecordService.parsePayload(record.getPayloadJson());
            NotifyRenderDTO renderDTO = notifyTemplateService.renderByTemplateCode(record.getTemplateCode(), payload);
            subject = renderDTO.getSubject();
            content = renderDTO.getContent();
            SystemEmailDTO emailDTO = new SystemEmailDTO();
            emailDTO.setTargetUser((String) payload.get("userName"));
            emailDTO.setTargetEmail(record.getTarget());
            emailDTO.setSubject(subject);
            emailDTO.setContent(content);
            boolean sent = emailSendService.sendRenderedSystemEmailChecked(emailDTO);
            if (sent) {
                notifyRecordService.markSent(record.getId(), subject, content);
                return;
            }
            notifyRecordService.markFailed(record, subject, content, "邮件发送失败");
        } catch (Exception e) {
            log.error("发送通知失败，recordId={}", record.getId(), e);
            notifyRecordService.markFailed(record, subject, content, e.getMessage());
        }
    }
}

