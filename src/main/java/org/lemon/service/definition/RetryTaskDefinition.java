package org.lemon.service.definition;

import org.lemon.entity.dto.RetryTaskTypeResultDTO;
import org.lemon.enumeration.RetryTaskTypeEnum;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2026/02/07 13:07:51
 */
public interface RetryTaskDefinition {

    /**
     * 获取任务类型
     *
     * @return
     */
    RetryTaskTypeEnum getType();

    /**
     * 执行任务
     *
     * @param userId
     * @param taskData
     * @return
     */
    RetryTaskTypeResultDTO execute(Integer userId, String taskData);

}
