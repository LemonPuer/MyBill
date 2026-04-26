package org.lemon.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lemon.entity.NotifyRecord;
import org.lemon.entity.dto.NotifyRenderDTO;
import org.lemon.entity.dto.SystemEmailDTO;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifyScheduleServiceTest {

    @Mock
    private DailyBookkeepingNotifyService dailyBookkeepingNotifyService;

    @Mock
    private MonthlySummaryNotifyService monthlySummaryNotifyService;

    @Mock
    private NotifyRecordService notifyRecordService;

    @Mock
    private NotifyTemplateService notifyTemplateService;

    @Mock
    private EmailSendService emailSendService;

    @Mock
    private PropertiesService propertiesService;

    @InjectMocks
    private NotifyScheduleService notifyScheduleService;

    @Test
    void sendDueNotifyRecordsMarksRecordSentWhenRenderedEmailSendSucceeds() {
        NotifyRecord record = NotifyRecord.builder()
                .id(99L)
                .templateCode("MONTHLY_SUMMARY")
                .target("amy@example.com")
                .payloadJson("{\"monthLabel\":\"2026-03\",\"userName\":\"amy\"}")
                .bizDate(LocalDate.of(2026, 3, 1))
                .scheduledTime(LocalDateTime.of(2026, 4, 1, 9, 0))
                .build();
        NotifyRenderDTO renderDTO = NotifyRenderDTO.builder()
                .subject("Monthly summary")
                .content("Rendered content")
                .build();
        Map<String, Object> payload = Map.of("monthLabel", "2026-03", "userName", "amy");
        when(notifyRecordService.queryDueSendableRecords(any())).thenReturn(List.of(record));
        when(notifyRecordService.parsePayload(record.getPayloadJson())).thenReturn(payload);
        when(notifyTemplateService.renderByTemplateCode(eq("MONTHLY_SUMMARY"), any())).thenReturn(renderDTO);
        when(emailSendService.sendRenderedSystemEmailChecked(any())).thenReturn(true);

        notifyScheduleService.sendDueNotifyRecords();

        ArgumentCaptor<SystemEmailDTO> emailCaptor = ArgumentCaptor.forClass(SystemEmailDTO.class);
        verify(emailSendService).sendRenderedSystemEmailChecked(emailCaptor.capture());
        SystemEmailDTO emailDTO = emailCaptor.getValue();
        assertNotNull(emailDTO);
        assertEquals("amy", emailDTO.getTargetUser());
        assertEquals("amy@example.com", emailDTO.getTargetEmail());
        assertEquals("Monthly summary", emailDTO.getSubject());
        assertEquals("Rendered content", emailDTO.getContent());
        verify(notifyRecordService).markSent(99L, "Monthly summary", "Rendered content");
        verify(notifyRecordService, never()).markFailed(any(), any(), any(), any());
    }

    @Test
    void buildActionUrlReturnsEmptyStringWhenBaseUrlBlank() {
        lenient().when(propertiesService.getAppWebBaseUrl()).thenReturn("   ");

        String result = notifyScheduleService.buildActionUrl("/records");

        assertEquals("", result);
    }

    @Test
    void buildActionUrlAppendsPathToTrimmedBaseUrl() {
        lenient().when(propertiesService.getAppWebBaseUrl()).thenReturn(" https://app.example.com/base/ ");

        String result = notifyScheduleService.buildActionUrl("/records");

        assertEquals("https://app.example.com/base/records", result);
    }
}
