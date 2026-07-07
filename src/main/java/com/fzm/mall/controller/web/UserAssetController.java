package com.fzm.mall.controller.web;

import com.alibaba.fastjson2.JSON;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.common.PageVO;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.entity.dataobject.*;
import com.fzm.mall.entity.queryobject.back.MUserAssetPageQO;
import com.fzm.mall.entity.viewobject.UserAssetCollectionVO;
import com.fzm.mall.entity.viewobject.UserAssetTokenVO;
import com.fzm.mall.entity.viewobject.UserAssetVO;
import com.fzm.mall.service.GoodsService;
import com.fzm.mall.service.UserAssetService;
import com.fzm.mall.third.chain.util.TokenUtils;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.BeanCopierUtils;
import com.fzm.mall.util.PageVOUtils;
import com.fzm.mall.util.ParamsUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "用户-资产")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/v/user/asset", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserAssetController {
    private final UserAssetService userAssetService;
    private final GoodsService goodsService;

    @Operation(summary = "分页查询")
    @PostMapping("/page")
    public ResponseVO<PageVO<UserAssetCollectionVO>> pageCollection(@RequestBody MUserAssetPageQO pageQO) {
        pageQO.setAddress(ThreadInfo.getInfo().getAddress());

        PageInfo<UserAssetCollectionDO> pageInfo = userAssetService.pageCollection(pageQO);

        List<String> goodsIds = pageInfo.getList().stream().map(UserAssetCollectionDO::getGoodsId).distinct().filter(StringUtils::isNotBlank).toList();
        Map<String, GoodsSpuDO> spuDOMap = goodsService.listSpuByGoodsIds(goodsIds).stream().collect(Collectors.toMap(GoodsSpuDO::getGoodsId, o -> o));

        List<UserAssetCollectionVO> assetVOS = pageInfo.getList().stream().map(o -> {
            GoodsSpuDO spuDO = spuDOMap.get(o.getGoodsId());

            UserAssetCollectionVO collectionVO = BeanCopierUtils.copy(o, UserAssetCollectionVO.class);

            collectionVO.setName(spuDO.getName());
            collectionVO.setCover(spuDO.getCover());
            return collectionVO;
        }).toList();

        return PageVOUtils.pageVO(pageInfo, assetVOS);
    }

    @Operation(summary = "列表")
    @Parameter(name = "goodsId", description = "商品编号", in = ParameterIn.QUERY)
    @GetMapping("/list")
    public ResponseVO<UserAssetVO> detail(@RequestParam("goodsId") String goodsId) {
        GoodsSkuPropertiesDO skuPropDO = goodsService.getSkuPropByGoodsId(goodsId);
        AssertUtils.isNotNull(skuPropDO, "商品编号错误");

        List<UserAssetTokenDO> tokenDOS = userAssetService.listTokenByGoodsIdAddress(goodsId, ThreadInfo.getInfo().getAddress());
        Map<String, List<UserAssetTokenDO>> tokenDOMap = tokenDOS.stream().collect(Collectors.groupingBy(UserAssetTokenDO::getSkuId));

        List<GoodsSkuDO> skuDOS = goodsService.listSkuBySkuIds(List.copyOf(tokenDOMap.keySet()));

        List<String> value1s = JSON.parseArray(skuPropDO.getValue1Json(), String.class);
        List<String> value2s = JSON.parseArray(skuPropDO.getValue2Json(), String.class);

        List<UserAssetVO.Value1VO> v1Assets = new ArrayList<>(value1s.size());
        for (String value1 : value1s) {
            List<GoodsSkuDO> v1Filters = skuDOS.stream().filter(o -> o.getPropValue1().equals(value1)).toList();
            if (CollectionUtils.isEmpty(v1Filters)) {
                continue;
            }

            List<UserAssetVO.Value2VO> v2Assets = new ArrayList<>(value2s.size());
            for (String value2 : value2s) {
                GoodsSkuDO skuDO = v1Filters.stream().filter(o -> o.getPropValue2().equals(value2)).findFirst().orElse(null);
                if (skuDO == null) {
                    continue;
                }

                List<UserAssetTokenDO> tokenDOSSub = tokenDOMap.get(skuDO.getSkuId());
                List<UserAssetVO.AssetTokenVO> tokenVOS = tokenDOSSub.stream().map(o -> {
                    UserAssetVO.AssetTokenVO tokenVO = BeanCopierUtils.copy(o, UserAssetVO.AssetTokenVO.class);
                    tokenVO.setTokenName(TokenUtils.tokenName(skuDO.getTokenName(), tokenVO.getTokenId()));
                    return tokenVO;
                }).toList();

                UserAssetVO.Value2VO value2VO = BeanCopierUtils.copy(skuDO, UserAssetVO.Value2VO.class);
                value2VO.setTokens(tokenVOS);

                v2Assets.add(value2VO);
            }

            UserAssetVO.Value1VO value1VO = new UserAssetVO.Value1VO();
            value1VO.setPropValue1(value1);
            value1VO.setV2Assets(v2Assets);

            v1Assets.add(value1VO);
        }

        UserAssetVO assetVO = new UserAssetVO();
        assetVO.setSkuProp(BeanCopierUtils.copy(skuPropDO, UserAssetVO.SkuPropVO.class));
        assetVO.setV1Assets(v1Assets);

        return ResponseUtils.success(assetVO);
    }


    @Operation(summary = "详情")
    @Parameters({
            @Parameter(name = "ctId", description = "合约编号", in = ParameterIn.QUERY),
            @Parameter(name = "tokenId", description = "tokenId", in = ParameterIn.QUERY),
    })
    @GetMapping("/detail")
    public ResponseVO<UserAssetTokenVO> detail(@RequestParam("ctId") Integer ctId, @RequestParam("tokenId") Long tokenId) {
        ParamsUtils.positiveInteger(ctId);
        ParamsUtils.positiveLong(tokenId);

        UserAssetTokenDO tokenDO = userAssetService.getByAddressCtIdTokenId(ThreadInfo.getInfo().getAddress(), ctId, tokenId);
        UserAssetTokenVO tokenVO = BeanCopierUtils.copy(tokenDO, UserAssetTokenVO.class);

        return ResponseUtils.success(tokenVO);
    }
}
