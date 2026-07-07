package com.fzm.mall.controller.manage;

import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.enums.FileEnum;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.common.PageVO;
import com.fzm.mall.entity.dataobject.SysBannerDO;
import com.fzm.mall.entity.queryobject.back.MSysBannerQO;
import com.fzm.mall.entity.requestobject.LongIdRO;
import com.fzm.mall.entity.requestobject.back.MLongIdStatusRO;
import com.fzm.mall.entity.requestobject.back.MSysBannerRO;
import com.fzm.mall.entity.viewobject.back.MSysBannerVO;
import com.fzm.mall.service.SysBannerService;
import com.fzm.mall.third.component.FileComponent;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.BeanCopierUtils;
import com.fzm.mall.util.PageVOUtils;
import com.fzm.mall.util.ParamsUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Tag(name = "系统-Banner")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/m/sys/banner", produces = MediaType.APPLICATION_JSON_VALUE)
public class MSysBannerController {
    private final SysBannerService sysBannerService;
    private final FileComponent fileComponent;

    @Operation(summary = "分页查询")
    @PostMapping("/page")
    public ResponseVO<PageVO<MSysBannerVO>> page(@RequestBody MSysBannerQO pageQO) {
        PageInfo<SysBannerDO> pageInfo = sysBannerService.pageBanner(pageQO);
        return PageVOUtils.pageVO(pageInfo, MSysBannerVO.class);
    }

    @Operation(summary = "详情")
    @Parameter(name = "id", description = "编号", in = ParameterIn.QUERY)
    @GetMapping("/detail")
    public ResponseVO<MSysBannerVO> detail(@RequestParam("id") Long id) {
        SysBannerDO bannerDO = sysBannerService.getById(id);
        MSysBannerVO bannerVO = BeanCopierUtils.copy(bannerDO, MSysBannerVO.class);

        return ResponseUtils.success(bannerVO);
    }

    @Operation(summary = "新增/编辑")
    @PostMapping("/add-update")
    public ResponseVO<Object> addUpdate(@RequestBody MSysBannerRO bannerRO) {
        String title = bannerRO.getTitle();
        AssertUtils.isNotBlank(title, "title为空");
        AssertUtils.isTrue(title.length() <= 32, "title最多32个字");

        fileComponent.verifyFileUrl(bannerRO.getCover(), FileEnum.image, "cover只能使用图片");

        String target = bannerRO.getTarget();
        if (StringUtils.isBlank(target)) {
            bannerRO.setTarget("");
        } else {
            AssertUtils.isTrue(target.length() <= 1024, "target过长");
        }

        AssertUtils.isNotNull(bannerRO.getShowOrder(), "showOrder为空");

        AssertUtils.isNotNull(bannerRO.getStartTime(), "startTime为空");
        AssertUtils.isNotNull(bannerRO.getEndTime(), "endTime为空");

        SysBannerDO bannerDO = BeanCopierUtils.copy(bannerRO, SysBannerDO.class);
        sysBannerService.addOrUpdate(bannerDO);

        return ResponseUtils.success();
    }

    @Operation(summary = "上下架")
    @PostMapping("/status")
    public ResponseVO<Object> status(@RequestBody MLongIdStatusRO statusRO) {
        AssertUtils.isNotNull(CommonEnum.BoolEnum.exist(statusRO.getStatus()), "status错误");
        AssertUtils.isNotNull(statusRO.getId(), "id错误");

        SysBannerDO bannerDO = BeanCopierUtils.copy(statusRO, SysBannerDO.class);

        sysBannerService.updateById(bannerDO);

        return ResponseUtils.success();
    }

    @Operation(summary = "删除")
    @PostMapping("/delete")
    public ResponseVO<Object> delete(@RequestBody LongIdRO idRO) {
        ParamsUtils.positiveLong(idRO.getId());

        sysBannerService.deleteById(idRO.getId());

        return ResponseUtils.success();
    }
}
