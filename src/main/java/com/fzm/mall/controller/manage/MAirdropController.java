package com.fzm.mall.controller.manage;

import com.fzm.mall.component.ExcelReadComponent;
import com.fzm.mall.constant.enums.AirdropEnum;
import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.enums.MetaTypeEnum;
import com.fzm.mall.constant.enums.UserEnum;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.common.PageVO;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.entity.dataobject.AirdropInfoDO;
import com.fzm.mall.entity.dataobject.AirdropWhiteDO;
import com.fzm.mall.entity.dataobject.GoodsSkuDO;
import com.fzm.mall.entity.queryobject.back.MAirdropPageQO;
import com.fzm.mall.entity.requestobject.back.MAirdropInfoIdRO;
import com.fzm.mall.entity.requestobject.back.MAirdropStatusRO;
import com.fzm.mall.entity.viewobject.back.MAirdropInfoVO;
import com.fzm.mall.entity.viewobject.back.MAirdropVO;
import com.fzm.mall.entity.viewobject.back.MAirdropWhiteVO;
import com.fzm.mall.service.AirdropService;
import com.fzm.mall.service.GoodsService;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.BeanCopierUtils;
import com.fzm.mall.util.PageVOUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "空投")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/m/airdrop", produces = MediaType.APPLICATION_JSON_VALUE)
public class MAirdropController {
    private final AirdropService airdropService;
    private final GoodsService goodsService;
    private final ExcelReadComponent excelReadComponent;

    @Operation(summary = "分页查询")
    @PostMapping("/page")
    public ResponseVO<PageVO<MAirdropInfoVO>> page(@RequestBody MAirdropPageQO pageQO) {
        if (ThreadInfo.getInfo().getRole() == UserEnum.RoleEnum.merchant.getRole()) {
            pageQO.setAddress(ThreadInfo.getInfo().getParentAddress());
        }

        PageInfo<AirdropInfoDO> pageInfo = airdropService.pageInfo(pageQO);

        return PageVOUtils.pageVO(pageInfo, MAirdropInfoVO.class);
    }

    @Operation(summary = "详情")
    @Parameter(name = "infoId", description = "编号", in = ParameterIn.QUERY)
    @GetMapping("/detail")
    public ResponseVO<MAirdropVO> detail(@RequestParam("infoId") Long infoId) {
        AirdropInfoDO infoDO = airdropService.getInfoByInfoId(infoId);
        AssertUtils.isNotNull(infoDO, "编号错误");
        if (ThreadInfo.getInfo().getRole() == UserEnum.RoleEnum.merchant.getRole()) {
            AssertUtils.isTrue(infoDO.getAddress().equals(ThreadInfo.getInfo().getParentAddress()), "编号错误");
        }

        List<AirdropWhiteDO> whiteDOS = airdropService.listWhiteByInfoId(infoId);

        List<MAirdropWhiteVO> whiteVOS = BeanCopierUtils.copyList(whiteDOS, MAirdropWhiteVO.class);

        List<MAirdropWhiteVO> vos = whiteVOS.stream().filter(o -> o.getMetaType() == MetaTypeEnum.goods.getType()).toList();
        if (CollectionUtils.isNotEmpty(vos)) {
            List<String> skuIds = vos.stream().map(MAirdropWhiteVO::getMetaId).distinct().toList();
            Map<String, GoodsSkuDO> skuDOMap = goodsService.listSkuBySkuIds(skuIds).stream().collect(Collectors.toMap(GoodsSkuDO::getSkuId, o -> o));
            for (MAirdropWhiteVO whiteVO : vos) {
                whiteVO.setMetaName(skuDOMap.get(whiteVO.getMetaId()).getTokenName());
            }
        }

        MAirdropVO airdropVO = new MAirdropVO();
        airdropVO.setInfo(BeanCopierUtils.copy(infoDO, MAirdropInfoVO.class));
        airdropVO.setWhites(whiteVOS);

        return ResponseUtils.success(airdropVO);
    }

    @Operation(summary = "新建/编辑")
    @Parameters({
            @Parameter(name = "infoId", description = "空投编号", in = ParameterIn.QUERY),
            @Parameter(name = "name", description = "名称", in = ParameterIn.QUERY),
            @Parameter(name = "startTime", description = "开始时间", in = ParameterIn.QUERY),
    })
    @PostMapping("/add-update")
    public ResponseVO<Object> addUpdate(@RequestParam(value = "infoId", required = false) Long infoId,
                                        @RequestParam("name") String name,
                                        @RequestParam("startTime") Long startTime,
                                        @RequestPart(value = "file", required = false) MultipartFile file) {
        AssertUtils.isNotBlank(name, "name为空");
        AssertUtils.isTrue(name.length() <= 64, "name最多64个字");

        AssertUtils.isNotNull(startTime, "startTime为空");

        List<AirdropWhiteDO> whiteDOS = null;
        if (file != null) {
            whiteDOS = excelReadComponent.readAirdropWhiteDOS(file, ThreadInfo.getInfo().getParentAddress());
            AssertUtils.isNotEmpty(whiteDOS, "名单为空");
        }

        AirdropInfoDO infoDO = new AirdropInfoDO();
        infoDO.setInfoId(infoId);
        infoDO.setName(name);
        infoDO.setStartTime(startTime);
        infoDO.setAddress(ThreadInfo.getInfo().getParentAddress());

        airdropService.addOrUpdate(infoDO, whiteDOS);

        return ResponseUtils.success();
    }

    @Operation(summary = "上下架")
    @PostMapping("/status")
    public ResponseVO<Object> status(@RequestBody MAirdropStatusRO statusRO) {
        Integer status = statusRO.getStatus();
        AssertUtils.isNotNull(CommonEnum.BoolEnum.exist(status), "status错误");

        Long infoId = statusRO.getInfoId();
        AirdropInfoDO selectDO = airdropService.getInfoByInfoId(infoId);
        AssertUtils.isNotNull(selectDO, "编号错误");
        AssertUtils.isTrue(selectDO.getAddress().equals(ThreadInfo.getInfo().getParentAddress()), "编号错误");
        AssertUtils.isTrue(selectDO.getTaskStatus() == AirdropEnum.TaskStatusEnum.wait.getStatus(), "只有未执行的可以上下架");

        AirdropInfoDO updateDO = new AirdropInfoDO();
        updateDO.setInfoId(infoId);
        updateDO.setStatus(status);
        updateDO.setOriginalStatus(selectDO.getStatus());

        airdropService.updateInfoByInfoId(updateDO);

        return ResponseUtils.success();
    }

    @Operation(summary = "重试，只有失败的可以")
    @PostMapping("/retry")
    public ResponseVO<Object> retry(@RequestBody MAirdropInfoIdRO infoIdRO) {
        Long infoId = infoIdRO.getInfoId();
        AirdropInfoDO selectDO = airdropService.getInfoByInfoId(infoId);
        AssertUtils.isNotNull(selectDO, "编号错误");
        AssertUtils.isTrue(selectDO.getAddress().equals(ThreadInfo.getInfo().getParentAddress()), "编号错误");
        AssertUtils.isTrue(selectDO.getTaskStatus() == AirdropEnum.TaskStatusEnum.fail.getStatus(), "只有失败的可以重试");

        AirdropInfoDO updateDO = new AirdropInfoDO();
        updateDO.setInfoId(infoId);
        updateDO.setTaskStatus(AirdropEnum.TaskStatusEnum.assign_token.getStatus());
        updateDO.setOriginalTaskStatus(AirdropEnum.TaskStatusEnum.fail.getStatus());

        airdropService.updateInfoByInfoId(updateDO);

        return ResponseUtils.success();
    }

    @Operation(summary = "删除，只有未上架待空投的能删除")
    @PostMapping("/delete")
    public ResponseVO<Object> delete(@RequestBody MAirdropInfoIdRO infoIdRO) {
        Long infoId = infoIdRO.getInfoId();
        AirdropInfoDO selectDO = airdropService.getInfoByInfoId(infoId);
        AssertUtils.isNotNull(selectDO, "编号错误");
        AssertUtils.isTrue(selectDO.getAddress().equals(ThreadInfo.getInfo().getParentAddress()), "编号错误");
        AssertUtils.isTrue(selectDO.getStatus() == CommonEnum.BoolEnum.NO.getStatus(), "只有未上架的可以删除");
        AssertUtils.isTrue(selectDO.getTaskStatus() == AirdropEnum.TaskStatusEnum.wait.getStatus(), "只有未空投的可以删除");

        airdropService.deleteByInfoId(infoId);

        return ResponseUtils.success();
    }
}
