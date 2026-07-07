package com.fzm.mall.entity.queryobject;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fzm.mall.entity.common.PageQO;
import com.fzm.mall.util.EscapeSqlDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VaguePageQO extends PageQO {
    @Schema(description = "模糊搜索")
    @JsonDeserialize(using = EscapeSqlDeserializer.StringDeserializer.class)
    private String vague;
}
