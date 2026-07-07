package com.fzm.mall.controller.manage;

import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.common.PageVO;
import com.fzm.mall.entity.dataobject.UserDO;
import com.fzm.mall.entity.queryobject.back.MUserPageQO;
import com.fzm.mall.entity.requestobject.back.MAddressParentRO;
import com.fzm.mall.entity.viewobject.back.MUserVO;
import com.fzm.mall.service.UserService;
import com.fzm.mall.util.BeanCopierUtils;
import com.fzm.mall.util.PageVOUtils;
import com.fzm.mall.util.ParamsUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户-前台用户")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/m/user/front", produces = MediaType.APPLICATION_JSON_VALUE)
public class MUserFrontController {
    private final UserService userService;

    @Operation(summary = "分页查询")
    @PostMapping("/page")
    public ResponseVO<PageVO<MUserVO>> page(@RequestBody MUserPageQO pageQO) {
        PageInfo<UserDO> pageInfo = userService.page(pageQO);
        return PageVOUtils.pageVO(pageInfo, MUserVO.class);
    }

    @Operation(summary = "指定地址的信息")
    @Parameter(name = "address", description = "地址", in = ParameterIn.QUERY)
    @GetMapping("/info")
    public ResponseVO<MUserVO> info(@RequestParam("address") String address) {
        address = ParamsUtils.address(address);
        UserDO userDO = userService.getByAddress(address);

        MUserVO userVO = BeanCopierUtils.copy(userDO, MUserVO.class);
        return ResponseUtils.success(userVO);
    }

    @Operation(summary = "修改邀请地址")
    @PostMapping("/parent-address")
    public ResponseVO<Object> updateParentAddress(@RequestBody MAddressParentRO parentRO) {
        String address = ParamsUtils.address(parentRO.getAddress());
        String parentAddress = ParamsUtils.address(parentRO.getParentAddress());
        userService.updateParentAddressByAddress(address, parentAddress, false);
        return ResponseUtils.success();
    }
}
