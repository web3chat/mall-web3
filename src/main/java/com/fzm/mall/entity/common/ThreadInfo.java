package com.fzm.mall.entity.common;

import com.fzm.mall.constant.enums.LangEnum;

public class ThreadInfo {
    private static final ThreadLocal<ReqInfo> local_info = new ThreadLocal<>();

    public static ReqInfo getInfo() {
        ReqInfo reqInfo = local_info.get();
        if (reqInfo == null) {
            reqInfo = new ReqInfo();
            reqInfo.setLangEnum(LangEnum.English);
        }
        return reqInfo;
    }

    public static void setInfo(ReqInfo reqInfo) {
        local_info.set(reqInfo);
    }

    public static void delInfo() {
        local_info.remove();
    }
}
