package com.fzm.mall.third.aliyun;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fzm.mall.entity.viewobject.LogisticsVO;
import com.fzm.mall.util.HttpUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AliLogisticsUtils {
    // https://market.aliyun.com/detail/cmapi00066593
    private static final String expressUrl = "https://kzexpress.market.alicloudapi.com/api-mall/api/express/query";

    public static LogisticsVO getLogistics(String expressCode, String phone, String appCode) {
        if (StringUtils.isAnyBlank(expressCode, phone, appCode)) {
            return null;
        }

        String execute = HttpUtils.url(expressUrl)
                .addHeader("Authorization", "APPCODE " + appCode)
                .addParam("expressNo", expressCode)
                .addParam("mobile", phoneSuffix(phone))
                .setMethod(HttpUtils.MethodEnum.GET)
                .execute();

        JSONObject obj = JSON.parseObject(execute);
        if (obj == null) {
            return noMessage(expressCode);
        }

        Integer resCode = obj.getInteger("code");
        if (resCode == null || resCode != 200) {
            return noMessage(expressCode);
        }

        JSONObject data = obj.getJSONObject("data");

        JSONArray detailList = data.getJSONArray("logisticsTraceDetailList");
        if (CollectionUtils.isEmpty(detailList)) {
            return noMessage(expressCode);
        }


        List<LogisticsVO.Detail> details = new ArrayList<>(detailList.size());
        for (int i = 0; i < detailList.size(); i++) {
            JSONObject detailObj = detailList.getJSONObject(i);

            LogisticsVO.Detail detail = new LogisticsVO.Detail();
            detail.setTime(detailObj.getLong("time"));
            detail.setDesc(detailObj.getString("desc"));
            details.add(detail);
        }

        LogisticsVO vo = new LogisticsVO();
        vo.setName(data.getString("logisticsCompanyName"));
        vo.setCode(data.getString("mailNo"));
        vo.setStatusDesc(data.getString("logisticsStatusDesc"));
        vo.setDetails(details);

        return vo;
    }

    private static String phoneSuffix(String phone) {
        if (StringUtils.isBlank(phone) || phone.length() < 4) {
            return "";
        }
        return phone.substring(phone.length() - 4);
    }

    private static LogisticsVO noMessage(String expressCode) {
        LogisticsVO vo = new LogisticsVO();
        vo.setCode(expressCode);
        vo.setStatusDesc("无物流信息");
        return vo;
    }
}
