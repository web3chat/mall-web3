package com.fzm.mall.controller.web;

import com.alibaba.fastjson2.JSON;
import com.fzm.mall.annotation.UnAuthorization;
import com.fzm.mall.component.GoodsComponent;
import com.fzm.mall.constant.ChainConstant;
import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.enums.GoodsEnum;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.common.PageVO;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.entity.dataobject.*;
import com.fzm.mall.entity.queryobject.GoodsPageQO;
import com.fzm.mall.entity.queryobject.back.MGoodsPageQO;
import com.fzm.mall.entity.requestobject.GoodsIdRO;
import com.fzm.mall.entity.viewobject.*;
import com.fzm.mall.service.ChainService;
import com.fzm.mall.service.GoodsService;
import com.fzm.mall.service.OrderService;
import com.fzm.mall.service.UserAdminService;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.BeanCopierUtils;
import com.fzm.mall.util.FormatVOUtils;
import com.fzm.mall.util.PageVOUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "商品")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/v/goods", produces = MediaType.APPLICATION_JSON_VALUE)
public class GoodsController {
    private final GoodsComponent goodsComponent;
    private final GoodsService goodsService;
    private final OrderService orderService;
    private final ChainService chainService;
    private final UserAdminService userAdminService;

    @UnAuthorization
    @Operation(summary = "分页查询")
    @PostMapping("/page")
    public ResponseVO<PageVO<GoodsPageVO>> page(@RequestBody GoodsPageQO goodsPageQO) {
        MGoodsPageQO pageQO = BeanCopierUtils.copy(goodsPageQO, MGoodsPageQO.class);
        pageQO.setStatus(GoodsEnum.SpuStatusEnum.mint_sell_ing.getStatus());
        pageQO.setHiddenStatus(CommonEnum.BoolEnum.NO.getStatus());
        pageQO.setOrderType(GoodsEnum.OrderTypeEnum.score.getType());

        PageInfo<GoodsSpuDO> pageInfo = goodsService.pageSpu(pageQO);

        List<String> addressList = pageInfo.getList().stream().map(GoodsSpuDO::getAddress).distinct().toList();
        Map<String, UserAdminDO> adminDOMap = userAdminService.listByAddressList(addressList).stream().collect(Collectors.toMap(UserAdminDO::getAddress, o -> o));

        List<GoodsPageVO> goodsVOS = pageInfo.getList().stream().map(o -> {
            GoodsPageVO goodsVO = new GoodsPageVO();
            goodsVO.setSpu(BeanCopierUtils.copy(o, GoodsSpuPageVO.class));
            goodsVO.setMerchant(FormatVOUtils.getMerchantVO(adminDOMap.get(o.getAddress())));
            return goodsVO;
        }).toList();

        return PageVOUtils.pageVO(pageInfo, goodsVOS);
    }

    @UnAuthorization
    @Operation(summary = "详情")
    @Parameter(name = "goodsId", description = "商品编号", in = ParameterIn.QUERY)
    @GetMapping("/detail")
    public ResponseVO<GoodsDetailVO> detail(@RequestParam("goodsId") String goodsId) {
        GoodsSpuDO spuDO = goodsComponent.verifyGoodsIdAndGetSpuDO(goodsId);
        AssertUtils.isNotNull(spuDO, ResponseEnum.invalid_parameter);

        GoodsSkuPropertiesDO skuPropDO = goodsService.getSkuPropByGoodsId(goodsId);
        List<GoodsSkuDO> skuDOS = goodsService.listSkuByGoodsId(goodsId);
        UserAdminDO userAdminDO = userAdminService.getByAddress(spuDO.getAddress());
        ChainContractDO contractDO = chainService.getContractByCtId(ChainConstant.contract_default_contract_id);


        // 设置SPU限购
        orderService.setOrderLimit(spuDO, null, ThreadInfo.getInfo().getAddress());

        // 设置SKU限购
        if (spuDO.getType() == GoodsEnum.SpuTypeEnum.blind_box.getType()) {
            skuDOS = Collections.singletonList(skuDOS.get(0));
        }
        List<GoodsSkuVO> skuVOS = skuDOS.stream()
                .peek(skuDO -> orderService.setOrderLimit(spuDO, skuDO, ThreadInfo.getInfo().getAddress()))
                .map(skuDO -> BeanCopierUtils.copy(skuDO, GoodsSkuVO.class)).toList();

        List<String> value1s = JSON.parseArray(skuPropDO.getValue1Json(), String.class);
        List<String> value2s = JSON.parseArray(skuPropDO.getValue2Json(), String.class);

        List<GoodsDetailVO.Value1VO> v1Skus = new ArrayList<>(value1s.size());
        for (String value1 : value1s) {
            List<GoodsSkuVO> v1Filters = skuVOS.stream().filter(o -> o.getPropValue1().equals(value1)).toList();
            if (CollectionUtils.isEmpty(v1Filters)) {
                continue;
            }

            List<GoodsDetailVO.Value2VO> v2Skus = new ArrayList<>(value2s.size());
            for (String value2 : value2s) {
                GoodsSkuVO skuVO = v1Filters.stream().filter(o -> o.getPropValue2().equals(value2)).findFirst().orElse(null);
                if (skuVO == null) {
                    continue;
                }

                GoodsDetailVO.Value2VO value2VO = new GoodsDetailVO.Value2VO();
                value2VO.setPropValue2(value2);
                value2VO.setSku(skuVO);

                v2Skus.add(value2VO);
            }

            GoodsDetailVO.Value1VO value1VO = new GoodsDetailVO.Value1VO();
            value1VO.setPropValue1(value1);
            value1VO.setV2Skus(v2Skus);

            v1Skus.add(value1VO);
        }


        GoodsDetailVO goodsVO = new GoodsDetailVO();
        goodsVO.setSpu(BeanCopierUtils.copy(spuDO, GoodsSpuVO.class));
        goodsVO.setSkuProp(BeanCopierUtils.copy(skuPropDO, GoodsDetailVO.SkuPropVO.class));
        goodsVO.setV1Skus(v1Skus);
        goodsVO.setContract(BeanCopierUtils.copy(contractDO, ChainContractVO.class));
        goodsVO.setMerchant(FormatVOUtils.getMerchantVO(userAdminDO));

        return ResponseUtils.success(goodsVO);
    }

    @UnAuthorization
    @Operation(summary = "是否已经收藏")
    @Parameter(name = "goodsId", description = "商品编号", in = ParameterIn.QUERY)
    @GetMapping("/favorite")
    public ResponseVO<GoodsFavoriteVO> isFavorite(@RequestParam("goodsId") String goodsId) {
        GoodsFavoriteDO favoriteDO = goodsService.getFavoriteByGoodsIdAddress(goodsId, ThreadInfo.getInfo().getAddress());
        GoodsFavoriteVO favoriteVO = BeanCopierUtils.copy(favoriteDO, GoodsFavoriteVO.class);
        return ResponseUtils.success(favoriteVO);
    }

    @Operation(summary = "收藏/取消收藏")
    @PostMapping("/favorite")
    public ResponseVO<Object> favorite(@RequestBody GoodsIdRO goodsIdRO) {

        goodsService.favorite(goodsIdRO.getGoodsId(), ThreadInfo.getInfo().getAddress());

        return ResponseUtils.success();
    }


}
