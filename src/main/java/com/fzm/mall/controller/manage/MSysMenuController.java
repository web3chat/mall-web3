package com.fzm.mall.controller.manage;

import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.enums.FileEnum;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.common.PageVO;
import com.fzm.mall.entity.dataobject.SysMenuDO;
import com.fzm.mall.entity.queryobject.back.MSysMenuQO;
import com.fzm.mall.entity.requestobject.back.MMenuIdRO;
import com.fzm.mall.entity.requestobject.back.MMenuStatusRO;
import com.fzm.mall.entity.requestobject.back.MSysMenuRO;
import com.fzm.mall.entity.viewobject.back.MSysMenuVO;
import com.fzm.mall.service.SysMenuService;
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

@Tag(name = "系统-菜单")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/m/sys/menu", produces = MediaType.APPLICATION_JSON_VALUE)
public class MSysMenuController {
    private final SysMenuService sysMenuService;
    private final FileComponent fileComponent;

    @Operation(summary = "分页查询")
    @PostMapping("/page")
    public ResponseVO<PageVO<MSysMenuVO>> page(@RequestBody MSysMenuQO pageQO) {
        PageInfo<SysMenuDO> pageInfo = sysMenuService.pageMenu(pageQO);
        return PageVOUtils.pageVO(pageInfo, MSysMenuVO.class);
    }

    @Operation(summary = "详情")
    @Parameter(name = "menuId", description = "编号", in = ParameterIn.QUERY)
    @GetMapping("/detail")
    public ResponseVO<MSysMenuVO> detail(@RequestParam("menuId") Integer menuId) {
        SysMenuDO menuDO = sysMenuService.getById(menuId);
        MSysMenuVO menuVO = BeanCopierUtils.copy(menuDO, MSysMenuVO.class);

        return ResponseUtils.success(menuVO);
    }

    @Operation(summary = "新增/编辑")
    @PostMapping("/add-update")
    public ResponseVO<Object> addUpdate(@RequestBody MSysMenuRO menuRO) {
        String title = menuRO.getTitle();
        AssertUtils.isNotBlank(title, "title为空");
        AssertUtils.isTrue(title.length() <= 16, "title最多只能16个字");

        if (StringUtils.isBlank(menuRO.getCover())) {
            menuRO.setCover("");
        } else {
            fileComponent.verifyFileUrl(menuRO.getCover(), FileEnum.image, "cover只能使用图片");
        }

        AssertUtils.isNotNull(menuRO.getShowOrder(), "showOrder为空");

        SysMenuDO menuDO = BeanCopierUtils.copy(menuRO, SysMenuDO.class);

        sysMenuService.addOrUpdate(menuDO);

        return ResponseUtils.success();
    }

    @Operation(summary = "上下架")
    @PostMapping("/status")
    public ResponseVO<Object> status(@RequestBody MMenuStatusRO statusRO) {
        AssertUtils.isNotNull(CommonEnum.BoolEnum.exist(statusRO.getStatus()), "status错误");
        AssertUtils.isNotNull(statusRO.getMenuId(), "menuId错误");

        SysMenuDO menuDO = BeanCopierUtils.copy(statusRO, SysMenuDO.class);

        sysMenuService.updateById(menuDO);

        return ResponseUtils.success();
    }

    @Operation(summary = "删除")
    @PostMapping("/delete")
    public ResponseVO<Object> delete(@RequestBody MMenuIdRO idRO) {
        ParamsUtils.positiveInteger(idRO.getMenuId());

        sysMenuService.deleteById(idRO.getMenuId());

        return ResponseUtils.success();
    }
}
