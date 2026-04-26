package org.lemon.controller;

import lombok.AllArgsConstructor;
import org.lemon.entity.common.ApiReq;
import org.lemon.entity.common.IdReq;
import org.lemon.service.MonthTotalRecordService;
import org.lemon.service.NotifyScheduleService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 定时任务控制器
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/18 14:12:50
 */
@RestController
@AllArgsConstructor
@RequestMapping("schedule")
public class ScheduleController {

    private final MonthTotalRecordService monthTotalRecordService;
    private final NotifyScheduleService notifyScheduleService;

    /**
     * 每月1日凌晨统计上月账单汇总
     */
    @Async
    @Scheduled(cron = "0 0 3 1 * ? ")
    public void monthStatistics() {
        monthTotalRecordService.monthStatistics(null);
    }

    /**
     * 按小时生成待发送的每日记账提醒
     */
    @Async
    @Scheduled(cron = "0 0 * * * ?")
    public void generateDailyBookkeepingNotifyRecords() {
        notifyScheduleService.generateDailyBookkeepingNotifyRecords();
    }

    /**
     * 每月1日上午生成上月汇总通知
     */
    @Async
    @Scheduled(cron = "0 0 9 1 * ?")
    public void generateMonthlySummaryNotifyRecords() {
        notifyScheduleService.generateMonthlySummaryNotifyRecords();
    }

    /**
     * 轮询发送到期的通知记录
     */
    @Async
    @Scheduled(cron = "0 * * * * ?")
    public void sendDailyBookkeepingNotifyRecords() {
        notifyScheduleService.sendDueNotifyRecords();
    }

    /**
     * 统计月度账单
     */
    @RequestMapping("monthStatistics")
    public void monthStatistics(@Validated @RequestBody ApiReq<IdReq> req) {
        monthTotalRecordService.monthStatistics(req.getData().getId());
    }
}

