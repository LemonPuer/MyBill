# Notify Refactor And Comment Cleanup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor notification scheduling out of `ScheduleController` into layered services and clean up missing or placeholder comments in files touched by the unpushed branch.

**Architecture:** Keep `ScheduleController` as a thin entrypoint, add one orchestration service plus two business-preparation services, and reuse the existing preference, record, template, and mail services as lower-level dependencies. Limit comment cleanup to files changed by the unpushed branch and keep business behavior stable while moving logic.

**Tech Stack:** Java 21, Spring Boot 3, Spring Scheduling, MyBatis-Flex, JUnit 5, Mockito

---

### Task 1: Add Unit Tests For The New Notification Services

**Files:**
- Create: `src/test/java/org/lemon/service/NotifyScheduleServiceTest.java`
- Create: `src/test/java/org/lemon/service/DailyBookkeepingNotifyServiceTest.java`
- Create: `src/test/java/org/lemon/service/MonthlySummaryNotifyServiceTest.java`

- [ ] **Step 1: Write the failing tests for orchestration and payload behavior**

```java
package org.lemon.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lemon.entity.NotifyRecord;
import org.lemon.entity.dto.NotifyRenderDTO;
import org.lemon.entity.dto.SystemEmailDTO;
import org.lemon.enumeration.NotifyRecordStatusEnum;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    void sendDueNotifyRecords_marksSentWhenEmailSucceeds() {
        NotifyRecord record = NotifyRecord.builder()
                .id(1L)
                .templateCode("MONTHLY_SUMMARY")
                .target("user@test.com")
                .payloadJson("{}")
                .status(NotifyRecordStatusEnum.PENDING.getCode())
                .scheduledTime(LocalDateTime.now())
                .build();
        when(notifyRecordService.queryDueSendableRecords(any())).thenReturn(List.of(record));
        when(notifyRecordService.parsePayload("{}")).thenReturn(Map.of("userName", "tester"));
        when(notifyTemplateService.renderByTemplateCode(eq("MONTHLY_SUMMARY"), any()))
                .thenReturn(NotifyRenderDTO.builder().subject("subject").content("content").build());
        when(emailSendService.sendRenderedSystemEmailChecked(any())).thenReturn(true);

        notifyScheduleService.sendDueNotifyRecords();

        ArgumentCaptor<SystemEmailDTO> captor = ArgumentCaptor.forClass(SystemEmailDTO.class);
        verify(emailSendService).sendRenderedSystemEmailChecked(captor.capture());
        assertEquals("tester", captor.getValue().getTargetUser());
        assertEquals("user@test.com", captor.getValue().getTargetEmail());
        assertEquals("subject", captor.getValue().getSubject());
        assertEquals("content", captor.getValue().getContent());
        verify(notifyRecordService).markSent(1L, "subject", "content");
        verify(notifyRecordService, never()).markFailed(any(), any(), any(), any());
    }

    @Test
    void buildActionUrl_returnsEmptyStringWhenBaseUrlBlank() {
        when(propertiesService.getAppWebBaseUrl()).thenReturn("  ");

        assertEquals("", notifyScheduleService.buildActionUrl("/dashboard"));
    }

    @Test
    void buildActionUrl_appendsPathToTrimmedBaseUrl() {
        when(propertiesService.getAppWebBaseUrl()).thenReturn("https://mybill.app/");

        assertEquals("https://mybill.app/dashboard", notifyScheduleService.buildActionUrl("/dashboard"));
    }
}
```

```java
package org.lemon.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lemon.entity.User;
import org.lemon.enumeration.NotifyBizTypeEnum;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyBookkeepingNotifyServiceTest {

    @Mock
    private NotifyPreferenceService notifyPreferenceService;
    @Mock
    private NotifyRecordService notifyRecordService;

    @InjectMocks
    private DailyBookkeepingNotifyService dailyBookkeepingNotifyService;

    @Test
    void generatePendingRecords_createsRecordForEachMatchedUser() {
        LocalDateTime now = LocalDateTime.of(2026, 4, 26, 9, 0);
        User user = User.builder().id(1).username("tester").email("user@test.com").build();
        when(notifyPreferenceService.queryDailyBookkeepingUsers(9)).thenReturn(List.of(user));

        dailyBookkeepingNotifyService.generatePendingRecords(now, "https://mybill.app/bills");

        verify(notifyRecordService).createDailyBookkeepingPendingRecord(eq(user), eq(LocalDate.of(2026, 4, 26)), eq(now), anyMap());
    }

    @Test
    void buildPayload_containsExpectedTemplateFields() {
        User user = User.builder().id(1).username("tester").email("user@test.com").build();

        Map<String, Object> payload = dailyBookkeepingNotifyService.buildPayload(user, LocalDate.of(2026, 4, 26), "https://mybill.app/bills");

        assertEquals("tester", payload.get("userName"));
        assertEquals("https://mybill.app/bills", payload.get("actionUrl"));
        assertEquals("花了什么、赚了什么，顺手记一笔，月底复盘更轻松。", payload.get("encouragement"));
    }
}
```

```java
package org.lemon.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lemon.entity.MonthTotalRecord;
import org.lemon.entity.User;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlySummaryNotifyServiceTest {

    @Mock
    private NotifyPreferenceService notifyPreferenceService;
    @Mock
    private MonthTotalRecordService monthTotalRecordService;
    @Mock
    private NotifyRecordService notifyRecordService;

    @InjectMocks
    private MonthlySummaryNotifyService monthlySummaryNotifyService;

    @Test
    void generatePendingRecords_createsRecordWhenSummaryExists() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 1, 9, 0);
        User user = User.builder().id(1).username("tester").email("user@test.com").build();
        MonthTotalRecord summary = new MonthTotalRecord();
        summary.setUserId(1);
        summary.setMonth("2026-04");
        summary.setTotalIncome(new BigDecimal("200"));
        summary.setTotalExpense(new BigDecimal("80"));
        summary.setTotalBalance(new BigDecimal("120"));
        when(notifyPreferenceService.queryMonthlySummaryUsers()).thenReturn(List.of(user));
        when(monthTotalRecordService.queryPreviousMonthSummaryByUserIds(Set.of(1))).thenReturn(Map.of(1, summary));

        monthlySummaryNotifyService.generatePendingRecords(now, "https://mybill.app/dashboard");

        verify(notifyRecordService).createMonthlySummaryPendingRecord(eq(user), eq("2026-04"), eq(now), anyMap());
    }

    @Test
    void buildPayload_formatsSummaryFields() {
        User user = User.builder().id(1).username("tester").email("user@test.com").build();
        MonthTotalRecord summary = new MonthTotalRecord();
        summary.setMonth("2026-04");
        summary.setTotalIncome(new BigDecimal("200.00"));
        summary.setTotalExpense(new BigDecimal("80.00"));
        summary.setTotalBalance(new BigDecimal("120.00"));

        Map<String, Object> payload = monthlySummaryNotifyService.buildPayload(user, summary, "https://mybill.app/dashboard");

        assertEquals("tester", payload.get("userName"));
        assertEquals("2026-04", payload.get("monthLabel"));
        assertEquals("200", payload.get("totalIncome"));
        assertEquals("80", payload.get("totalExpense"));
        assertEquals("120", payload.get("totalBalance"));
        assertEquals("https://mybill.app/dashboard", payload.get("actionUrl"));
    }
}
```

- [ ] **Step 2: Run the new test classes and verify they fail**

Run: `mvn test -Dtest=NotifyScheduleServiceTest,DailyBookkeepingNotifyServiceTest,MonthlySummaryNotifyServiceTest`

Expected: FAIL with compile errors such as `cannot find symbol class NotifyScheduleService` and `cannot find symbol class DailyBookkeepingNotifyService`.

- [ ] **Step 3: Create minimal service skeletons so the tests compile**

```java
package org.lemon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotifyScheduleService {

    private final DailyBookkeepingNotifyService dailyBookkeepingNotifyService;
    private final MonthlySummaryNotifyService monthlySummaryNotifyService;
    private final NotifyRecordService notifyRecordService;
    private final NotifyTemplateService notifyTemplateService;
    private final EmailSendService emailSendService;
    private final PropertiesService propertiesService;

    public void generateDailyBookkeepingNotifyRecords() {
    }

    public void generateMonthlySummaryNotifyRecords() {
    }

    public void sendDueNotifyRecords() {
    }

    String buildActionUrl(String path) {
        return "";
    }
}
```

```java
package org.lemon.service;

import lombok.RequiredArgsConstructor;
import org.lemon.entity.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DailyBookkeepingNotifyService {

    private final NotifyPreferenceService notifyPreferenceService;
    private final NotifyRecordService notifyRecordService;

    public void generatePendingRecords(LocalDateTime now, String actionUrl) {
    }

    Map<String, Object> buildPayload(User user, LocalDate bizDate, String actionUrl) {
        return Collections.emptyMap();
    }
}
```

```java
package org.lemon.service;

import lombok.RequiredArgsConstructor;
import org.lemon.entity.MonthTotalRecord;
import org.lemon.entity.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MonthlySummaryNotifyService {

    private final NotifyPreferenceService notifyPreferenceService;
    private final MonthTotalRecordService monthTotalRecordService;
    private final NotifyRecordService notifyRecordService;

    public void generatePendingRecords(LocalDateTime now, String actionUrl) {
    }

    Map<String, Object> buildPayload(User user, MonthTotalRecord summary, String actionUrl) {
        return Collections.emptyMap();
    }
}
```

- [ ] **Step 4: Run the tests again to get behavior failures instead of missing-class failures**

Run: `mvn test -Dtest=NotifyScheduleServiceTest,DailyBookkeepingNotifyServiceTest,MonthlySummaryNotifyServiceTest`

Expected: FAIL on assertions and missing Mockito interactions, proving the test shape is correct.

### Task 2: Implement The New Notification Services

**Files:**
- Modify: `src/main/java/org/lemon/service/NotifyScheduleService.java`
- Modify: `src/main/java/org/lemon/service/DailyBookkeepingNotifyService.java`
- Modify: `src/main/java/org/lemon/service/MonthlySummaryNotifyService.java`
- Modify: `src/main/java/org/lemon/service/NotifyRecordService.java`
- Modify: `src/main/java/org/lemon/entity/dto/NotifyRenderDTO.java`
- Modify: `src/main/java/org/lemon/entity/dto/SystemEmailDTO.java`

- [ ] **Step 1: Implement the daily preparation service**

```java
package org.lemon.service;

import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import org.lemon.entity.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DailyBookkeepingNotifyService {

    private final NotifyPreferenceService notifyPreferenceService;
    private final NotifyRecordService notifyRecordService;

    public void generatePendingRecords(LocalDateTime now, String actionUrl) {
        LocalDate bizDate = now.toLocalDate();
        notifyPreferenceService.queryDailyBookkeepingUsers(now.getHour()).forEach(user ->
                notifyRecordService.createDailyBookkeepingPendingRecord(user, bizDate, now, buildPayload(user, bizDate, actionUrl)));
    }

    Map<String, Object> buildPayload(User user, LocalDate bizDate, String actionUrl) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userName", user.getUsername());
        payload.put("todayLabel", DateUtil.formatDate(java.sql.Date.valueOf(bizDate)));
        payload.put("encouragement", "花了什么、赚了什么，顺手记一笔，月底复盘更轻松。");
        payload.put("actionUrl", actionUrl);
        return payload;
    }
}
```

- [ ] **Step 2: Implement the monthly summary preparation service**

```java
package org.lemon.service;

import lombok.RequiredArgsConstructor;
import org.lemon.entity.MonthTotalRecord;
import org.lemon.entity.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonthlySummaryNotifyService {

    private final NotifyPreferenceService notifyPreferenceService;
    private final MonthTotalRecordService monthTotalRecordService;
    private final NotifyRecordService notifyRecordService;

    public void generatePendingRecords(LocalDateTime now, String actionUrl) {
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
            notifyRecordService.createMonthlySummaryPendingRecord(user, summary.getMonth(), now, buildPayload(user, summary, actionUrl));
        });
    }

    Map<String, Object> buildPayload(User user, MonthTotalRecord summary, String actionUrl) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userName", user.getUsername());
        payload.put("monthLabel", summary.getMonth());
        payload.put("totalIncome", NotifyRecordService.formatAmount(summary.getTotalIncome()));
        payload.put("totalExpense", NotifyRecordService.formatAmount(summary.getTotalExpense()));
        payload.put("totalBalance", NotifyRecordService.formatAmount(summary.getTotalBalance()));
        payload.put("actionUrl", actionUrl);
        return payload;
    }
}
```

- [ ] **Step 3: Implement orchestration and the send flow**

```java
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

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyScheduleService {

    private final DailyBookkeepingNotifyService dailyBookkeepingNotifyService;
    private final MonthlySummaryNotifyService monthlySummaryNotifyService;
    private final NotifyRecordService notifyRecordService;
    private final NotifyTemplateService notifyTemplateService;
    private final EmailSendService emailSendService;
    private final PropertiesService propertiesService;

    public void generateDailyBookkeepingNotifyRecords() {
        dailyBookkeepingNotifyService.generatePendingRecords(LocalDateTime.now(), buildActionUrl("/bills"));
    }

    public void generateMonthlySummaryNotifyRecords() {
        monthlySummaryNotifyService.generatePendingRecords(LocalDateTime.now(), buildActionUrl("/dashboard"));
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
        return StrUtil.removeSuffix(baseUrl, "/") + path;
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
```

- [ ] **Step 4: Add or refine comments on DTOs touched by the refactor**

```java
/**
 * 通知模板渲染结果
 */
public class NotifyRenderDTO {

    /**
     * 渲染后的邮件主题
     */
    private String subject;

    /**
     * 渲染后的邮件正文
     */
    private String content;
}
```

```java
/**
 * 系统邮件发送参数
 */
public class SystemEmailDTO {

    /**
     * 收件人名称
     */
    private String targetUser;

    /**
     * 收件人邮箱
     */
    private String targetEmail;

    /**
     * 邮件主题
     */
    private String subject;

    /**
     * 基于文件模板发送时使用的模板名
     */
    private String fileName;

    /**
     * 已渲染的邮件正文
     */
    private String content;

    /**
     * 文件模板占位参数
     */
    private Map<String, String> templateParams;
}
```

- [ ] **Step 5: Run the three test classes and make them pass**

Run: `mvn test -Dtest=NotifyScheduleServiceTest,DailyBookkeepingNotifyServiceTest,MonthlySummaryNotifyServiceTest`

Expected: PASS with `Tests run: 7, Failures: 0, Errors: 0`.

### Task 3: Slim Down ScheduleController And Wire The New Services

**Files:**
- Modify: `src/main/java/org/lemon/controller/ScheduleController.java`

- [ ] **Step 1: Replace controller dependencies with the thin-entrypoint dependencies**

```java
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("schedule")
public class ScheduleController {

    private final MonthTotalRecordService monthTotalRecordService;
    private final NotifyScheduleService notifyScheduleService;
```

- [ ] **Step 2: Replace direct business logic calls with orchestration service calls**

```java
@Async
@Scheduled(cron = "0 0 * * * ?")
public void generateDailyBookkeepingNotifyRecords() {
    notifyScheduleService.generateDailyBookkeepingNotifyRecords();
}

@Async
@Scheduled(cron = "0 0 9 1 * ?")
public void generateMonthlySummaryNotifyRecords() {
    notifyScheduleService.generateMonthlySummaryNotifyRecords();
}

@Async
@Scheduled(cron = "0 * * * * ?")
public void sendDailyBookkeepingNotifyRecords() {
    notifyScheduleService.sendDueNotifyRecords();
}
```

- [ ] **Step 3: Delete the private payload and sending helpers from the controller**

```java
// Remove these methods from ScheduleController:
// - buildDailyBookkeepingPayload(...)
// - buildBillsActionUrl()
// - buildMonthlySummaryPayload(...)
// - buildDashboardActionUrl()
// - sendNotifyRecord(...)
```

- [ ] **Step 4: Add controller comments that match the codebase style**

```java
/**
 * 调度任务 控制层。
 *
 * @author Lemon
 * @since 2025-05-18
 */
public class ScheduleController {

    /**
     * 生成每日记账提醒记录。
     */
    @Async
    @Scheduled(cron = "0 0 * * * ?")
    public void generateDailyBookkeepingNotifyRecords() {
        notifyScheduleService.generateDailyBookkeepingNotifyRecords();
    }
}
```

- [ ] **Step 5: Run focused tests plus a compile check**

Run: `mvn test -Dtest=NotifyScheduleServiceTest,DailyBookkeepingNotifyServiceTest,MonthlySummaryNotifyServiceTest`

Expected: PASS.

Run: `mvn -q -DskipTests compile`

Expected: BUILD SUCCESS.

### Task 4: Clean Up Comments In The Remaining Unpushed Files

**Files:**
- Modify: `src/main/java/org/lemon/config/SecurityConfig.java`
- Modify: `src/main/java/org/lemon/config/filter/JwtAuthenticationFilter.java`
- Modify: `src/main/java/org/lemon/controller/AppController.java`
- Modify: `src/main/java/org/lemon/controller/UserController.java`
- Modify: `src/main/java/org/lemon/service/EmailSendService.java`
- Modify: `src/main/java/org/lemon/service/UserService.java`
- Modify: `src/main/java/org/lemon/entity/req/FinanceTransactionsQueryReq.java`
- Modify: `src/main/java/org/lemon/entity/req/UserUpdateReq.java`
- Modify: `src/main/java/org/lemon/entity/req/UserSendResetCodeReq.java`
- Modify: `src/main/java/org/lemon/entity/req/UserResetPasswordReq.java`
- Modify: `src/main/java/org/lemon/entity/resp/BudgetInfoVO.java`
- Modify: `src/main/java/org/lemon/entity/resp/ConsumptionStatisticsVO.java`
- Modify: `src/main/java/org/lemon/entity/resp/NotifyPreferenceVO.java`
- Modify: `src/main/java/org/lemon/entity/dto/UserResetCodeDTO.java`
- Modify: `src/main/java/org/lemon/entity/dto/SystemEmailDTO.java`
- Modify: `src/main/java/org/lemon/entity/NotifyPreference.java`
- Modify: `src/main/java/org/lemon/entity/NotifyRecord.java`
- Modify: `src/main/java/org/lemon/entity/NotifyTemplate.java`
- Modify: `src/main/java/org/lemon/enumeration/NotifyBizTypeEnum.java`
- Modify: `src/main/java/org/lemon/enumeration/NotifyChannelEnum.java`
- Modify: `src/main/java/org/lemon/enumeration/NotifyRecordStatusEnum.java`
- Modify: `src/main/java/org/lemon/mapper/NotifyPreferenceMapper.java`
- Modify: `src/main/java/org/lemon/mapper/NotifyRecordMapper.java`
- Modify: `src/main/java/org/lemon/mapper/NotifyTemplateMapper.java`

- [ ] **Step 1: Replace placeholder class comments with meaningful descriptions**

```java
/**
 * 安全配置。
 *
 * @author Lemon
 * @since 2024-10-02
 */
public class SecurityConfig {
```

```java
/**
 * JWT 认证过滤器。
 *
 * @author Lemon
 * @since 2025-05-10
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
```

```java
/**
 * 账单 控制层。
 *
 * @author Lemon
 * @since 2025-05-10
 */
public class AppController {
```

- [ ] **Step 2: Add method comments to public controller and service entrypoints that were added or changed in the unpushed branch**

```java
/**
 * 发送找回密码验证码。
 */
public ApiResp<Boolean> sendResetCode(@Validated @RequestBody ApiReq<UserSendResetCodeReq> req) {
    return ApiResp.ok(userService.sendResetCode(req.getData()));
}

/**
 * 重置密码。
 */
public ApiResp<Boolean> resetPassword(@Validated @RequestBody ApiReq<UserResetPasswordReq> req) {
    return ApiResp.ok(userService.resetPassword(req.getData()));
}
```

```java
/**
 * 发送找回密码验证码。
 */
public Boolean sendResetCode(UserSendResetCodeReq req) {
```

```java
/**
 * 校验访问令牌是否仍然有效。
 */
public boolean isAccessTokenValid(String token, String userInfo) {
```

- [ ] **Step 3: Add field comments to the changed request and response models**

```java
public class FinanceTransactionsQueryReq extends BasePage {

    /**
     * 备注关键字
     */
    private String keyword;
}
```

```java
public class UserSendResetCodeReq {

    /**
     * 找回密码使用的邮箱地址
     */
    private String email;
}
```

```java
public class UserResetPasswordReq {

    /**
     * 找回密码使用的邮箱地址
     */
    private String email;

    /**
     * 邮箱验证码
     */
    private String code;

    /**
     * 重置后的新密码
     */
    private String newPassword;
}
```

```java
public class NotifyPreferenceVO {

    /**
     * 是否开启每日记账邮件提醒
     */
    private Boolean emailReminderEnabled;

    /**
     * 是否开启月度摘要邮件通知
     */
    private Boolean monthlySummaryEnabled;

    /**
     * 提醒发送小时，0~23
     */
    private Integer reminderSendHour;
}
```

- [ ] **Step 4: Run a compile check after the comment cleanup to ensure no accidental edits broke code**

Run: `mvn -q -DskipTests compile`

Expected: BUILD SUCCESS.

### Task 5: Final Verification

**Files:**
- Modify: only if verification exposes a real issue

- [ ] **Step 1: Run the focused new tests again**

Run: `mvn test -Dtest=NotifyScheduleServiceTest,DailyBookkeepingNotifyServiceTest,MonthlySummaryNotifyServiceTest`

Expected: PASS with `Failures: 0, Errors: 0`.

- [ ] **Step 2: Run a final compile verification**

Run: `mvn -q -DskipTests compile`

Expected: BUILD SUCCESS.

- [ ] **Step 3: Inspect the final diff to confirm the scope stayed inside the plan boundary**

Run: `git diff --stat origin/main...HEAD`

Expected: the diff shows notification service refactor plus comment cleanup in files already touched by the unpushed branch, without unrelated module churn.
