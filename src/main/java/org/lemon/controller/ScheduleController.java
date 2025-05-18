package org.lemon.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.common.ApiReq;
import org.lemon.entity.common.IdReq;
import org.lemon.service.MonthTotalRecordService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/18 14:12:50
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("schedule")
public class ScheduleController {

    private final MonthTotalRecordService monthTotalRecordService;

    @Async
    @Scheduled(cron = "0 0 3 1 * ? ")
    public void monthStatistics() {
        monthTotalRecordService.monthStatistics(null);
    }

    /**
     * 统计月度账单
     *
     * @param req
     */
    @RequestMapping("monthStatistics")
    public void monthStatistics(@Validated @RequestBody ApiReq<IdReq> req) {
        monthTotalRecordService.monthStatistics(req.getData().getId());
    }
}

