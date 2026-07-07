package com.fzm.mall.third.component;

import com.alibaba.fastjson2.JSON;
import com.fzm.mall.entity.properties.LogisticsProperties;
import com.fzm.mall.entity.viewobject.LogisticsVO;
import com.fzm.mall.third.aliyun.AliLogisticsUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LogisticsComponent {
    private final LogisticsProperties logisticsProperties;

    private static LogisticsVO defLogisticsVO(String expressCode) {
        String context = """
                [{
                    "time": "1744010437000",
                    "desc": "您的快件已由【门口】签收，如有疑问请联系派件员：180xxxx8859，有事先呼叫我，勿找平台，少一次投诉，多一份感恩，投诉电话：0571-2897xxxx"
                }, {
                    "time": "1744009117000",
                    "desc": "【杭州市】浙江杭州西湖区西部公司 的快递员(付亚威/180xxxx8859)正在为您派送(有事呼叫我，勿找平台，少一次投诉，多一份感恩)"
                }, {
                    "time": "1744004217000",
                    "desc": "【杭州市】浙江杭州西湖区西部公司 的快递员(付亚威/180xxxx8859)正在为您派送(有事呼叫我，勿找平台，少一次投诉，多一份感恩)，投诉电话：0571-2897xxxx，可放心接听95089申通专属派送号码"
                }, {
                    "time": "1744004207000",
                    "desc": "【杭州市】快件已到达 浙江杭州西湖区西部公司 咨询电话：0571-2897xxxx"
                }, {
                    "time": "1743982486000",
                    "desc": "【杭州市】快件已发往 浙江杭州西湖区西部公司"
                }, {
                    "time": "1743982289000",
                    "desc": "【杭州市】快件已到达 浙江杭州临平转运中心 "
                }, {
                    "time": "1743970091000",
                    "desc": "【杭州市】快件已发往 浙江杭州临平转运中心"
                }, {
                    "time": "1743969412000",
                    "desc": "【杭州市】快件已到达 浙江杭州转运中心 "
                }, {
                    "time": "1743951460000",
                    "desc": "【上海市】快件已发往 浙江杭州转运中心"
                }, {
                    "time": "1743950653000",
                    "desc": "【上海市】快件已到达 上海浦东转运中心 "
                }, {
                    "time": "1743913115000",
                    "desc": "【上海市】上海松江区浦南公司的蔡传阳(132xxxx3666) 已揽收"
                }]
                """;

        List<LogisticsVO.Detail> details = JSON.parseArray(context, LogisticsVO.Detail.class);

        LogisticsVO vo = new LogisticsVO();
        vo.setName("测试快递");
        vo.setCode(expressCode);
        vo.setDetails(details);
        return vo;
    }

    public List<LogisticsVO> getLogistics(String expressCode, String phone) {
        if (StringUtils.isBlank(expressCode)) {
            return Collections.emptyList();
        }

        String[] codes = expressCode.split(",");
        List<LogisticsVO> vos = new ArrayList<>();
        for (String code : codes) {
            if (logisticsProperties.getEnable()) {
                vos.add(AliLogisticsUtils.getLogistics(code, phone, logisticsProperties.getAppCode()));
            } else {
                vos.add(defLogisticsVO(code));
            }
        }

        return vos;
    }
}
