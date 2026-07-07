package com.fzm.mall.controller.manage;

import com.alibaba.fastjson2.JSON;
import com.fzm.mall.component.ExcelReadComponent;
import com.fzm.mall.component.GoodsComponent;
import com.fzm.mall.constant.ChainConstant;
import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.enums.FileEnum;
import com.fzm.mall.constant.enums.GoodsEnum;
import com.fzm.mall.constant.enums.UserEnum;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.common.PageVO;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.entity.dataobject.*;
import com.fzm.mall.entity.queryobject.back.MGoodsPageQO;
import com.fzm.mall.entity.requestobject.GoodsIdRO;
import com.fzm.mall.entity.requestobject.back.*;
import com.fzm.mall.entity.viewobject.GoodsWhiteVO;
import com.fzm.mall.entity.viewobject.back.*;
import com.fzm.mall.service.GoodsService;
import com.fzm.mall.service.UserAdminService;
import com.fzm.mall.third.component.FileComponent;
import com.fzm.mall.util.*;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "商品")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/m/goods", produces = MediaType.APPLICATION_JSON_VALUE)
public class MGoodsController {
    private final GoodsService goodsService;
    private final UserAdminService userAdminService;
    private final GoodsComponent goodsComponent;
    private final ExcelReadComponent excelReadComponent;
    private final FileComponent fileComponent;


    @Operation(summary = "分页查询")
    @PostMapping("/page")
    public ResponseVO<PageVO<MGoodsVO>> page(@RequestBody MGoodsPageQO pageQO) {
        // 商户只能查看自己的商品
        if (ThreadInfo.getInfo().getRole() == UserEnum.RoleEnum.merchant.getRole()) {
            pageQO.setAddress(ThreadInfo.getInfo().getParentAddress());
        }

        PageInfo<GoodsSpuDO> pageInfo = goodsService.pageSpu(pageQO);

        List<String> addressList = pageInfo.getList().stream().map(GoodsSpuDO::getAddress).distinct().toList();
        Map<String, UserAdminDO> adminDOMap = userAdminService.listByAddressList(addressList).stream().collect(Collectors.toMap(UserAdminDO::getAddress, o -> o));

        List<MGoodsVO> goodsVOS = pageInfo.getList().stream().map(o -> {
            MGoodsVO goodsVO = new MGoodsVO();
            MGoodsSpuVO spuVO = BeanCopierUtils.copy(o, MGoodsSpuVO.class);
            spuVO.setDes("");
            spuVO.setDetail("");
            spuVO.setImageJson("[]");
            goodsVO.setSpu(spuVO);
            goodsVO.setMerchant(FormatVOUtils.getMerchantVO(adminDOMap.get(o.getAddress())));
            return goodsVO;
        }).toList();

        return PageVOUtils.pageVO(pageInfo, goodsVOS);
    }

    @Operation(summary = "详情")
    @Parameter(name = "goodsId", description = "商品编号", in = ParameterIn.QUERY)
    @GetMapping("/detail")
    public ResponseVO<MGoodsVO> detail(@RequestParam("goodsId") String goodsId) {
        GoodsSpuDO spuDO = goodsComponent.verifyGoodsIdAndGetSpuDO(goodsId, ThreadInfo.getInfo().getRole());
        GoodsSkuPropertiesDO skuPropDO = goodsService.getSkuPropByGoodsId(goodsId);
        List<GoodsSkuDO> skuDOS = goodsService.listSkuByGoodsId(goodsId);

        MGoodsVO goodsVO = new MGoodsVO();
        goodsVO.setSpu(BeanCopierUtils.copy(spuDO, MGoodsSpuVO.class));
        goodsVO.setSkuProp(BeanCopierUtils.copy(skuPropDO, MGoodsSkuPropertiesVO.class));
        goodsVO.setSkus(BeanCopierUtils.copyList(skuDOS, MGoodsSkuVO.class));

        return ResponseUtils.success(goodsVO);
    }

    @Operation(summary = "新增/编辑，只有草稿和下架状态可以编辑")
    @PostMapping("/add-update")
    public ResponseVO<Object> addUpdate(@RequestBody MGoodsRO goodsRO) {
        // SPU
        MGoodsSpuRO spuRO = goodsRO.getSpu();
        AssertUtils.isNotNull(spuRO, "spu为空");

        GoodsEnum.SpuTypeEnum spuTypeEnum = GoodsEnum.SpuTypeEnum.exist(spuRO.getType());
        AssertUtils.isNotNull(spuTypeEnum, "spu.type类型错误");

        List<Integer> menuIds = spuRO.getMenuIds();
        if (CollectionUtils.isEmpty(menuIds)) {
            menuIds = Collections.emptyList();
        }
        menuIds = menuIds.stream().distinct().toList();

        List<Integer> classifies = spuRO.getClassifies();
        // TODO 校验分类


        String name = spuRO.getName();
        AssertUtils.isNotBlank(name, "spu.name为空");
        AssertUtils.isTrue(name.length() <= 64, "spu.name最多只能64个字");

        String des = spuRO.getDes();
        if (StringUtils.isBlank(des)) {
            spuRO.setDes("");
        } else {
            AssertUtils.isTrue(des.length() <= 256, "spu.des最多只能256个字");
        }

        String detail = spuRO.getDetail();
        if (StringUtils.isBlank(detail)) {
            spuRO.setDetail("");
        } else {
            String detailLower = detail.toLowerCase().replace(fileComponent.getHost(), "");
            AssertUtils.isFalse(detailLower.contains("http"), "spu.detail不能使用外部链接");
            AssertUtils.isFalse(detailLower.contains("<iframe"), "spu.detail不能使用iframe标签");
            AssertUtils.isFalse(detailLower.contains("<embed"), "spu.detail不能使用embed标签");
            AssertUtils.isFalse(detailLower.contains("<audio"), "spu.detail不能使用audio标签");
        }

        fileComponent.verifyFileUrl(spuRO.getCover(), FileEnum.image, "spu.cover只能使用图片");
        for (String image : spuRO.getImages()) {
            fileComponent.verifyFileUrl(image, FileEnum.image, "spu.images只能使用图片");
        }

        Integer orderLimit = spuRO.getOrderLimit();
        AssertUtils.isNotNull(orderLimit, "spu.orderLimit为空");
        AssertUtils.isTrue(orderLimit >= -1, "spu.orderLimit必须大于等于-1");

        GoodsEnum.SaleTypeEnum saleTypeEnum = GoodsEnum.SaleTypeEnum.exist(spuRO.getSaleType());
        AssertUtils.isNotNull(saleTypeEnum, "spu.saleType不在范围内");

        Long saleTime = spuRO.getSaleTime();
        AssertUtils.isNotNull(saleTime, "spu.saleTime为空");

        if (saleTypeEnum == GoodsEnum.SaleTypeEnum.normal) {
            spuRO.setSaleTimeNormal(null);
        } else {
            Long saleTimeNormal = spuRO.getSaleTimeNormal();
            AssertUtils.isNotNull(saleTimeNormal, "spu.saleTimeNormal为空");
            AssertUtils.isTrue(saleTimeNormal > saleTime, "spu.saleTimeNormal必须在saleTime之后");
        }

        // SKU Properties
        MGoodsSkuPropertiesRO skuPropRO = goodsRO.getSkuProp();
        String title1 = skuPropRO.getTitle1();
        AssertUtils.isNotBlank(title1, "skuProp.title1为空");
        AssertUtils.isTrue(title1.length() <= 16, "skuProp.title1最多只能16个字");

        List<String> value1s = skuPropRO.getValue1s();
        AssertUtils.isNotEmpty(value1s, "skuProp.value1s为空");
        long value1sCount = value1s.stream().filter(StringUtils::isNotBlank).distinct().count();
        AssertUtils.isTrue(value1s.size() == value1sCount, "skuProp.value1s有重复项");

        String title2 = skuPropRO.getTitle2();
        List<String> value2s;
        if (StringUtils.isBlank(title2)) {
            value2s = Collections.singletonList("");
            skuPropRO.setTitle2("");
            skuPropRO.setValue2s(value2s);
        } else {
            AssertUtils.isTrue(title2.length() <= 16, "skuProp.title2最多只能16个字");
            value2s = skuPropRO.getValue2s();
            AssertUtils.isNotEmpty(value2s, "skuProp.value2s为空");
            long value2sCount = value2s.stream().filter(StringUtils::isNotBlank).distinct().count();
            AssertUtils.isTrue(value2s.size() == value2sCount, "skuProp.value2s有重复项");
        }

        // SKU
        List<MGoodsSkuRO> skuROS = goodsRO.getSkus();
        AssertUtils.isNotEmpty(skuROS, "skus为空");

        skuROS = skuROS.stream().peek(o -> {
            if (StringUtils.isBlank(o.getPropValue2())) {
                o.setPropValue2("");
            }
        }).toList();

        Map<String, List<MGoodsSkuRO>> skusMap = skuROS.stream().collect(Collectors.groupingBy(MGoodsSkuRO::getPropValue1));
        skusMap.forEach((propValue1, skusSub) -> {
            boolean anyMatch = value1s.stream().anyMatch(v -> v.equals(propValue1));
            AssertUtils.isTrue(anyMatch, "skus.propValue1无法匹配skuProp.value1s");

            long propValue2distinctCount = skusSub.stream().map(MGoodsSkuRO::getPropValue2).distinct().count();
            AssertUtils.isTrue(skusSub.size() == propValue2distinctCount, "同一个skus.propValue1下，skus.propValue2有重复项");

            for (MGoodsSkuRO skuRO : skusSub) {
                anyMatch = value2s.stream().anyMatch(v -> v.equals(skuRO.getPropValue2()));
                AssertUtils.isTrue(anyMatch, "skus.propValue2无法匹配skuProp.value2s");

                String skuName = skuRO.getName();
                AssertUtils.isNotBlank(skuName, "skus.name为空");
                AssertUtils.isTrue(skuName.length() <= 64, "skus.name最多只能64个字");

                String tokenName = skuRO.getTokenName();
                AssertUtils.isNotBlank(tokenName, "skus.tokenName为空");
                AssertUtils.isTrue(tokenName.length() <= 64, "skus.tokenName最多只能64个字");

                fileComponent.verifyFileUrl(skuRO.getCover(), FileEnum.image, "skus.cover只能使用图片");

                BigDecimal price = skuRO.getPrice();
                AssertUtils.isNotNull(price, "skus.price为空");
                BigDecimal priceScale = price.setScale(2, RoundingMode.DOWN);
                AssertUtils.isTrue(price.compareTo(priceScale) == 0, "skus.price最多支持2位小数");
                AssertUtils.isTrue(priceScale.compareTo(BigDecimal.ZERO) > 0, "skus.price必须大于0");

                Integer total = skuRO.getTotal();
                AssertUtils.isNotNull(total, "skus.total为空");
                AssertUtils.isTrue(total > 0, "skus.total必须大于0");
                AssertUtils.isTrue(total < ChainConstant.token_max_serial, "skus.total最多只能" + ChainConstant.token_max_serial);


                Integer skuOrderLimit = skuRO.getOrderLimit();
                AssertUtils.isNotNull(skuOrderLimit, "skus.orderLimit为空");
                AssertUtils.isTrue(skuOrderLimit >= -1, "skus.orderLimit必须大于等于-1");


                Integer orderPack = skuRO.getOrderPack();
                AssertUtils.isNotNull(orderPack, "skus.orderPack为空");
                AssertUtils.isTrue(orderPack >= 1, "skus.orderPack必须大于等于1");

                AssertUtils.isNotNull(CommonEnum.BoolEnum.exist(skuRO.getExpressType()), "skus.expressType错误");

                CommonEnum.BoolEnum blindBoxTypeEnum = CommonEnum.BoolEnum.exist(skuRO.getBlindBoxType());
                AssertUtils.isNotNull(blindBoxTypeEnum, "skus.blindBoxType错误");
                if (blindBoxTypeEnum == CommonEnum.BoolEnum.YES) {
                    AssertUtils.isTrue(spuTypeEnum == GoodsEnum.SpuTypeEnum.blind_box, "skus.blindBoxType为盲盒时，spu.type错误");
                }

                String traceHash = skuRO.getTraceHash();
                if (StringUtils.isBlank(traceHash)) {
                    traceHash = "";
                } else {
                    traceHash = ParamsUtils.txHash(traceHash);
                }
                skuRO.setTraceHash(traceHash);
            }
        });

        // 检测商品类型
        if (spuTypeEnum == GoodsEnum.SpuTypeEnum.blind_box) {
            AssertUtils.isTrue(skuROS.size() > 1, "spu.type为盲盒时，skus必须大于1");

            int otherTotal = 0;
            for (int i = 0; i < skuROS.size(); i++) {
                MGoodsSkuRO skuRO = skuROS.get(i);

                if (i == 0) {
                    AssertUtils.isTrue(skuRO.getBlindBoxType() == CommonEnum.BoolEnum.YES.getStatus(), "盲盒商品第一个sku的blindBoxType必须是1");
                } else {
                    AssertUtils.isTrue(skuRO.getBlindBoxType() == CommonEnum.BoolEnum.NO.getStatus(), "盲盒商品其他sku的blindBoxType必须是0");

                    otherTotal += skuRO.getTotal();
                }
            }
            AssertUtils.isTrue(skuROS.get(0).getTotal() == otherTotal, "盲盒第一个sku的总量必须等于其他sku的总和");
        }


        GoodsSpuDO spuDO = BeanCopierUtils.copy(spuRO, GoodsSpuDO.class);
        spuDO.setAddress(ThreadInfo.getInfo().getParentAddress());
        spuDO.setMenuJson(JSON.toJSONString(menuIds));
        spuDO.setClassifyJson(JSON.toJSONString(spuRO.getClassifies()));
        spuDO.setImageJson(JSON.toJSONString(spuRO.getImages()));

        GoodsSkuPropertiesDO skuPropDO = BeanCopierUtils.copy(skuPropRO, GoodsSkuPropertiesDO.class);
        skuPropDO.setValue1Json(JSON.toJSONString(skuPropRO.getValue1s()));
        skuPropDO.setValue2Json(JSON.toJSONString(skuPropRO.getValue2s()));

        List<GoodsSkuDO> skuDOS = BeanCopierUtils.copyList(skuROS, GoodsSkuDO.class);
        for (GoodsSkuDO skuDO : skuDOS) {
            skuDO.setAddress(ThreadInfo.getInfo().getParentAddress());
        }

        goodsService.addOrUpdate(spuDO, skuPropDO, skuDOS);

        return ResponseUtils.success();
    }

    @Operation(summary = "铸造，只有草稿可以")
    @PostMapping("/mint")
    public ResponseVO<Object> mint(@RequestBody GoodsIdRO goodsIdRO) {
        String goodsId = goodsIdRO.getGoodsId();
        GoodsSpuDO selectSpuDO = goodsComponent.verifyGoodsIdAndGetSpuDO(goodsId, ThreadInfo.getInfo().getRole());
        AssertUtils.isTrue(selectSpuDO.getStatus() == GoodsEnum.SpuStatusEnum.draft.getStatus(), "商品不可铸造");

        goodsService.mint(selectSpuDO);

        return ResponseUtils.success();
    }

    @Operation(summary = "铸造记录")
    @Parameter(name = "skuId", description = "skuId", in = ParameterIn.QUERY)
    @GetMapping("/mint/record")
    public ResponseVO<List<MGoodsMintVO>> mintRecord(@RequestParam("skuId") String skuId) {
        List<GoodsMintDO> mintDOS = goodsService.listMintBySkuId(skuId);
        List<MGoodsMintVO> mintVOS = BeanCopierUtils.copyList(mintDOS, MGoodsMintVO.class);

        return ResponseUtils.success(mintVOS);
    }

    @Operation(summary = "重试，只有铸造失败/增发失败的可以")
    @PostMapping("/mint/retry")
    public ResponseVO<Object> mintRetry(@RequestBody GoodsIdRO goodsIdRO) {
        String goodsId = goodsIdRO.getGoodsId();
        GoodsSpuDO selectSpuDO = goodsComponent.verifyGoodsIdAndGetSpuDO(goodsId, ThreadInfo.getInfo().getRole());
        AssertUtils.isTrue(selectSpuDO.getStatus() == GoodsEnum.SpuStatusEnum.mint_fail.getStatus() || selectSpuDO.getStatus() == GoodsEnum.SpuStatusEnum.mint_again_fail.getStatus(), "商品不可铸造");

        goodsService.retry(selectSpuDO);

        return ResponseUtils.success();
    }

    @Operation(summary = "上下架")
    @PostMapping("/status")
    public ResponseVO<Object> status(@RequestBody MGoodsStatusRO statusRO) {
        Integer status = statusRO.getStatus();
        AssertUtils.isNotNull(status, "status错误");
        AssertUtils.isTrue(status == GoodsEnum.SpuStatusEnum.mint_success.getStatus() || status == GoodsEnum.SpuStatusEnum.mint_sell_ing.getStatus(), "status错误");

        String goodsId = statusRO.getGoodsId();
        GoodsSpuDO selectSpuDO = goodsComponent.verifyGoodsIdAndGetSpuDO(goodsId, ThreadInfo.getInfo().getRole());

        if (status == GoodsEnum.SpuStatusEnum.mint_success.getStatus()) {
            AssertUtils.isTrue(selectSpuDO.getStatus() == GoodsEnum.SpuStatusEnum.mint_sell_ing.getStatus(), "商品不可下架");
        }
        if (status == GoodsEnum.SpuStatusEnum.mint_sell_ing.getStatus()) {
            AssertUtils.isTrue(selectSpuDO.getStatus() == GoodsEnum.SpuStatusEnum.mint_success.getStatus(), "商品不可上架");
        }

        GoodsSpuDO updateSpuDO = new GoodsSpuDO();
        updateSpuDO.setGoodsId(goodsId);
        updateSpuDO.setStatus(status);
        updateSpuDO.setOriginalStatus(selectSpuDO.getStatus());

        goodsService.updateSpuByGoodsId(updateSpuDO);

        return ResponseUtils.success();
    }

    @Operation(summary = "商品隐藏")
    @PostMapping("/hidden")
    public ResponseVO<Object> hidden(@RequestBody MGoodsHiddenStatusRO statusRO) {
        Integer hiddenStatus = statusRO.getHiddenStatus();
        AssertUtils.isNotNull(CommonEnum.BoolEnum.exist(hiddenStatus), "hiddenStatus错误");

        String goodsId = statusRO.getGoodsId();
        goodsComponent.verifyGoodsIdAndGetSpuDO(goodsId, ThreadInfo.getInfo().getRole());

        GoodsSpuDO updateSpuDO = new GoodsSpuDO();
        updateSpuDO.setGoodsId(goodsId);
        updateSpuDO.setHiddenStatus(hiddenStatus);

        goodsService.updateSpuByGoodsId(updateSpuDO);

        return ResponseUtils.success();
    }

    @Operation(summary = "删除，只有草稿能删除")
    @PostMapping("/delete")
    public ResponseVO<Object> delete(@RequestBody GoodsIdRO goodsIdRO) {
        String goodsId = goodsIdRO.getGoodsId();
        GoodsSpuDO selectSpuDO = goodsComponent.verifyGoodsIdAndGetSpuDO(goodsId, ThreadInfo.getInfo().getRole());
        AssertUtils.isTrue(selectSpuDO.getStatus() == GoodsEnum.SpuStatusEnum.draft.getStatus(), "商品不可删除");

        goodsService.deleteByGoodsId(goodsId);

        return ResponseUtils.success();
    }

    @Operation(summary = "推荐")
    @PostMapping("/recommend")
    public ResponseVO<Object> recommend(@RequestBody MGoodsRecommendRO recommendRO) {
        Integer recommend = recommendRO.getRecommend();
        AssertUtils.isNotNull(CommonEnum.BoolEnum.exist(recommend), "recommend错误");

        String goodsId = recommendRO.getGoodsId();
        goodsComponent.verifyGoodsIdAndGetSpuDO(goodsId, ThreadInfo.getInfo().getRole());

        GoodsSpuDO updateSpuDO = new GoodsSpuDO();
        updateSpuDO.setGoodsId(goodsId);
        updateSpuDO.setRecommend(recommend);

        goodsService.updateSpuByGoodsId(updateSpuDO);

        return ResponseUtils.success();
    }

    @Operation(summary = "查看白名单")
    @Parameter(name = "goodsId", description = "商品编号", in = ParameterIn.QUERY)
    @GetMapping("/white-list")
    public ResponseVO<List<GoodsWhiteVO>> listWhite(@RequestParam("goodsId") String goodsId) {
        goodsComponent.verifyGoodsIdAndGetSpuDO(goodsId, ThreadInfo.getInfo().getRole());

        List<GoodsWhiteDO> whiteDOS = goodsService.listWhiteByGoodsId(goodsId);
        List<GoodsWhiteVO> whiteVOS = BeanCopierUtils.copyList(whiteDOS, GoodsWhiteVO.class);

        return ResponseUtils.success(whiteVOS);
    }

    @Operation(summary = "白名单添加，会删除原名单")
    @Parameter(name = "goodsId", description = "商品编号", in = ParameterIn.QUERY)
    @PostMapping("/add-white-list")
    public ResponseVO<Object> addWhite(@RequestParam("goodsId") String goodsId, @RequestPart("file") MultipartFile file) {
        goodsComponent.verifyGoodsIdAndGetSpuDO(goodsId, ThreadInfo.getInfo().getRole());

        List<GoodsWhiteDO> whiteDOS = excelReadComponent.readGoodsWhiteDOS(goodsId, file);
        AssertUtils.isNotEmpty(whiteDOS, "名单为空");

        goodsService.addOrUpdateWhite(goodsId, whiteDOS);

        return ResponseUtils.success();
    }

}
