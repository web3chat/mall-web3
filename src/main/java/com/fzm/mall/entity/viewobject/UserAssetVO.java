package com.fzm.mall.entity.viewobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UserAssetVO {
    private SkuPropVO skuProp;

    private List<Value1VO> v1Assets;

    @Data
    public static class SkuPropVO {
        @Schema(description = "标题1")
        private String title1;

        @Schema(description = "标题2")
        private String title2;
    }

    @Data
    public static class Value1VO {
        @Schema(description = "参数1")
        private String propValue1;

        private List<Value2VO> v2Assets;
    }

    @Data
    public static class Value2VO {
        @Schema(description = "商品编号")
        private String goodsId;
        @Schema(description = "skuId")
        private String skuId;

        @Schema(description = "参数2")
        private String propValue2;

        @Schema(description = "封面")
        private String cover;

        @Schema(description = "提货类型，0不可提货，1可以提货")
        private Integer expressType;
        @Schema(description = "是否盲盒，0不是，1是")
        private Integer blindBoxType;

        private List<AssetTokenVO> tokens;
    }

    @Data
    public static class AssetTokenVO {
        @Schema(description = "合约编号")
        private Integer ctId;
        @Schema(description = "tokenId")
        private Long tokenId;

        @Schema(description = "token名称，最多32个字符")
        private String tokenName;

        @Schema(description = "数量")
        private BigDecimal num;

        @Schema(description = "时间")
        private Long updateTime;
    }
}
