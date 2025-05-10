package org.lemon.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.lemon.entity.Accounts;
import org.lemon.mapper.AccountsMapper;
import org.lemon.service.AccountsService;
import org.springframework.stereotype.Service;

/**
 * 账户表 服务层实现。
 *
 * @author Lemon
 * @since 2024-11-03
 */
@Service
public class AccountsServiceImpl extends ServiceImpl<AccountsMapper, Accounts> implements AccountsService {

}
