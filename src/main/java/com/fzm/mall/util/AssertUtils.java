package com.fzm.mall.util;

import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.constant.response.ValidateException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * 断言某些对象或值是否符合规定，否则抛出异常
 */
public class AssertUtils {
    private AssertUtils() {
    }

    /**
     * 断言为true，如果为false，抛出异常
     *
     * @param expression 表达式
     * @param source     响应
     */
    public static void isTrue(boolean expression, ResponseEnum source) {
        if (!expression) {
            throw new ValidateException(source);
        }
    }

    /**
     * 断言为true，如果为false，抛出异常
     *
     * @param expression 表达式
     * @param message    提示信息
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new ValidateException(message);
        }
    }

    /**
     * 断言为true，如果为false，抛出自定义异常
     *
     * @param <X>        异常类型
     * @param expression 表达式
     * @param supplier   指定断言不通过时抛出的异常
     * @throws X 异常
     */
    public static <X extends Throwable> void isTrue(boolean expression, Supplier<? extends X> supplier) throws X {
        if (!expression) {
            throw supplier.get();
        }
    }

    /**
     * 断言为false，如果为true，抛出异常
     *
     * @param expression 表达式
     * @param source     响应
     */
    public static void isFalse(boolean expression, ResponseEnum source) {
        if (expression) {
            throw new ValidateException(source);
        }
    }

    /**
     * 断言为false，如果为true，抛出异常
     *
     * @param expression 表达式
     * @param message    提示信息
     */
    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throw new ValidateException(message);
        }
    }

    /**
     * 断言为false，如果为true，抛出自定义异常
     *
     * @param <X>        异常类型
     * @param expression 表达式
     * @param supplier   指定断言不通过时抛出的异常
     * @throws X 异常
     */
    public static <X extends Throwable> void isFalse(boolean expression, Supplier<? extends X> supplier) throws X {
        if (expression) {
            throw supplier.get();
        }
    }

    /**
     * 断言为null，如果不为null，抛出异常
     *
     * @param object 被检查的对象
     * @param source 响应
     */
    public static void isNull(Object object, ResponseEnum source) {
        if (object != null) {
            throw new ValidateException(source);
        }
    }

    /**
     * 断言为null，如果不为null，抛出异常
     *
     * @param object  被检查的对象
     * @param message 提示信息
     */
    public static void isNull(Object object, String message) {
        if (object != null) {
            throw new ValidateException(message);
        }
    }

    /**
     * 断言为null，如果不为null，抛出自定义异常
     *
     * @param <X>      异常类型
     * @param object   被检查的对象
     * @param supplier 指定断言不通过时抛出的异常
     * @throws X 异常
     */
    public static <X extends Throwable> void isNull(Object object, Supplier<? extends X> supplier) throws X {
        if (object != null) {
            throw supplier.get();
        }
    }

    /**
     * 断言为不null，如果为null，抛出异常
     *
     * @param object 被检查的对象
     * @param source 响应
     */
    public static void isNotNull(Object object, ResponseEnum source) {
        if (object == null) {
            throw new ValidateException(source);
        }
    }

    /**
     * 断言为不null，如果为null，抛出异常
     *
     * @param object  被检查的对象
     * @param message 提示信息
     */
    public static void isNotNull(Object object, String message) {
        if (object == null) {
            throw new ValidateException(message);
        }
    }

    /**
     * 断言为不null，如果为null，抛出自定义异常
     *
     * @param <X>      异常类型
     * @param object   被检查的对象
     * @param supplier 指定断言不通过时抛出的异常
     * @throws X 异常
     */
    public static <X extends Throwable> void isNotNull(Object object, Supplier<? extends X> supplier) throws X {
        if (object == null) {
            throw supplier.get();
        }
    }

    /**
     * 断言为blank，如果不为blank，抛出异常
     *
     * @param text   被检查的内容
     * @param source 响应
     */
    public static void isBlank(String text, ResponseEnum source) {
        if (StringUtils.isNotBlank(text)) {
            throw new ValidateException(source);
        }
    }

    /**
     * 断言为blank，如果不为blank，抛出异常
     *
     * @param text    被检查的内容
     * @param message 提示信息
     */
    public static void isBlank(String text, String message) {
        if (StringUtils.isNotBlank(text)) {
            throw new ValidateException(message);
        }
    }

    /**
     * 断言为blank，如果不为blank，抛出自定义异常
     *
     * @param <X>      异常类型
     * @param text     被检查的内容
     * @param supplier 指定断言不通过时抛出的异常
     * @throws X 异常
     */
    public static <X extends Throwable> void isBlank(String text, Supplier<? extends X> supplier) throws X {
        if (StringUtils.isNotBlank(text)) {
            throw supplier.get();
        }
    }

    /**
     * 断言不为blank，如果为blank，抛出异常
     *
     * @param text   被检查的内容
     * @param source 响应
     */
    public static void isNotBlank(String text, ResponseEnum source) {
        if (StringUtils.isBlank(text)) {
            throw new ValidateException(source);
        }
    }

    /**
     * 断言不为blank，如果为blank，抛出异常
     *
     * @param text    被检查的内容
     * @param message 提示信息
     */
    public static void isNotBlank(String text, String message) {
        if (StringUtils.isBlank(text)) {
            throw new ValidateException(message);
        }
    }

    /**
     * 断言不为blank，如果为blank，抛出自定义异常
     *
     * @param <X>      异常类型
     * @param text     被检查的内容
     * @param supplier 指定断言不通过时抛出的异常
     * @throws X 异常
     */
    public static <X extends Throwable> void isNotBlank(String text, Supplier<? extends X> supplier) throws X {
        if (StringUtils.isBlank(text)) {
            throw supplier.get();
        }
    }

    /**
     * 断言textToSearch包含substring，如果不包含，抛出异常
     *
     * @param textToSearch 被搜索的字符串
     * @param substring    被检查的子串
     * @param source       响应
     */
    public static void isContains(String textToSearch, String substring, ResponseEnum source) {
        if (!StringUtils.contains(textToSearch, substring)) {
            throw new ValidateException(source);
        }
    }

    /**
     * 断言textToSearch包含substring，如果不包含，抛出异常
     *
     * @param textToSearch 被搜索的字符串
     * @param substring    被检查的子串
     * @param message      提示信息
     */
    public static void isContains(String textToSearch, String substring, String message) {
        if (!StringUtils.contains(textToSearch, substring)) {
            throw new ValidateException(message);
        }
    }

    /**
     * 断言textToSearch包含substring，如果不包含，抛出自定义异常
     *
     * @param <X>          异常类型
     * @param textToSearch 被搜索的字符串
     * @param substring    被检查的子串
     * @param supplier     指定断言不通过时抛出的异常
     * @throws X 异常
     */
    public static <X extends Throwable> void isContains(String textToSearch, String substring, Supplier<? extends X> supplier) throws X {
        if (!StringUtils.contains(textToSearch, substring)) {
            throw supplier.get();
        }
    }

    /**
     * 断言textToSearch不包含substring，如果包含，抛出异常
     *
     * @param textToSearch 被搜索的字符串
     * @param substring    被检查的子串
     * @param source       响应
     */
    public static void isNotContains(String textToSearch, String substring, ResponseEnum source) {
        if (StringUtils.contains(textToSearch, substring)) {
            throw new ValidateException(source);
        }
    }

    /**
     * 断言textToSearch不包含substring，如果包含，抛出异常
     *
     * @param textToSearch 被搜索的字符串
     * @param substring    被检查的子串
     * @param message      提示信息
     */
    public static void isNotContains(String textToSearch, String substring, String message) {
        if (StringUtils.contains(textToSearch, substring)) {
            throw new ValidateException(message);
        }
    }

    /**
     * 断言textToSearch不包含substring，如果包含，抛出自定义异常
     *
     * @param <X>          异常类型
     * @param textToSearch 被搜索的字符串
     * @param substring    被检查的子串
     * @param supplier     指定断言不通过时抛出的异常
     * @throws X 异常
     */
    public static <X extends Throwable> void isNotContains(String textToSearch, String substring, Supplier<? extends X> supplier) throws X {
        if (StringUtils.contains(textToSearch, substring)) {
            throw supplier.get();
        }
    }

    /**
     * 断言为empty，如果不为empty，抛出异常
     *
     * @param array  被检查的数组
     * @param source 响应
     */
    public static void isEmpty(Object[] array, ResponseEnum source) {
        if (ArrayUtils.isNotEmpty(array)) {
            throw new ValidateException(source);
        }
    }

    /**
     * 断言为empty，如果不为empty，抛出异常
     *
     * @param array   被检查的数组
     * @param message 提示信息
     */
    public static void isEmpty(Object[] array, String message) {
        if (ArrayUtils.isNotEmpty(array)) {
            throw new ValidateException(message);
        }
    }

    /**
     * 断言为empty，如果不为empty，抛出自定义异常
     *
     * @param <X>      异常类型
     * @param array    被检查的数组
     * @param supplier 指定断言不通过时抛出的异常
     * @throws X 异常
     */
    public static <X extends Throwable> void isEmpty(Object[] array, Supplier<? extends X> supplier) throws X {
        if (ArrayUtils.isNotEmpty(array)) {
            throw supplier.get();
        }
    }

    /**
     * 断言不为empty，如果为empty，抛出异常
     *
     * @param array  被检查的数组
     * @param source 响应
     */
    public static void isNotEmpty(Object[] array, ResponseEnum source) {
        if (ArrayUtils.isEmpty(array)) {
            throw new ValidateException(source);
        }
    }

    /**
     * 断言不为empty，如果为empty，抛出异常
     *
     * @param array   被检查的数组
     * @param message 提示信息
     */
    public static void isNotEmpty(Object[] array, String message) {
        if (ArrayUtils.isEmpty(array)) {
            throw new ValidateException(message);
        }
    }

    /**
     * 断言不为empty，如果为empty，抛出自定义异常
     *
     * @param <X>      异常类型
     * @param array    被检查的数组
     * @param supplier 指定断言不通过时抛出的异常
     * @throws X 异常
     */
    public static <X extends Throwable> void isNotEmpty(Object[] array, Supplier<? extends X> supplier) throws X {
        if (ArrayUtils.isEmpty(array)) {
            throw supplier.get();
        }
    }


    /**
     * 断言为empty，如果不为empty，抛出异常
     *
     * @param collection 被检查的集合
     * @param source     响应
     */
    public static void isEmpty(Collection<?> collection, ResponseEnum source) {
        if (CollectionUtils.isNotEmpty(collection)) {
            throw new ValidateException(source);
        }
    }

    /**
     * 断言为empty，如果不为empty，抛出异常
     *
     * @param collection 被检查的集合
     * @param message    提示信息
     */
    public static void isEmpty(Collection<?> collection, String message) {
        if (CollectionUtils.isNotEmpty(collection)) {
            throw new ValidateException(message);
        }
    }

    /**
     * 断言为empty，如果不为empty，抛出自定义异常
     *
     * @param <X>        异常类型
     * @param collection 被检查的集合
     * @param supplier   指定断言不通过时抛出的异常
     * @throws X 异常
     */
    public static <X extends Throwable> void isEmpty(Collection<?> collection, Supplier<? extends X> supplier) throws X {
        if (CollectionUtils.isNotEmpty(collection)) {
            throw supplier.get();
        }
    }

    /**
     * 断言不为empty，如果为empty，抛出异常
     *
     * @param collection 被检查的集合
     * @param source     响应
     */
    public static void isNotEmpty(Collection<?> collection, ResponseEnum source) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new ValidateException(source);
        }
    }

    /**
     * 断言不为empty，如果为empty，抛出异常
     *
     * @param collection 被检查的集合
     * @param message    提示信息
     */
    public static void isNotEmpty(Collection<?> collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new ValidateException(message);
        }
    }

    /**
     * 断言不为empty，如果为empty，抛出自定义异常
     *
     * @param <X>        异常类型
     * @param collection 被检查的集合
     * @param supplier   指定断言不通过时抛出的异常
     * @throws X 异常
     */
    public static <X extends Throwable> void isNotEmpty(Collection<?> collection, Supplier<? extends X> supplier) throws X {
        if (CollectionUtils.isEmpty(collection)) {
            throw supplier.get();
        }
    }
}
