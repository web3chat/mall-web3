package com.fzm.mall.third.chain.entity;

import lombok.Data;

@Data
public class TokenUri {
    /**
     * 名称
     */
    private String name;
    /**
     * 图片地址，宽度在320-1080之间，长宽比在1.91:1-4:5之间
     */
    private String image;
    /**
     * 描述
     */
    private String description;
    /**
     * 小数位数
     */
    private Integer decimals = 0;
    /**
     * 其他属性
     */
    private TokenUriProperties properties;
}
