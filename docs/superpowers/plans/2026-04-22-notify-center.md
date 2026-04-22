# Notify Center Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a lightweight email notification center with DB-backed templates, user preferences, send records, and the first daily bookkeeping reminder flow.

**Architecture:** Add three persistence models (`template`, `preference`, `record`) and a FreeMarker-based rendering path in `EmailSendService`. Use one scheduled generator to create daily-bookkeeping `PENDING` records and one scheduled sender to deliver due records and update send status.

**Tech Stack:** Spring Boot, MyBatis-Flex, FreeMarker, Spring Mail, Maven, MySQL

---

## File Map

- Modify: `pom.xml`
  - Add `spring-boot-starter-freemarker`.
- Create: `src/main/java/org/lemon/entity/NotifyTemplate.java`
- Create: `src/main/java/org/lemon/entity/NotifyPreference.java`
- Create: `src/main/java/org/lemon/entity/NotifyRecord.java`
- Create: `src/main/java/org/lemon/mapper/NotifyTemplateMapper.java`
- Create: `src/main/java/org/lemon/mapper/NotifyPreferenceMapper.java`
- Create: `src/main/java/org/lemon/mapper/NotifyRecordMapper.java`
- Create: `src/main/java/org/lemon/enumeration/NotifyBizTypeEnum.java`
- Create: `src/main/java/org/lemon/enumeration/NotifyChannelEnum.java`
- Create: `src/main/java/org/lemon/enumeration/NotifyRecordStatusEnum.java`
- Create: `src/main/java/org/lemon/entity/dto/NotifyRenderDTO.java`
- Create: `src/main/java/org/lemon/service/NotifyTemplateService.java`
- Create: `src/main/java/org/lemon/service/NotifyPreferenceService.java`
- Create: `src/main/java/org/lemon/service/NotifyRecordService.java`
- Modify: `src/main/java/org/lemon/service/EmailSendService.java`
  - Add DB-template FreeMarker render/send support.
- Modify: `src/main/java/org/lemon/controller/ScheduleController.java`
  - Add two scheduled jobs for generation and sending.
- Create: `checkList/20260422/DDL.sql`
  - Add the three new notify tables and template seed data.
- Create: `src/test/java/org/lemon/service/NotifyRecordServiceTest.java`
  - Add focused unit tests around biz key generation / send-state rules.

## Task 1: Add notification persistence model

**Files:**
- Create: `src/main/java/org/lemon/entity/NotifyTemplate.java`
- Create: `src/main/java/org/lemon/entity/NotifyPreference.java`
- Create: `src/main/java/org/lemon/entity/NotifyRecord.java`
- Create: `src/main/java/org/lemon/mapper/NotifyTemplateMapper.java`
- Create: `src/main/java/org/lemon/mapper/NotifyPreferenceMapper.java`
- Create: `src/main/java/org/lemon/mapper/NotifyRecordMapper.java`
- Create: `src/main/java/org/lemon/enumeration/NotifyBizTypeEnum.java`
- Create: `src/main/java/org/lemon/enumeration/NotifyChannelEnum.java`
- Create: `src/main/java/org/lemon/enumeration/NotifyRecordStatusEnum.java`
- Create: `checkList/20260422/DDL.sql`

- [ ] **Step 1: Add notification enums and entities**
- [ ] **Step 2: Add mapper interfaces extending `BaseMapper<T>`**
- [ ] **Step 3: Add DDL for `tt_notify_template`, `tt_notify_preference`, `tt_notify_record`**
- [ ] **Step 4: Seed one `DAILY_BOOKKEEPING_REMINDER` template row in DDL**

## Task 2: Add DB-backed FreeMarker rendering

**Files:**
- Modify: `pom.xml`
- Create: `src/main/java/org/lemon/entity/dto/NotifyRenderDTO.java`
- Create: `src/main/java/org/lemon/service/NotifyTemplateService.java`
- Modify: `src/main/java/org/lemon/service/EmailSendService.java`

- [ ] **Step 1: Add `spring-boot-starter-freemarker` dependency**
- [ ] **Step 2: Add template lookup/render service reading DB templates by `code`**
- [ ] **Step 3: Add `EmailSendService` entrypoint that accepts target email + rendered subject/body**
- [ ] **Step 4: Keep old file-template send path intact for existing flows**

## Task 3: Add preference and record services

**Files:**
- Create: `src/main/java/org/lemon/service/NotifyPreferenceService.java`
- Create: `src/main/java/org/lemon/service/NotifyRecordService.java`
- Create: `src/test/java/org/lemon/service/NotifyRecordServiceTest.java`

- [ ] **Step 1: Add service for querying active preferences by biz type/hour**
- [ ] **Step 2: Add service for creating idempotent `PENDING` records by `bizKey`**
- [ ] **Step 3: Add service methods to mark `SENT` / `FAILED`**
- [ ] **Step 4: Add unit tests for idempotent key and state transitions**

## Task 4: Add daily bookkeeping generation job

**Files:**
- Modify: `src/main/java/org/lemon/controller/ScheduleController.java`
- Modify: `src/main/java/org/lemon/service/NotifyPreferenceService.java`
- Modify: `src/main/java/org/lemon/service/NotifyRecordService.java`

- [ ] **Step 1: Add scheduled generator method that runs hourly**
- [ ] **Step 2: Query active `DAILY_BOOKKEEPING` email preferences for current hour**
- [ ] **Step 3: Create one `PENDING` record per user/day using `daily-bookkeeping:{userId}:{date}`**
- [ ] **Step 4: Skip users without email or disabled preferences**

## Task 5: Add notify sender compensation job

**Files:**
- Modify: `src/main/java/org/lemon/controller/ScheduleController.java`
- Modify: `src/main/java/org/lemon/service/NotifyRecordService.java`
- Modify: `src/main/java/org/lemon/service/NotifyTemplateService.java`
- Modify: `src/main/java/org/lemon/service/EmailSendService.java`

- [ ] **Step 1: Add scheduled sender method that scans due `PENDING` records**
- [ ] **Step 2: Render template from DB with `payload_json` and send email**
- [ ] **Step 3: Mark record `SENT` with `sentTime` on success**
- [ ] **Step 4: Mark record `FAILED` with `errorMessage` on failure**

## Task 6: Verify and document

**Files:**
- Modify: `docs/superpowers/2026-04-21-backend-task-document.md`

- [ ] **Step 1: Update task 7 in backend task doc to reference the new notify-center direction**
- [ ] **Step 2: Run `mvn test`**
- [ ] **Step 3: Run targeted verification if needed for new notify services**
