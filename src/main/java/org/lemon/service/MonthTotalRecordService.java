package org.lemon.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.lemon.entity.MonthTotalRecord;
import org.lemon.entity.req.ConsumerTrendsReq;
import org.lemon.entity.resp.ConsumerTrendsVO;
import org.lemon.mapper.MonthTotalRecordMapper;
import org.lemon.utils.UserUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 月度总收支记录表 服务层实现。
 *
 * @author Lemon
 * @since 2025-05-17
 */
@Service
public class MonthTotalRecordService extends ServiceImpl<MonthTotalRecordMapper, MonthTotalRecord> {

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
}
