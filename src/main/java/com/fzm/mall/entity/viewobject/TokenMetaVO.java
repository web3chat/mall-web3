package com.fzm.mall.entity.viewobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TokenMetaVO {
    @Schema(description = "tokenId")
    private Long tokenId;

    @Schema(description = "名称")
    private String name;
    @Schema(description = "token名称，最多32个字符")
    private String tokenName;

    @Schema(description = "封面")
    private String cover;

    @Schema(description = "提货类型，0不可提货，1可以提货")
    private Integer expressType;
    @Schema(description = "是否盲盒，0不是，1是")
    private Integer blindBoxType;
}
