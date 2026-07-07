package com.fzm.mall.util;

import java.util.List;

/**
 * 分页工具类
 */
public class PageUtils {
    /**
     * 第一页
     */
    private static final int first_page = 1;
    /**
     * 默认条目数
     */
    private static final int size_default = 100;
    /**
     * 最小条目数
     */
    private static final int size_min = 1;
    /**
     * 最大条目数
     */
    private static final int size_max = 2000;

    private PageUtils() {
    }

    /**
     * 页号
     *
     * @param page 页号
     * @return 页号
     */
    public static int page(Integer page) {
        return page == null ? first_page : page < first_page ? first_page : page;
    }

    /**
     * 条目数
     *
     * @param size 条目数
     * @return 条目数
     */
    public static int size(Integer size) {
        return size == null ? size_default : size < size_min ? size_default : size > size_max ? size_max : size;
    }

    /**
     * 开始位置
     *
     * @param pageNumber 当前页码
     * @param pageSize   每页条目数
     * @return 开始位置 start
     */
    public static int start(int pageNumber, int pageSize) {
        if (pageNumber < first_page) {
            pageNumber = first_page;
        }

        if (pageSize < 1) {
            pageSize = 0;
        }

        return (pageNumber - first_page) * pageSize;
    }

    /**
     * 结束位置
     *
     * @param pageNumber 当前页码
     * @param pageSize   每页条目数
     * @return 结束位置 end
     */
    public static int end(int pageNumber, int pageSize) {
        int start = start(pageNumber, pageSize);
        return getEndByStart(start, pageSize);
    }

    /**
     * 总页数
     *
     * @param totalSize 总条目数
     * @param pageSize  每页条目数
     * @return 总页数 int
     */
    public static long totalPage(long totalSize, int pageSize) {
        if (pageSize == 0) {
            return 0;
        }
        return totalSize % pageSize == 0 ? (totalSize / pageSize) : (totalSize / pageSize + 1);
    }

    /**
     * 是否有下一页
     *
     * @param pageNumber 当前页码
     * @return true :有，false:无
     */
    public static boolean hasPrePage(int pageNumber) {
        return pageNumber > first_page;
    }

    /**
     * 是否有下一页
     *
     * @param pageNumber 当前页码
     * @param totalPage  总页数
     * @return true :有，false:无
     */
    public static boolean hasNextPage(int pageNumber, long totalPage) {
        return pageNumber < totalPage;
    }

    /**
     * 当前页条目数
     *
     * @param data 当前页数据
     * @return 条目数 int
     */
    public static int dataSize(List<?> data) {
        return data == null ? 0 : data.size();
    }

    /**
     * 根据起始位置获取结束位置
     *
     * @param start    起始位置
     * @param pageSize 每页条目数
     * @return 结束位置
     */
    private static int getEndByStart(int start, int pageSize) {
        if (pageSize < 1) {
            pageSize = 0;
        }
        return start + pageSize;
    }

}
