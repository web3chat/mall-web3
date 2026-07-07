package com.fzm.mall.util;

import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.common.PageVO;
import com.github.pagehelper.PageInfo;

import java.util.List;

public class PageVOUtils {
    public static <T> ResponseVO<PageVO<T>> pageVO(PageInfo<?> pageInfo, Class<T> targetClass) {
        List<T> vos = BeanCopierUtils.copyList(pageInfo.getList(), targetClass);
        return pageVO(pageInfo, vos);
    }

    public static <T> ResponseVO<PageVO<T>> pageVO(PageInfo<?> pageInfo, List<T> vos) {
        long total = pageInfo.getTotal();

        PageVO<T> pageVO = PageVO.of(pageInfo.getPageNum(), pageInfo.getPageSize(), total, vos);
        return ResponseUtils.success(pageVO);
    }
}
