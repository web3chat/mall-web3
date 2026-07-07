package com.fzm.mall.controller.web;

import com.fzm.mall.annotation.UnAuthorization;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.dataobject.UserAdminDO;
import com.fzm.mall.entity.viewobject.MerchantVO;
import com.fzm.mall.service.UserAdminService;
import com.fzm.mall.util.FormatVOUtils;
import com.fzm.mall.util.ParamsUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商户")
@RestController
@UnAuthorization
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/v/merchant", produces = MediaType.APPLICATION_JSON_VALUE)
public class MerchantController {
    private final UserAdminService userAdminService;

    @Operation(summary = "信息")
    @Parameter(name = "address", description = "地址", in = ParameterIn.QUERY)
    @GetMapping("/info")
    public ResponseVO<MerchantVO> info(@RequestParam("address") String address) {
        address = ParamsUtils.address(address);
        UserAdminDO userAdminDO = userAdminService.getByAddress(address);

        MerchantVO merchantVO = FormatVOUtils.getMerchantVO(userAdminDO);

        return ResponseUtils.success(merchantVO);
    }
}
