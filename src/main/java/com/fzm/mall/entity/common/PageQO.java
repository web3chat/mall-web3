package com.fzm.mall.entity.common;

import com.fzm.mall.util.PageUtils;
import lombok.Setter;

@Setter
public class PageQO {
    /**
     * 当前页码
     */
    private Integer page;
    /**
     * 每页条目数
     */
    private Integer size;

    public Integer getPage() {
        return PageUtils.page(page);
    }

    public Integer getSize() {
        return PageUtils.size(size);
    }
}
