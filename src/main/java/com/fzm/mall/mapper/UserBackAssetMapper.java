package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.UserBackAssetDO;
import com.fzm.mall.entity.queryobject.back.MUserBackAssetPageQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserBackAssetMapper {
    int insert(@Param("address") String address, @Param("coinType") Integer coinType, @Param("balance") BigDecimal balance, @Param("frozenBalance") BigDecimal frozenBalance);

    int updateBalance(@Param("address") String address, @Param("coinType") Integer coinType, @Param("changeBalance") BigDecimal changeBalance, @Param("changeFrozenBalance") BigDecimal changeFrozenBalance);

    UserBackAssetDO getAssetByAddressCoinType(@Param("address") String address, @Param("coinType") Integer coinType);

    List<UserBackAssetDO> listByPageQO(MUserBackAssetPageQO pageQO);
}
