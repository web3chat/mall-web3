package com.fzm.mall.controller.manage;

import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.common.PageVO;
import com.fzm.mall.entity.dataobject.UserAssetTokenDO;
import com.fzm.mall.entity.queryobject.back.MUserAssetPageQO;
import com.fzm.mall.entity.viewobject.back.MUserAssetTokenVO;
import com.fzm.mall.service.UserAssetService;
import com.fzm.mall.util.PageVOUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户-资产")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/m/user/asset", produces = MediaType.APPLICATION_JSON_VALUE)
public class MUserAssetController {
    private final UserAssetService userAssetService;

    @Operation(summary = "分页查询")
    @PostMapping("/page")
    public ResponseVO<PageVO<MUserAssetTokenVO>> page(@RequestBody MUserAssetPageQO pageQO) {
        PageInfo<UserAssetTokenDO> pageInfo = userAssetService.page(pageQO);
        return PageVOUtils.pageVO(pageInfo, MUserAssetTokenVO.class);
    }


}
