package org.lemon.enumeration;

import java.util.Arrays;
import java.util.Objects;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/11 21:54:04
 */
public interface IBaseEnum {

    /**
     * 获取枚举key
     *
     * @return
     */
    Integer getKey();

    /**
     * 获取枚举value
     *
     * @return
     */
    String getValue();

    /**
     * 通过key获取枚举value
     *
     * @param key
     * @return
     */
    static <T extends Enum<T> & IBaseEnum> String getValueByKey(Class<T> enumClass, Integer key) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> Objects.equals(e.getKey(), key))
                .findFirst()
                .map(IBaseEnum::getValue)
                .orElse("");
    }

    /**
     * 根据value获取key
     *
     * @param value
     * @return
     */
    static <T extends Enum<T> & IBaseEnum> Integer getKeyByValue(Class<T> enumClass, String value) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.getValue().equals(value))
                .findFirst()
                .map(IBaseEnum::getKey)
                .orElse(null);
    }

}
