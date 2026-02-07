package org.lemon.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lemon.chat.BillAssistantService;
import org.lemon.entity.RetryTask;
import org.lemon.entity.common.ApiReq;
import org.lemon.entity.common.BasePage;
import org.lemon.entity.common.PageResp;
import org.lemon.service.RetryTaskService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI重试任务调度控制器
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2026/02/07
 */
@Slf4j
@RestController
@RequestMapping("schedule/ai-retry")
@RequiredArgsConstructor
public class AiRetryScheduleController {

    private final RetryTaskService retryTaskService;

    /**
     * 每10分钟处理一次待处理的任务
     */
    @Async
    @Scheduled(cron = "0 */10 * * * *")
    public void processRetryTasks() {
        try {
            log.info("开始处理AI重试任务...");

            List<RetryTask> pendingTasks = retryTaskService.getPendingTasks();
            log.info("发现 {} 个待处理任务", pendingTasks.size());

            int successCount = 0;
            int failureCount = 0;

            for (RetryTask task : pendingTasks) {
                boolean success = retryTaskService.executeRetry(task.getId(), taskData -> {
                    //ZFH TODO : 2026/2/7 待调整
                    // 根据任务类型执行不同的重试逻辑
                    switch (task.getTaskType()) {
                        case TELEGRAM_BILL_PARSE:
                            return null;
                        default:
                            throw new IllegalArgumentException("不支持的任务类型: " + task.getTaskType());
                    }
                });

                if (success) {
                    successCount++;
                } else {
                    failureCount++;
                }
            }

            log.info("AI重试任务处理完成 - 成功: {}, 失败: {}", successCount, failureCount);

        } catch (Exception e) {
            log.error("处理AI重试任务时发生异常", e);
        }
    }

    /**
     * 获取当前用户的重试任务列表
     */
    @PostMapping("list")
    public PageResp<RetryTask> getUserTasks(@Validated @RequestBody ApiReq<BasePage> req) {
        return PageResp.ok(retryTaskService.getUserTasks(req.getData()));
    }
}