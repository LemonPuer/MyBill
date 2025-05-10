package org.lemon.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.lemon.entity.MonthlyBalances;
import org.lemon.mapper.MonthlyBalancesMapper;
import org.lemon.service.MonthlyBalancesService;
import org.springframework.stereotype.Service;

/**
 * 月度收支记录表 服务层实现。
 *
 * @author Lemon
 * @since 2024-11-03
 */
@Service
public class MonthlyBalancesServiceImpl extends ServiceImpl<MonthlyBalancesMapper, MonthlyBalances> implements MonthlyBalancesService {

}
