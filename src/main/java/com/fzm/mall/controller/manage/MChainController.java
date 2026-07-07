package com.fzm.mall.controller.manage;

import com.fzm.mall.annotation.UnAuthorization;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.dataobject.ChainContractDO;
import com.fzm.mall.entity.viewobject.ChainContractVO;
import com.fzm.mall.service.ChainService;
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

@Tag(name = "区块链")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/m/chain", produces = MediaType.APPLICATION_JSON_VALUE)
public class MChainController {
    private final ChainService chainService;

    @UnAuthorization
    @Operation(summary = "合约列表")
    @GetMapping("/contract/list")
    public ResponseVO<List<ChainContractVO>> listContract() {
        List<ChainContractDO> dos = chainService.listContract();
        List<ChainContractVO> vos = BeanCopierUtils.copyList(dos, ChainContractVO.class);
        return ResponseUtils.success(vos);
    }

}
