package org.lemon.service;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.NotifyRecord;
import org.lemon.entity.dto.NotifyRenderDTO;
import org.lemon.entity.dto.SystemEmailDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyScheduleService {

    private static final String BILLS_PATH = "/bills";

    private static final String DASHBOARD_PATH = "/dashboard";

    private final DailyBookkeepingNotifyService dailyBookkeepingNotifyService;

    private final MonthlySummaryNotifyService monthlySummaryNotifyService;

    private final NotifyRecordService notifyRecordService;

    private final NotifyTemplateService notifyTemplateService;

    private final EmailSendService emailSendService;

    private final PropertiesService propertiesService;

    public void generateDailyBookkeepingNotifyRecords() {
        dailyBookkeepingNotifyService.generatePendingRecords(LocalDateTime.now(), buildActionUrl(BILLS_PATH));
    }

    public void generateMonthlySummaryNotifyRecords() {
        monthlySummaryNotifyService.generatePendingRecords(LocalDateTime.now(), buildActionUrl(DASHBOARD_PATH));
    }

    public void sendDueNotifyRecords() {
        List<NotifyRecord> records = notifyRecordService.queryDueSendableRecords(LocalDateTime.now());
        records.forEach(this::sendNotifyRecord);
    }

    String buildActionUrl(String path) {
        String baseUrl = StrUtil.trim(propertiesService.getAppWebBaseUrl());
        if (StrUtil.isBlank(baseUrl)) {
            return "";
        }
        String normalizedBaseUrl = StrUtil.removeSuffix(baseUrl, "/");
        if (StrUtil.isBlank(path)) {
            return normalizedBaseUrl;
        }
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return normalizedBaseUrl + normalizedPath;
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
            emailDTO.setTargetUser(Objects.toString(payload.get("userName"), null));
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
