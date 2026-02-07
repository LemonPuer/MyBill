package org.lemon.service;

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
import org.lemon.enumeration.RetryTaskTypeEnum;
import org.lemon.mapper.RetryTaskMapper;
import org.lemon.mapper.UserMapper;
import org.lemon.utils.UserUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

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
    public List<RetryTask> getPendingTasks() {
        return queryChain().eq(RetryTask::getStatus, 0)
                .orderBy(RetryTask::getCreatedTime, true)
                .limit(100).list();
    }

    /**
     * 执行任务重试
     *
     * @param taskId        任务ID
     * @param retryFunction 重试函数
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean executeRetry(Long taskId, RetryFunction retryFunction) {
        RetryTask task = getById(taskId);
        if (task == null) {
            log.warn("任务不存在，ID: {}", taskId);
            return false;
        }

        if (task.getStatus() != 0) {
            log.warn("任务状态不正确，ID: {}, 状态: {}", taskId, task.getStatus());
            return false;
        }
        // ZFH TODO : 2026/2/7 待调整
        // 更新任务状态为重试中
        updateChain()
                .set(RetryTask::getStatus, 1) // 重试中
                .set(RetryTask::getUpdatedTime, LocalDateTime.now())
                .eq(RetryTask::getId, taskId)
                .update();
        String errorMessage = "";
        RetryTaskTypeResultDTO execute = new RetryTaskTypeResultDTO(false, "");
        try {
            // 执行重试逻辑
            execute = retryFunction.execute(task.getTaskData());
        } catch (Exception e) {
            errorMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error("任务重试失败，ID: {}", taskId, e);
        }
        if (Boolean.TRUE.equals(execute.getSuccess())) {
            // 重试成功
            updateChain()
                    .set(RetryTask::getStatus, 2) // 重试成功
                    .set(RetryTask::getSuccessResult, execute.getData())
                    .set(RetryTask::getRetryCount, task.getRetryCount() + 1)
                    .set(RetryTask::getUpdatedTime, LocalDateTime.now())
                    .eq(RetryTask::getId, taskId)
                    .update();
            return true;
        } else {
            int newRetryCount = task.getRetryCount() + 1;
            int status = newRetryCount >= task.getMaxRetryCount() ? 3 : 0; // 达到最大次数则标记为失败

            // 更新失败信息
            updateChain()
                    .set(RetryTask::getStatus, status)
                    .set(RetryTask::getRetryCount, newRetryCount)
                    .set(RetryTask::getErrorMessage, errorMessage)
                    .set(RetryTask::getUpdatedTime, LocalDateTime.now())
                    .eq(RetryTask::getId, taskId)
                    .update();

            // 如果达到最大重试次数，发送邮件通知
            if (status == 3) {
                sendFailureNotification(task, errorMessage);
            }

            return false;
        }
    }

    /**
     * 发送失败通知邮件
     *
     * @param task         失败任务
     * @param errorMessage 异常信息
     */
    private void sendFailureNotification(RetryTask task, String errorMessage) {
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
                put("errorMessage", errorMessage);
                put("taskId", String.valueOf(task.getId()));
                put("failureTime", LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss"));
            }});

            emailSendService.sendSystemEmail(emailDTO);

            // 更新通知状态
            updateChain().set(RetryTask::getNotifyStatus, 1)
                    .eq(RetryTask::getId, task.getId())
                    .update();

            log.info("发送失败通知邮件成功，任务ID: {}, 用户: {}", task.getId(), user.getUsername());

        } catch (Exception e) {
            log.error("发送失败通知邮件失败，任务ID: {}", task.getId(), e);
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

    /**
     * 重试函数接口
     */
    @FunctionalInterface
    public interface RetryFunction {
        /**
         * 执行重试逻辑
         *
         * @param taskData 任务数据
         * @return 成功结果
         * @throws Exception 执行异常
         */
        RetryTaskTypeResultDTO execute(String taskData) throws Exception;
    }
}