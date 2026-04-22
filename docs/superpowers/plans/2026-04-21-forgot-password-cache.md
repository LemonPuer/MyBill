# Forgot Password Cache Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a single-node forgot-password flow backed by Caffeine cache with public send-code and reset-password endpoints.

**Architecture:** Keep the existing `UserController -> UserService` flow, add two request DTOs plus a small in-memory reset-code component, and reuse the existing email sending service for code delivery. Security remains header-based for authenticated routes, with only the two forgot-password endpoints added to the anonymous allowlist.

**Tech Stack:** Spring Boot, Spring Security, MyBatis-Flex, Caffeine, existing email templates/service, Maven

---

## File Map

- Modify: `pom.xml`
  - Add `Caffeine` dependency.
- Modify: `src/main/java/org/lemon/config/SecurityConfig.java`
  - Allow anonymous access to forgot-password endpoints.
- Modify: `src/main/java/org/lemon/controller/UserController.java`
  - Expose `sendResetCode` and `resetPassword` endpoints.
- Modify: `src/main/java/org/lemon/service/UserService.java`
  - Add send/reset business logic and email delivery wiring.
- Create: `src/main/java/org/lemon/entity/req/UserSendResetCodeReq.java`
  - Request body for sending reset code.
- Create: `src/main/java/org/lemon/entity/req/UserResetPasswordReq.java`
  - Request body for resetting password.
- Create: `src/main/java/org/lemon/entity/dto/UserResetCodeDTO.java`
  - In-memory cached reset-code payload.
- Create: `src/main/resources/email/reset_password_code.html`
  - Email template for reset-code delivery.

## Task 1: Add request and cache model types

**Files:**
- Create: `src/main/java/org/lemon/entity/req/UserSendResetCodeReq.java`
- Create: `src/main/java/org/lemon/entity/req/UserResetPasswordReq.java`
- Create: `src/main/java/org/lemon/entity/dto/UserResetCodeDTO.java`

- [ ] **Step 1: Add send-code request DTO**

```java
package org.lemon.entity.req;

import lombok.Data;

@Data
public class UserSendResetCodeReq {

    private String email;
}
```

- [ ] **Step 2: Add reset-password request DTO**

```java
package org.lemon.entity.req;

import lombok.Data;

@Data
public class UserResetPasswordReq {

    private String email;

    private String code;

    private String newPassword;
}
```

- [ ] **Step 3: Add cached reset-code payload DTO**

```java
package org.lemon.entity.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResetCodeDTO {

    private String code;

    private LocalDateTime expireAt;

    private LocalDateTime lastSendAt;
}
```

## Task 2: Wire controller and security entry points

**Files:**
- Modify: `src/main/java/org/lemon/controller/UserController.java`
- Modify: `src/main/java/org/lemon/config/SecurityConfig.java`

- [ ] **Step 1: Add public controller endpoints**

```java
@PostMapping("/sendResetCode")
public ApiResp<Boolean> sendResetCode(@RequestBody @Validated ApiReq<UserSendResetCodeReq> req) {
    return ApiResp.ok(userService.sendResetCode(req.getData()));
}

@PostMapping("/resetPassword")
public ApiResp<Boolean> resetPassword(@RequestBody @Validated ApiReq<UserResetPasswordReq> req) {
    return ApiResp.ok(userService.resetPassword(req.getData()));
}
```

- [ ] **Step 2: Allow anonymous access in security config**

```java
.requestMatchers("/openApi/**", "/user/register", "/user/login",
        "/user/sendResetCode", "/user/resetPassword").permitAll()
```

## Task 3: Implement Caffeine-backed reset flow in service

**Files:**
- Modify: `src/main/java/org/lemon/service/UserService.java`
- Modify: `pom.xml`

- [ ] **Step 1: Add Caffeine dependency**

```xml
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

- [ ] **Step 2: Add cache constants and cache field in `UserService`**

```java
private static final int RESET_CODE_LENGTH = 6;
private static final long RESET_CODE_EXPIRE_MINUTES = 10L;
private static final long RESET_CODE_RESEND_SECONDS = 60L;

private final Cache<String, UserResetCodeDTO> resetCodeCache = Caffeine.newBuilder()
        .expireAfterWrite(RESET_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES)
        .maximumSize(1000)
        .build();
```

- [ ] **Step 3: Add send-code service method**

```java
public Boolean sendResetCode(UserSendResetCodeReq req) {
    String email = StrUtil.trim(req.getEmail());
    if (StrUtil.isBlank(email)) {
        throw new BusinessException("邮箱不能为空！");
    }
    User user = queryChain().eq(User::getEmail, email).one();
    if (user == null) {
        throw new BusinessException("邮箱未注册！");
    }
    UserResetCodeDTO cached = resetCodeCache.getIfPresent(email);
    LocalDateTime now = LocalDateTime.now();
    if (cached != null && cached.getLastSendAt() != null
            && Duration.between(cached.getLastSendAt(), now).getSeconds() < RESET_CODE_RESEND_SECONDS) {
        throw new BusinessException("验证码发送过于频繁，请稍后再试！");
    }
    String code = RandomUtil.randomNumbers(RESET_CODE_LENGTH);
    resetCodeCache.put(email, UserResetCodeDTO.builder()
            .code(code)
            .expireAt(now.plusMinutes(RESET_CODE_EXPIRE_MINUTES))
            .lastSendAt(now)
            .build());
    sendResetCodeEmail(user, code);
    return true;
}
```

- [ ] **Step 4: Add reset-password service method**

```java
public Boolean resetPassword(UserResetPasswordReq req) {
    String email = StrUtil.trim(req.getEmail());
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
    User user = queryChain().eq(User::getEmail, email).one();
    if (user == null) {
        throw new BusinessException("邮箱未注册！");
    }
    UserResetCodeDTO cached = resetCodeCache.getIfPresent(email);
    if (cached == null || cached.getExpireAt() == null || cached.getExpireAt().isBefore(LocalDateTime.now())) {
        resetCodeCache.invalidate(email);
        throw new BusinessException("验证码已过期，请重新获取！");
    }
    if (!StrUtil.equals(cached.getCode(), code)) {
        throw new BusinessException("验证码错误！");
    }
    updateChain().set(User::getPassword, passwordEncoder.encode(req.getNewPassword()))
            .eq(User::getId, user.getId())
            .update();
    resetCodeCache.invalidate(email);
    return true;
}
```

- [ ] **Step 5: Add reset email helper**

```java
private void sendResetCodeEmail(User user, String code) {
    SystemEmailDTO dto = new SystemEmailDTO();
    dto.setTargetUser(user.getUsername());
    dto.setTargetEmail(user.getEmail());
    dto.setSubject("MyBill 找回密码验证码");
    dto.setFileName("reset_password_code.html");
    Map<String, String> params = new HashMap<>();
    params.put("email", user.getEmail());
    params.put("code", code);
    params.put("expireMinutes", String.valueOf(RESET_CODE_EXPIRE_MINUTES));
    dto.setTemplateParams(params);
    emailSendService.sendSystemEmail(dto);
}
```

## Task 4: Add email template and verify integration

**Files:**
- Create: `src/main/resources/email/reset_password_code.html`

- [ ] **Step 1: Add reset-code email template**

```html
<!DOCTYPE html>
<html lang="zh-CN">
<body>
<p>您好，{{email}}：</p>
<p>您的 MyBill 找回密码验证码为：<strong>{{code}}</strong></p>
<p>验证码 {{expireMinutes}} 分钟内有效，请勿泄露给他人。</p>
</body>
</html>
```

- [ ] **Step 2: Run project verification without adding unit tests**

Run: `mvn test`

Expected:
- Build succeeds
- Existing tests remain green
- No new test files are added for forgot-password flow, per user request
