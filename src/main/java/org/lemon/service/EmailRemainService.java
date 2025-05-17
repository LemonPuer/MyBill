package org.lemon.service;

import cn.hutool.core.date.DateUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.lemon.entity.EmailRemain;
import org.lemon.entity.resp.EmailRemainVO;
import org.lemon.enumeration.EmailRemainEnum;
import org.lemon.enumeration.IBaseEnum;
import org.lemon.mapper.EmailRemainMapper;
import org.lemon.utils.UserUtil;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 邮件提醒表 服务层实现。
 *
 * @author Lemon
 * @since 2025-05-17
 */
@Service
public class EmailRemainService extends ServiceImpl<EmailRemainMapper, EmailRemain> {

    public List<EmailRemainVO> getRecentReminder() {
        Integer userId = UserUtil.getCurrentUserId();
        Date date = new Date();
        return queryChain().eq(EmailRemain::getUserId, userId).list().stream()
                .map(o -> EmailRemainVO.builder().remainDate(o.getRemainDate()).title(o.getTitle())
                        .remark(o.getRemark()).amount(o.getAmount())
                        .typeStr(IBaseEnum.getValueByKey(EmailRemainEnum.class, o.getType()))
                        .durationBetweenNow(TimeUnit.MILLISECONDS.toHours(DateUtil.betweenMs(date, o.getRemainDate())))
                        .build())
                .sorted(Comparator.comparing(EmailRemainVO::getDurationBetweenNow))
                .collect(Collectors.toList());
    }
}
