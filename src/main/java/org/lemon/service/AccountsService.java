package org.lemon.service;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.lemon.entity.Accounts;
import org.lemon.entity.FinanceTransactions;
import org.lemon.entity.MonthTotalRecord;
import org.lemon.entity.exception.BusinessException;
import org.lemon.entity.req.AccountReq;
import org.lemon.entity.resp.AccountVO;
import org.lemon.entity.resp.CommonDicVO;
import org.lemon.enumeration.AccountCategoryEnum;
import org.lemon.enumeration.AccountTypeEnum;
import org.lemon.enumeration.IBaseEnum;
import org.lemon.mapper.AccountsMapper;
import org.lemon.mapper.FinanceTransactionsMapper;
import org.lemon.mapper.MonthTotalRecordMapper;
import org.lemon.utils.UserUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 账户表 服务层实现。
 *
 * @author Lemon
 * @since 2025-05-17
 */
@Service
@AllArgsConstructor
public class AccountsService extends ServiceImpl<AccountsMapper, Accounts> {

    private final MonthTotalRecordMapper monthTotalRecordMapper;
    private final FinanceTransactionsMapper financeTransactionsMapper;

    public List<AccountVO> getAccounts(Integer pid) {
        Integer userId = UserUtil.getCurrentUserId();
        Set<Integer> parentIdSet = new HashSet<>();
        List<AccountVO> list = queryChain().eq(Accounts::getUserId, userId).list()
                .stream().map(o -> {
                    parentIdSet.add(o.getPid());
                    if (Objects.equals(o.getPid(), pid == null ? 0 : pid)) {
                        return AccountVO.builder().id(o.getId()).pid(o.getPid())
                                .accountType(IBaseEnum.getValueByKey(AccountTypeEnum.class, o.getAccountType()))
                                .accountCategory(IBaseEnum.getValueByKey(AccountCategoryEnum.class, o.getAccountCategory()))
                                .amount(o.getAmount().doubleValue()).accountName(o.getAccountName())
                                .build();
                    } else {
                        return null;
                    }
                }).filter(o -> !Objects.isNull(o))
                .collect(Collectors.toList());
        for (AccountVO vo : list) {
            vo.setChildren(parentIdSet.contains(vo.getId()));
        }
        return list;
    }


    public List<CommonDicVO> getAccountTree() {
        Integer userId = UserUtil.getCurrentUserId();
        List<CommonDicVO> result = new ArrayList<>();
        Map<Integer, List<Accounts>> treeMap = queryChain().eq(Accounts::getUserId, userId).list()
                .stream().collect(Collectors.groupingBy(Accounts::getPid));
        if (CollUtil.isEmpty(treeMap)) {
            return result;
        }
        List<Accounts> parent = treeMap.get(0);
        for (Accounts accounts : parent) {
            CommonDicVO dic = new CommonDicVO();
            dic.setId(accounts.getId());
            dic.setName(accounts.getAccountName());
            List<CommonDicVO> children = treeMap.getOrDefault(accounts.getId(), new ArrayList<>()).stream().map(o -> {
                CommonDicVO vo = new CommonDicVO();
                vo.setId(o.getId());
                vo.setName(o.getAccountName());
                return vo;
            }).collect(Collectors.toList());
            dic.setChildren(children);
        }
        return result;
    }

    // private List<AccountVO> combinationTree(Integer parentId, List<AccountVO> list) {
    //     return list.stream()
    //             .filter(node -> node.getPid() != null && node.getPid().equals(parentId))
    //             .peek(node -> node.setChildren(combinationTree(node.getId(), list)))
    //             .collect(Collectors.toList());
    // }

    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdateAccount(AccountReq data) {
        Integer userId = UserUtil.getCurrentUserId();
        Accounts result = data.toAccounts();
        // 查询是否存在子类，单机加锁
        synchronized (this) {
            List<Accounts> list = queryChain().eq(Accounts::getPid, data.getId()).list();
            if (CollUtil.isNotEmpty(list)) {
                throw new BusinessException("账户下存在子账户，请先删除子账户！");
            }
            if (result.getPid() != null && result.getPid() != 0) {
                Accounts accounts = Optional.ofNullable(getById(result.getPid())).orElseThrow(() -> new BusinessException("父账户不存在！"));
                if (accounts.getHierarchy() > 1) {
                    throw new BusinessException("账户层级不能超过2级！");
                }
                accounts.setAmount(BigDecimal.ZERO);
                updateById(accounts);
            }
            if (data.getId() != null && data.getId() > 0) {
                result.setUpdateNo(userId);
            } else {
                result.setCreateNo(userId);
            }
            return saveOrUpdate(result);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean delAccount(Integer id) {
        Integer userId = UserUtil.getCurrentUserId();
        // 校验
        if (!queryChain().eq(Accounts::getId, id).eq(Accounts::getUserId, userId).exists()) {
            throw new BusinessException("账户不存在！");
        }
        Set<Integer> ids = queryChain().eq(Accounts::getPid, id).list().stream().map(Accounts::getId).collect(Collectors.toSet());
        ids.add(id);
        // 1.删除账户
        removeByIds(ids);
        // 2.删除账户绑定的账单信息
        UpdateChain.of(financeTransactionsMapper).in(FinanceTransactions::getAccountId, ids).remove();
        // 3.设置月度账单重新统计
        UpdateChain.of(monthTotalRecordMapper).set(MonthTotalRecord::getRepeat, true)
                .eq(MonthTotalRecord::getUserId, userId)
                .update();
        return true;
    }
}
