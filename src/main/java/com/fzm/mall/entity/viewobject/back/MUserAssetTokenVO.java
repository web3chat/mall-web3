package com.fzm.mall.entity.viewobject.back;

import com.fzm.mall.entity.viewobject.UserAssetTokenVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MUserAssetTokenVO extends UserAssetTokenVO {
    @Schema(description = "地址")
    private String address;
}
