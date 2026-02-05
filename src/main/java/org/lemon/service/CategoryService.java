package org.lemon.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.lemon.entity.Category;
import org.lemon.entity.common.BasePage;
import org.lemon.entity.req.CategorySaveReq;
import org.lemon.entity.resp.CategoryVO;
import org.lemon.mapper.CategoryMapper;
import org.lemon.utils.UserUtil;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 收支分类表 服务层实现。
 *
 * @author Lemon
 * @since 2025-05-17
 */
@Service
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {

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
}
