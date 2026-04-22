package org.lemon.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.lemon.entity.Budget;
import org.lemon.entity.Category;
import org.lemon.entity.FinanceTransactions;
import org.lemon.entity.common.BasePage;
import org.lemon.entity.exception.BusinessException;
import org.lemon.entity.req.CategorySaveReq;
import org.lemon.entity.resp.CategoryVO;
import org.lemon.mapper.BudgetMapper;
import org.lemon.mapper.CategoryMapper;
import org.lemon.mapper.FinanceTransactionsMapper;
import org.lemon.utils.UserUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 收支分类表 服务层实现。
 *
 * @author Lemon
 * @since 2025-05-17
 */
@Service
@RequiredArgsConstructor
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {

    private final FinanceTransactionsMapper financeTransactionsMapper;
    private final BudgetMapper budgetMapper;

    public Boolean saveOrUpdateCategory(CategorySaveReq data) {
        Integer userId = UserUtil.getCurrentUserId();
        if (Objects.isNull(data.getId())) {
            return save(Category.builder()
                    .userId(userId)
                    .category(data.getCategory())
                    .icon(data.getIcon())
                    .createNo(userId)
                    .build());
        }
        return updateChain().set(Category::getCategory, data.getCategory())
                .set(Category::getIcon, data.getIcon())
                .set(Category::getUpdateNo, userId)
                .eq(Category::getId, data.getId())
                .eq(Category::getUserId, userId)
                .update();
    }

    public Page<CategoryVO> getCategory(BasePage data) {
        return queryChain().select(Category::getId, Category::getCategory, Category::getIcon)
                .eq(Category::getUserId, UserUtil.getCurrentUserId())
                .orderBy(Category::getId, false)
                .pageAs(new Page<>(data.getPageNum(), data.getPageSize()), CategoryVO.class);
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
    public Boolean delCategory(Integer id) {
        Integer userId = UserUtil.getCurrentUserId();
        Category category = queryChain().eq(Category::getId, id)
                .eq(Category::getUserId, userId)
                .one();
        if (category == null) {
            throw new BusinessException("分类不存在或无权限删除！");
        }
        FinanceTransactions financeTransactions = QueryChain.of(financeTransactionsMapper)
                .select(FinanceTransactions::getId)
                .eq(FinanceTransactions::getUserId, userId)
                .eq(FinanceTransactions::getCategoryId, id)
                .one();
        if (financeTransactions != null) {
            throw new BusinessException("该分类已被账单引用，无法删除！");
        }
        Budget budget = QueryChain.of(budgetMapper)
                .select(Budget::getId)
                .eq(Budget::getUserId, userId)
                .eq(Budget::getCategoryId, id)
                .one();
        if (budget != null) {
            throw new BusinessException("该分类已被预算引用，无法删除！");
        }
        return removeById(id);
    }
}
