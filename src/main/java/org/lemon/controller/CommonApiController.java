package org.lemon.controller;

import org.lemon.entity.common.ApiResp;
import org.lemon.entity.resp.SimpleEnumVO;
import org.lemon.enumeration.IBaseEnum;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;


/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/16 23:26:28
 */
@RestController
@RequestMapping("common")
public class CommonApiController {

    @PostMapping("getDicInfo")
    public ApiResp<Map<String, List<SimpleEnumVO>>> getDicInfo() {
        ServiceLoader<IBaseEnum> loader = ServiceLoader.load(IBaseEnum.class);
        Map<Class<?>, List<SimpleEnumVO>> enumMap = new HashMap<>();
        // 收集所有枚举实例并按类分组
        for (IBaseEnum enumInstance : loader) {
            Class<?> enumClass = enumInstance.getClass();
            if (!enumClass.isEnum()) {
                continue;
            }
            enumMap.computeIfAbsent(enumClass, k -> new ArrayList<>())
                    .add(new SimpleEnumVO(enumInstance.getKey(), enumInstance.getValue()));
        }
        // 转换为所需格式
        Map<String, List<SimpleEnumVO>> result = enumMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getSimpleName().replace("Enum", ""), Map.Entry::getValue));
        return ApiResp.ok(result);
    }
}
