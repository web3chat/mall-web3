package com.fzm.mall.entity.viewobject.back;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MAuthPermissionTreeVO extends MAuthPermissionVO {
    private List<MAuthPermissionTreeVO> tree;
}
