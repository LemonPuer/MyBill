package org.lemon.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.lemon.entity.AiRetryTask;
import org.lemon.mapper.AiRetryTaskMapper;
import org.springframework.stereotype.Service;

/**
 * AI任务失败重试表 服务层实现。
 *
 * @author Lemon
 * @since 2026-02-07
 */
@Service
public class AiRetryTaskServiceImpl extends ServiceImpl<AiRetryTaskMapper, AiRetryTask> {

}
