package org.lemon.service.definition;

import org.lemon.entity.RetryTask;
import org.lemon.entity.dto.RetryTaskTypeResultDTO;
import org.lemon.enumeration.RetryTaskTypeEnum;

import java.util.List;
import java.util.Map;

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
     * @param taskList
     * @return
     */
    Map<Long, RetryTaskTypeResultDTO> execute(List<RetryTask> taskList);

}
