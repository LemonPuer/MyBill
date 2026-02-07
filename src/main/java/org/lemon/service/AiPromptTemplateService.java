package org.lemon.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.AiPromptTemplate;
import org.lemon.entity.dto.UserPromptInfoDTO;
import org.lemon.mapper.AiPromptTemplateMapper;
import org.lemon.mapper.CategoryMapper;
import org.lemon.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI提示词模板表 服务层实现。
 *
 * @author Lemon
 * @since 2026-02-07
 */
@Slf4j
@Service
@AllArgsConstructor
public class AiPromptTemplateService extends ServiceImpl<AiPromptTemplateMapper, AiPromptTemplate> {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    public Map<Integer, String> getPromptDetail(String theme, List<UserPromptInfoDTO> dtoList) {
        if (CollUtil.isEmpty(dtoList)) {
            return Collections.emptyMap();
        }
        AiPromptTemplate template = queryChain().eq(AiPromptTemplate::getTheme, theme)
                .eq(AiPromptTemplate::getStatus, 1).one();
        if (template == null) {
            log.error("提示词模板不存在或未启用：{}", theme);
            return Collections.emptyMap();
        }

        // 解析模板中的占位符
        Set<String> placeholders = extractPlaceholders(template.getContent());
        log.info("提取到的占位符: {}", placeholders);

        Map<Integer, Map<String, Object>> userInfoMap = new HashMap<>(dtoList.size());

        // 遍历所有DTO对象，提取属性值
        for (UserPromptInfoDTO dto : dtoList) {
            userInfoMap.put(dto.getUserId(), extractDtoValues(dto, placeholders));
        }
        Map<Integer, String> result = new HashMap<>();
        userInfoMap.forEach((userId, values) -> {
            // 替换模板中的占位符
            String filledContent = replacePlaceholders(new String(template.getContent()), values);
            result.put(template.getVersion(), filledContent);
        });
        return result;
    }

    /**
     * 从模板内容中提取 {xxx} 形式的占位符
     *
     * @param content 模板内容
     * @return 占位符集合
     */
    private Set<String> extractPlaceholders(String content) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);
        Set<String> placeholders = new HashSet<>();

        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }

        return placeholders;
    }

    /**
     * 通过反射从DTO对象中提取指定属性的值
     *
     * @param dto          DTO对象
     * @param placeholders 需要提取的占位符
     * @return 存储结果的Map
     */
    private Map<String, Object> extractDtoValues(Object dto, Set<String> placeholders) {
        // 构建占位符到值的映射
        Map<String, Object> result = new HashMap<>();
        if (dto == null || placeholders.isEmpty()) {
            return result;
        }

        Class<?> clazz = dto.getClass();

        for (String placeholder : placeholders) {
            try {
                // 尝试直接获取属性
                java.lang.reflect.Field field = getFieldRecursively(clazz, placeholder);
                if (field != null) {
                    field.setAccessible(true);
                    Object value = field.get(dto);
                    if (value != null) {
                        result.put(placeholder, value);
                        log.debug("成功提取属性 {}: {}", placeholder, value);
                    }
                }
            } catch (Exception e) {
                log.warn("提取属性 {} 时发生异常: {}", placeholder, e.getMessage());
            }
        }
        return result;
    }

    /**
     * 递归查找字段（包括父类）
     *
     * @param clazz     类
     * @param fieldName 字段名
     * @return Field对象，如果找不到返回null
     */
    private java.lang.reflect.Field getFieldRecursively(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // 如果当前类没有该字段，尝试父类
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                return getFieldRecursively(superClass, fieldName);
            }
            return null;
        }
    }

    /**
     * 将占位符替换为实际值
     *
     * @param content 原始内容
     * @param values  占位符值映射
     * @return 替换后的内容
     */
    private String replacePlaceholders(String content, Map<String, Object> values) {
        String result = content;

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String valueStr = convertToString(entry.getValue());
            result = result.replace(placeholder, valueStr);
            log.debug("替换占位符 {} -> {}", placeholder, valueStr);
        }

        return result;
    }

    /**
     * 将对象转换为字符串表示
     *
     * @param obj 对象
     * @return 字符串
     */
    private String convertToString(Object obj) {
        if (obj == null) {
            return "";
        }
        return JSONUtil.toJsonStr(obj);
    }
}
