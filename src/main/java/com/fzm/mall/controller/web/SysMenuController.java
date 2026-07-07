package com.fzm.mall.controller.web;

import com.fzm.mall.annotation.UnAuthorization;
import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.dataobject.SysMenuDO;
import com.fzm.mall.entity.queryobject.back.MSysMenuQO;
import com.fzm.mall.entity.viewobject.SysMenuVO;
import com.fzm.mall.service.SysMenuService;
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

@Tag(name = "系统-菜单")
@RestController
@UnAuthorization
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/v/sys/menu", produces = MediaType.APPLICATION_JSON_VALUE)
public class SysMenuController {
    private final SysMenuService sysMenuService;

    @Operation(summary = "列表")
    @GetMapping("/list")
    public ResponseVO<List<SysMenuVO>> list() {
        MSysMenuQO pageQO = new MSysMenuQO();
        pageQO.setStatus(CommonEnum.BoolEnum.YES.getStatus());

        List<SysMenuDO> menuDOS = sysMenuService.listMenu(pageQO);

        List<SysMenuVO> noticeVOS = BeanCopierUtils.copyList(menuDOS, SysMenuVO.class);

        return ResponseUtils.success(noticeVOS);
    }

}
