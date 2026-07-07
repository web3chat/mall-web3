package com.fzm.mall.controller.web;

import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.entity.dataobject.UserExpressDO;
import com.fzm.mall.entity.requestobject.LongIdRO;
import com.fzm.mall.entity.requestobject.UserExpressRO;
import com.fzm.mall.entity.viewobject.UserExpressVO;
import com.fzm.mall.service.UserExpressService;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.BeanCopierUtils;
import com.fzm.mall.util.ParamsUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户-收货地址")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/v/user/express", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserExpressController {
    private final UserExpressService userExpressService;

    @Operation(summary = "列表")
    @GetMapping("/list")
    public ResponseVO<List<UserExpressVO>> list() {
        List<UserExpressDO> dos = userExpressService.listByAddress(ThreadInfo.getInfo().getAddress());
        List<UserExpressVO> vos = BeanCopierUtils.copyList(dos, UserExpressVO.class);

        return ResponseUtils.success(vos);
    }

    @Operation(summary = "详情")
    @Parameter(name = "id", description = "编号", in = ParameterIn.QUERY)
    @GetMapping("/detail")
    public ResponseVO<UserExpressVO> detail(@RequestParam("id") Long id) {
        UserExpressDO expressDO = userExpressService.getById(id);

        UserExpressVO expressVO = BeanCopierUtils.copy(expressDO, UserExpressVO.class);

        return ResponseUtils.success(expressVO);
    }

    @Operation(summary = "新增/编辑")
    @PostMapping("/add-update")
    public ResponseVO<Object> addUpdate(@RequestBody UserExpressRO expressRO) {
        String name = expressRO.getName();
        AssertUtils.isNotBlank(name, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(name.length() <= 32, ResponseEnum.invalid_parameter);

        String phone = expressRO.getPhone();
        AssertUtils.isNotBlank(phone, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(phone.length() <= 16, ResponseEnum.invalid_parameter);

        String region = expressRO.getRegion();
        AssertUtils.isNotBlank(region, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(region.length() <= 256, ResponseEnum.invalid_parameter);

        String location = expressRO.getLocation();
        AssertUtils.isNotBlank(location, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(location.length() <= 256, ResponseEnum.invalid_parameter);

        AssertUtils.isNotNull(CommonEnum.BoolEnum.exist(expressRO.getStatus()), ResponseEnum.invalid_parameter);

        UserExpressDO expressDO = BeanCopierUtils.copy(expressRO, UserExpressDO.class);
        expressDO.setAddress(ThreadInfo.getInfo().getAddress());

        userExpressService.addUpdate(expressDO);

        return ResponseUtils.success();
    }

    @Operation(summary = "删除")
    @PostMapping("/delete")
    public ResponseVO<Object> delete(@RequestBody LongIdRO idRO) {
        ParamsUtils.positiveLong(idRO.getId());

        UserExpressDO selectDO = userExpressService.getById(idRO.getId());
        AssertUtils.isNotNull(selectDO, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(selectDO.getAddress().equals(ThreadInfo.getInfo().getAddress()), ResponseEnum.invalid_parameter);

        userExpressService.deleteById(idRO.getId());
        return ResponseUtils.success();
    }
}
