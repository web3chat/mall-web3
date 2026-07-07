package com.fzm.mall.entity.dataobject;

import lombok.Data;

@Data
public class AirdropWhiteDO {
    private Long id;
    private Long infoId;
    private String address;
    private Integer metaType;
    private String metaId;
    private Integer num;

    private String tokenIdJson;
}
