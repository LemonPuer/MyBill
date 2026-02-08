package org.lemon.controller;

import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.RetryTask;
import org.lemon.entity.common.ApiReq;
import org.lemon.entity.common.BasePage;
import org.lemon.entity.common.PageResp;
import org.lemon.entity.dto.RetryTaskTypeResultDTO;
import org.lemon.enumeration.RetryTaskTypeEnum;
import org.lemon.service.RetryTaskService;
import org.lemon.service.definition.RetryTaskDefinition;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
public class RetryScheduleController {

    private final RetryTaskService retryTaskService;
    private final Map<RetryTaskTypeEnum, RetryTaskDefinition> factory;

    public RetryScheduleController(RetryTaskService retryTaskService, List<RetryTaskDefinition> taskList) {
        this.retryTaskService = retryTaskService;
        this.factory = taskList.stream().collect(Collectors.toMap(RetryTaskDefinition::getType, Function.identity()));
    }


    /**
     * 每10分钟处理一次待处理的任务
     */
    @Async
    @Scheduled(cron = "0 */10 * * * *")
    public void processRetryTasks() {
        List<RetryTask> pendingTasks = retryTaskService.getPendingTasks();
        Map<RetryTaskTypeEnum, List<RetryTask>> taskTypeMap = pendingTasks.stream().collect(Collectors.groupingBy(RetryTask::getTaskType));
        taskTypeMap.forEach((type, taskList) -> {
            try {
                RetryTaskDefinition task = factory.get(type);
                retryTaskService.saveTaskResult(task.execute(taskList));
            } catch (Exception e) {
                log.error("处理重试任务时发生异常", e);
            }
        });
    }

    /**
     * 获取当前用户的重试任务列表
     */
    @PostMapping("list")
    public PageResp<RetryTask> getUserTasks(@Validated @RequestBody ApiReq<BasePage> req) {
        return PageResp.ok(retryTaskService.getUserTasks(req.getData()));
    }
}