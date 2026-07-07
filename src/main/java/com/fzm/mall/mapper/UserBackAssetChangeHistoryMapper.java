package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.UserBackAssetChangeHistoryDO;
import com.fzm.mall.entity.queryobject.back.MUserBackAssetLogPageQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserBackAssetChangeHistoryMapper {
    int insert(UserBackAssetChangeHistoryDO historyDO);

    int updateLogTypeByLogId(@Param("logId") Long logId, @Param("fromLogType") Integer fromLogType, @Param("toLogType") Integer toLogType, @Param("extendData") String extendData);

    List<UserBackAssetChangeHistoryDO> listByPageQO(MUserBackAssetLogPageQO pageQO);

    List<UserBackAssetChangeHistoryDO> listByLogType(@Param("logType") Integer logType);
}
