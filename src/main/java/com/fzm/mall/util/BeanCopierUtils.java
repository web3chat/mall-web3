package com.fzm.mall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;

import java.util.Collections;
import java.util.List;

/**
 * bean复制工具类
 */
@Slf4j
public class BeanCopierUtils {

    private static final ModelMapper modelMapper;

    static {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                // 设置属性匹配策略为严格模式
                .setMatchingStrategy(MatchingStrategies.STRICT)
                // 启用字段级别的匹配（而不仅仅是getter/setter方法）
                .setFieldMatchingEnabled(true)
                // 设置字段的访问级别，控制ModelMapper可以访问什么权限的字段
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);
    }

    private BeanCopierUtils() {
    }

    /**
     * 复制
     *
     * @param source 源类
     * @param target 目标类
     */
    public static void copy(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        try {
            modelMapper.map(source, target);
        } catch (Exception e) {
            log.error("对象映射失败：{}", e.getMessage());
        }

    }

    /**
     * 复制
     *
     * @param <T>         目标类型
     * @param source      源类
     * @param targetClass 目标类型
     * @return 目标类 t
     */
    public static <T> T copy(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            return modelMapper.map(source, targetClass);
        } catch (Exception e) {
            log.error("对象映射失败：{}", e.getMessage());
        }
        return null;
    }

    /**
     * 使用转换器复制数组
     *
     * @param <T>         目标类型
     * @param sourceList  源数组
     * @param targetClass 目标数组类型
     * @return 目标数组 list
     */
    public static <T> List<T> copyList(List<?> sourceList, Class<T> targetClass) {
        if (CollectionUtils.isEmpty(sourceList)) {
            return Collections.emptyList();
        }

        return sourceList.stream().map(o -> copy(o, targetClass)).toList();
    }
}
