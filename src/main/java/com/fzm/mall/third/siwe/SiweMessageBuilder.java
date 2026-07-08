package com.fzm.mall.third.siwe;

import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * SIWE 待签名消息构建器：纯静态工具，按固定模板拼接原文。
 */
public final class SiweMessageBuilder {

    private SiweMessageBuilder() {
    }

    public static final String DOMAIN_FIELD = "想要您使用该地址进行签名：";
    public static final String TOS_FIELD = "请签名此消息以验证您对该地址的控制权，并接受我们的服务条款：https://";
    public static final String PP_FIELD = "和隐私政策：https://";
    public static final String URI_FIELD = "网站：";
    public static final String VERSION_FIELD = "版本：1";
    public static final String NONCE_FIELD = "随机数：";
    public static final String INVITE_CODE_FIELD = "邀请码：";
    public static final String ISSUED_AT_FIELD = "签发时间：";
    public static final String EXPIRE_AT_FIELD = "过期时间：";

    /**
     * 按固定模板构建 SIWE 原文（含可选邀请码行）。
     */
    public static String build(String domain,
                               String address,
                               String inviteCode,
                               String nonce,
                               OffsetDateTime issuedAt,
                               OffsetDateTime expireAt,
                               DateTimeFormatter formatter) {
        List<String> lines = new ArrayList<>();
        lines.add(domain + DOMAIN_FIELD);
        lines.add(address);
        lines.add("");
        lines.add(TOS_FIELD + domain + "/tos");
        lines.add(PP_FIELD + domain + "/privacy-policy");
        lines.add("");
        lines.add(URI_FIELD + "https://" + domain);
        lines.add(VERSION_FIELD);
        lines.add(NONCE_FIELD + nonce);
        if (StringUtils.isNotBlank(inviteCode)) {
            lines.add(INVITE_CODE_FIELD + inviteCode);
        }
        lines.add(ISSUED_AT_FIELD + issuedAt.format(formatter));
        lines.add(EXPIRE_AT_FIELD + expireAt.format(formatter));
        return String.join("\n", lines);
    }
}
