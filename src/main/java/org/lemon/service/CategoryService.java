package org.lemon.service;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.lemon.entity.Category;
import org.lemon.mapper.CategoryMapper;
import org.lemon.service.CategoryService;
import org.springframework.stereotype.Service;

/**
 * 收支分类表 服务层实现。
 *
 * @author Lemon
 * @since 2025-05-17
 */
@Service
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {

}
