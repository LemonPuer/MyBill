package org.lemon.controller;

import org.lemon.entity.common.ApiResp;
import org.lemon.entity.resp.SimpleEnumVO;
import org.lemon.enumeration.IBaseEnum;
import org.reflections.Reflections;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


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
        Class<IBaseEnum> baseEnumClass = IBaseEnum.class;
        Reflections reflections = new Reflections(baseEnumClass);
        Set<Class<? extends IBaseEnum>> subTypes = reflections.getSubTypesOf(baseEnumClass);
        Map<String, List<SimpleEnumVO>> result = new HashMap<>(subTypes.size());
        for (Class<? extends IBaseEnum> enumClass : subTypes) {
            if (!enumClass.isEnum()) {
                continue;
            }
            Object[] enumConstants = enumClass.getEnumConstants();
            List<SimpleEnumVO> vos = new ArrayList<>();
            for (Object constant : enumConstants) {
                IBaseEnum enumInstance = (IBaseEnum) constant;
                vos.add(new SimpleEnumVO(enumInstance.getKey(), enumInstance.getValue()));
            }
            result.put(enumClass.getSimpleName().replace("Enum", ""), vos);
        }
        return ApiResp.ok(result);
    }
}
