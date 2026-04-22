# Monthly Summary Notify Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add monthly summary email generation on the 1st of each month at 09:00 using month total records and DB-backed notify templates.

**Architecture:** Reuse the notify center foundation instead of adding a separate email path. Generate one monthly-summary notify record per eligible user/month from `tt_month_total_record`, then let the existing notify sender deliver it from the `MONTHLY_SUMMARY` template stored in `tt_notify_template`.

**Tech Stack:** Spring Boot scheduling, MyBatis-Flex, FreeMarker, MySQL, Maven

---

## File Map

- Modify: `checkList/20260422/DDL.sql`
  - Add `MONTHLY_SUMMARY` template seed row.
- Modify: `src/main/java/org/lemon/service/NotifyRecordService.java`
  - Add monthly-summary record creation and biz key logic.
- Modify: `src/main/java/org/lemon/controller/ScheduleController.java`
  - Add monthly-summary generation scheduler.
- Modify: `src/main/java/org/lemon/service/MonthTotalRecordService.java`
  - Add lookup method for last month summary rows by user.
- Modify: `docs/superpowers/2026-04-21-backend-task-document.md`
  - Mark task 8 progress and finalized rule.

## Task 1: Seed monthly summary template

**Files:**
- Modify: `checkList/20260422/DDL.sql`

- [ ] **Step 1: Add `MONTHLY_SUMMARY` template row**
- [ ] **Step 2: Keep template content fully DB-backed and do not add file-template reads**

## Task 2: Add monthly summary record creation

**Files:**
- Modify: `src/main/java/org/lemon/service/NotifyRecordService.java`
- Modify: `src/main/java/org/lemon/service/MonthTotalRecordService.java`

- [ ] **Step 1: Add biz key format `monthly-summary:{userId}:{yyyy-MM}`**
- [ ] **Step 2: Add idempotent `PENDING` record creation method for monthly summary**
- [ ] **Step 3: Build payload from `MonthTotalRecord` with month label, income, expense, balance, action URL**

## Task 3: Add generation scheduler

**Files:**
- Modify: `src/main/java/org/lemon/controller/ScheduleController.java`

- [ ] **Step 1: Add scheduler `0 0 9 1 * ?`**
- [ ] **Step 2: Query enabled `MONTHLY_SUMMARY` preferences**
- [ ] **Step 3: For users with last month totals and nonblank email, create notify records**

## Task 4: Update task document and verify

**Files:**
- Modify: `docs/superpowers/2026-04-21-backend-task-document.md`

- [ ] **Step 1: Update task 8 to reflect notify-center implementation direction and rule**
- [ ] **Step 2: Run `mvn test`**
