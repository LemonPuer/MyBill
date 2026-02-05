package org.lemon.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.lemon.entity.FinanceTransactions;
import org.lemon.entity.MonthTotalRecord;
import org.lemon.entity.User;
import org.lemon.entity.req.ConsumerTrendsReq;
import org.lemon.entity.resp.ConsumerTrendsVO;
import org.lemon.enumeration.AmountTypeEnum;
import org.lemon.mapper.FinanceTransactionsMapper;
import org.lemon.mapper.MonthTotalRecordMapper;
import org.lemon.mapper.UserMapper;
import org.lemon.utils.UserUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 月度总收支记录表 服务层实现。
 *
 * @author Lemon
 * @since 2025-05-17
 */
@Service
@AllArgsConstructor
public class MonthTotalRecordService extends ServiceImpl<MonthTotalRecordMapper, MonthTotalRecord> {

    private final UserMapper userMapper;
    private final FinanceTransactionsMapper financeTransactionsMapper;

    public Page<ConsumerTrendsVO> getConsumerTrends(ConsumerTrendsReq data) {
        Integer userId = UserUtil.getCurrentUserId();
        Page<MonthTotalRecord> page = queryChain().eq(MonthTotalRecord::getUserId, userId)
                .between(MonthTotalRecord::getCreatedAt, data.getStartTime(), data.getEndTime())
                .orderBy(MonthTotalRecord::getId, false)
                .page(new Page<>(data.getPageNum(), data.getPageSize()));
        Page<ConsumerTrendsVO> result = new Page<>();
        result.setTotalRow(page.getTotalRow());
        if (Objects.equals(page.getTotalRow(), 0)) {
            return result;
        }
        List<ConsumerTrendsVO> list = page.getRecords().stream().map(o -> ConsumerTrendsVO.builder()
                .month(o.getMonth())
                .totalIncome(o.getTotalIncome().doubleValue())
                .totalExpense(o.getTotalExpense().doubleValue())
                .totalBalance(o.getTotalBalance().doubleValue())
                .build()).collect(Collectors.toList());
        result.setRecords(list);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void monthStatistics(Integer userId) {
        // 1.统计上月数据
        monthStatisticsPart1(userId);
        // 2.处理需要重复统计的数据
        monthStatisticsPart2();
    }

    private void monthStatisticsPart1(Integer userId) {
        DateTime dateTime = DateUtil.offsetMonth(new Date(), -1);
        String lastMonth = DateUtil.format(dateTime, DatePattern.NORM_MONTH_FORMAT);
        List<Integer> userIds;
        List<MonthTotalRecord> records = new ArrayList<>();
        if (userId != null) {
            userIds = Collections.singletonList(userId);
        } else {
            // 查询所有用户
            userIds = userMapper.selectAll().stream().map(User::getId).collect(Collectors.toList());
        }
        for (Integer id : userIds) {
            MonthTotalRecord result = statistics(id, lastMonth, DateUtil.beginOfMonth(dateTime).toLocalDateTime(), DateUtil.endOfMonth(dateTime).toLocalDateTime());
            records.add(result);
        }
        if (CollUtil.isNotEmpty(records)) {
            saveOrUpdateBatch(records);
        }
    }

    private MonthTotalRecord statistics(Integer userId, String month, LocalDateTime startTime, LocalDateTime endTime) {
        MonthTotalRecord record = queryChain().eq(MonthTotalRecord::getUserId, userId)
                .eq(MonthTotalRecord::getMonth, month).one();
        Map<Integer, Double> map = QueryChain.of(financeTransactionsMapper).eq(FinanceTransactions::getUserId, userId)
                .between(FinanceTransactions::getTransactionDate, startTime, endTime).list()
                .stream().collect(Collectors.groupingBy(FinanceTransactions::getType,
                        Collectors.summingDouble(o -> o.getAmount().doubleValue())));
        BigDecimal income = BigDecimal.valueOf(map.getOrDefault(AmountTypeEnum.INCOME.getCode(), 0D));
        BigDecimal expense = BigDecimal.valueOf(map.getOrDefault(AmountTypeEnum.EXPENSE.getCode(), 0D));
        if (record == null) {
            return MonthTotalRecord.builder().userId(userId).month(month).totalIncome(income).totalExpense(expense)
                    .totalBalance(income.subtract(expense)).build();
        }
        record.setTotalIncome(income);
        record.setTotalExpense(expense);
        record.setTotalBalance(income.subtract(expense));
        return record;
    }

    private void monthStatisticsPart2() {
        // 查询所有需要重新统计数据的月份
        Map<String, List<Integer>> monthUserMap = queryChain()
                .eq(MonthTotalRecord::getRepeat, true)
                .list().stream().collect(Collectors.groupingBy(MonthTotalRecord::getMonth,
                        Collectors.mapping(MonthTotalRecord::getUserId, Collectors.toList())));
        List<MonthTotalRecord> records = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : monthUserMap.entrySet()) {
            String month = entry.getKey();
            List<Integer> userIds = entry.getValue();
            DateTime parse = DateUtil.parse(month);
            for (Integer id : userIds) {
                MonthTotalRecord statistics = statistics(id, month, DateUtil.beginOfMonth(parse).toLocalDateTime(), DateUtil.endOfMonth(parse).toLocalDateTime());
                records.add(statistics);
            }
        }
        if (CollUtil.isNotEmpty(records)) {
            saveOrUpdateBatch(records);
        }
    }
}
