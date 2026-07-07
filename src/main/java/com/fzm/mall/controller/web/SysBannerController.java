package com.fzm.mall.controller.web;

import com.fzm.mall.annotation.UnAuthorization;
import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.dataobject.SysBannerDO;
import com.fzm.mall.entity.viewobject.SysBannerVO;
import com.fzm.mall.service.SysBannerService;
import com.fzm.mall.util.BeanCopierUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "系统-Banner")
@RestController
@UnAuthorization
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/v/sys/banner", produces = MediaType.APPLICATION_JSON_VALUE)
public class SysBannerController {
    private final SysBannerService sysBannerService;

    @Operation(summary = "列表")
    @GetMapping("/list")
    public ResponseVO<List<SysBannerVO>> list() {
        List<SysBannerDO> dos = sysBannerService.listByStatus(CommonEnum.BoolEnum.YES);
        List<SysBannerVO> vos = BeanCopierUtils.copyList(dos, SysBannerVO.class);
        return ResponseUtils.success(vos);
    }
}
