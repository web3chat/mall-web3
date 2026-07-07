package com.fzm.mall.entity.viewobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class GoodsDetailVO {
    private GoodsSpuVO spu;
    private SkuPropVO skuProp;
    private List<Value1VO> v1Skus;

    private MerchantVO merchant;
    private ChainContractVO contract;

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

        private List<Value2VO> v2Skus;
    }

    @Data
    public static class Value2VO {
        @Schema(description = "参数2")
        private String propValue2;

        private GoodsSkuVO sku;
    }
}
