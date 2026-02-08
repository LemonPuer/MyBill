package org.lemon.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.RetryTask;
import org.lemon.entity.User;
import org.lemon.entity.common.BasePage;
import org.lemon.entity.dto.RetryTaskTypeResultDTO;
import org.lemon.entity.dto.SystemEmailDTO;
import org.lemon.enumeration.RetryTaskStatusEnum;
import org.lemon.enumeration.RetryTaskTypeEnum;
import org.lemon.mapper.RetryTaskMapper;
import org.lemon.mapper.UserMapper;
import org.lemon.utils.UserUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI任务失败重试表 服务层实现
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2026/02/07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetryTaskService extends ServiceImpl<RetryTaskMapper, RetryTask> {

    private final UserMapper userMapper;
    private final EmailSendService emailSendService;

    @Transactional(rollbackFor = Exception.class)
    public Long createRetryTask(RetryTaskTypeEnum taskType, Object taskData, Integer userId) {
        return createRetryTask(taskType, taskData, userId, 3);
    }


    /**
     * 创建重试任务
     *
     * @param taskType      任务类型
     * @param taskData      任务数据
     * @param userId        用户ID
     * @param maxRetryCount 最大重试次数
     * @return 任务ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createRetryTask(RetryTaskTypeEnum taskType, Object taskData, Integer userId, Integer maxRetryCount) {
        RetryTask task = RetryTask.builder()
                .taskType(taskType)
                .taskData(JSONObject.toJSONString(taskData))
                .userId(userId)
                .maxRetryCount(maxRetryCount != null ? maxRetryCount : 3)
                .build();

        save(task);
        return task.getId();
    }

    /**
     * 获取待重试的任务列表
     *
     * @return 待重试任务列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<RetryTask> getPendingTasks() {
        List<RetryTask> list = queryChain().eq(RetryTask::getStatus, RetryTaskStatusEnum.PENDING)
                .orderBy(RetryTask::getCreatedTime, true)
                .limit(100).list();
        if (CollUtil.isEmpty(list)) {
            return list;
        }
        updateChain().set(RetryTask::getStatus, RetryTaskStatusEnum.RETRYING)
                .set(RetryTask::getUpdatedTime, LocalDateTime.now())
                .in(RetryTask::getId, list.stream().map(RetryTask::getId).collect(Collectors.toSet()))
                .update();
        return list;
    }

    /**
     * 发送失败通知邮件
     *
     * @param task 失败任务
     */
    private void sendFailureNotification(RetryTask task) {
        try {
            User user = userMapper.selectOneById(task.getUserId());
            if (user == null || StrUtil.isBlank(user.getEmail())) {
                log.warn("用户邮箱为空，无法发送通知，用户ID: {}", task.getUserId());
                return;
            }

            String taskTypeName = task.getTaskType() != null ? task.getTaskType().getDesc() : StrUtil.format("任务id：{}执行失败", task.getId());

            SystemEmailDTO emailDTO = new SystemEmailDTO();
            emailDTO.setTargetUser(user.getUsername());
            emailDTO.setTargetEmail(user.getEmail());
            emailDTO.setSubject("任务处理失败通知");
            emailDTO.setFileName("ai_task_failure_notification.html");
            emailDTO.setTemplateParams(new HashMap<>() {{
                put("userName", user.getUsername());
                put("taskType", taskTypeName);
                put("errorMessage", task.getErrorMessage());
                put("taskId", String.valueOf(task.getId()));
                put("failureTime", LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss"));
            }});
            emailSendService.sendSystemEmail(emailDTO);
            // 更新通知状态
            task.setNotifyStatus(Boolean.TRUE);
        } catch (Exception e) {
            log.error("发送失败通知邮件失败，任务ID: {}", task.getId(), e);
            task.setNotifyStatus(Boolean.FALSE);
        }
    }

    /**
     * 获取用户任务列表（分页）
     *
     * @param basePage 分页参数
     * @return 分页结果
     */
    public Page<RetryTask> getUserTasks(BasePage basePage) {
        Integer userId = UserUtil.getCurrentUserId();
        return queryChain()
                .eq(RetryTask::getUserId, userId)
                .orderBy(RetryTask::getCreatedTime, false)
                .page(new Page<>(basePage.getPageNum(), basePage.getPageSize()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveTaskResult(Map<Long, RetryTaskTypeResultDTO> result) {
        if (CollUtil.isEmpty(result)) {
            return;
        }
        List<RetryTask> list = queryChain().eq(RetryTask::getStatus, 0)
                .in(RetryTask::getId, result.keySet()).list();
        for (RetryTask task : list) {
            setRetryTaskStatus(task, result.get(task.getId()));
        }
        updateBatch(list);
    }

    private void setRetryTaskStatus(RetryTask task, RetryTaskTypeResultDTO dto) {
        boolean success = false;
        String message = "";
        if (dto != null) {
            success = Boolean.TRUE.equals(dto.getSuccess());
            message = dto.getData();
        }
        task.setUpdatedTime(LocalDateTime.now());
        if (success) {
            task.setErrorMessage("");
            task.setSuccessResult(message);
            task.setStatus(RetryTaskStatusEnum.SUCCESS);
            return;
        }
        task.setRetryCount(task.getRetryCount() + 1);
        if (task.getRetryCount() >= task.getMaxRetryCount()) {
            task.setErrorMessage(message);
            task.setStatus(RetryTaskStatusEnum.FAILURE);
            sendFailureNotification(task);
            return;
        }
        task.setStatus(RetryTaskStatusEnum.PENDING);
    }

}