package com.fzm.mall.entity.common;

import com.fzm.mall.util.PageUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页类
 *
 * @param <T> 分页数据类型
 */
@Data
@NoArgsConstructor
public class PageVO<T> {
    /**
     * 当前页码
     */
    private int pageNumber;
    /**
     * 每页条目数
     */
    private int pageSize;
    /**
     * 总页数
     */
    private long totalPage;
    /**
     * 总条目数
     */
    private long totalSize;
    /**
     * 是否有上一页
     */
    private boolean hasPrePage;
    /**
     * 是否有下一页
     */
    private boolean hasNextPage;
    /**
     * 开始行
     */
    private int start;
    /**
     * 结束行
     */
    private int end;
    /**
     * 当前页条目数
     */
    private int dataSize;
    /**
     * 当前页数据
     */
    private List<T> data;

    /**
     * 构造
     *
     * @param pageNumber 当前页码
     * @param pageSize   每页条目数
     * @param totalSize  总条目数
     * @param data       当前页数据
     */
    public PageVO(int pageNumber, int pageSize, long totalSize, List<T> data) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalPage = PageUtils.totalPage(totalSize, pageSize);
        this.totalSize = totalSize;
        this.hasPrePage = PageUtils.hasPrePage(pageNumber);
        this.hasNextPage = PageUtils.hasNextPage(pageNumber, totalPage);
        this.start = PageUtils.start(pageNumber, pageSize);
        this.end = PageUtils.end(pageNumber, pageSize);
        this.data = data;
        this.dataSize = PageUtils.dataSize(data);
    }

    /**
     * 静态构造
     *
     * @param <T>        数据类型
     * @param pageNumber 当前页码
     * @param pageSize   每页条目数
     * @param totalSize  总条目数
     * @param data       当前页数据
     * @return 分页类 page vo
     */
    public static <T> PageVO<T> of(int pageNumber, int pageSize, long totalSize, List<T> data) {
        return new PageVO<>(pageNumber, pageSize, totalSize, data);
    }
}
