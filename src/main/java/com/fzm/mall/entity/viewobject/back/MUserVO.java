package com.fzm.mall.entity.viewobject.back;

import com.fzm.mall.entity.viewobject.UserVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MUserVO extends UserVO {
    @Schema(description = "注册IP")
    private String registerIp;

    @Schema(description = "最近登录IP")
    private String latestLoginIp;
}
