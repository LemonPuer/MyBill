package org.lemon.service;

import cn.hutool.core.date.DateUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.lemon.entity.EmailRemain;
import org.lemon.entity.req.EmailRemainReq;
import org.lemon.entity.resp.EmailRemainVO;
import org.lemon.enumeration.EmailRemainEnum;
import org.lemon.enumeration.IBaseEnum;
import org.lemon.mapper.EmailRemainMapper;
import org.lemon.utils.UserUtil;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
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
                .map(o -> EmailRemainVO.builder().id(o.getId()).type(o.getType())
                        .remainDate(o.getRemainDate()).title(o.getTitle())
                        .remark(o.getRemark()).amount(o.getAmount())
                        .typeStr(IBaseEnum.getValueByKey(EmailRemainEnum.class, o.getType()))
                        .durationBetweenNow(TimeUnit.MILLISECONDS.toHours(DateUtil.betweenMs(date, o.getRemainDate())))
                        .build())
                .sorted(Comparator.comparing(EmailRemainVO::getDurationBetweenNow))
                .collect(Collectors.toList());
    }

    public Boolean saveOrUpdateEmailRemain(EmailRemainReq data) {
        Integer userId = UserUtil.getCurrentUserId();
        EmailRemain result = EmailRemain.builder()
                .id(data.getId()).userId(userId)
                .email(UserUtil.getCurrentUsername())
                .title(data.getTitle()).amount(data.getAmount())
                .type(data.getType()).remainDate(data.getRemainDate())
                .remark(data.getRemark()).build();
        if (data.getId() != null && data.getId() > 0) {
            result.setUpdateNo(userId);
        } else {
            result.setCreateNo(userId);
        }
        return saveOrUpdate(result);
    }
}
