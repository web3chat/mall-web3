package com.fzm.mall.constant.enums;

import com.fzm.mall.util.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Locale;

@Getter
@AllArgsConstructor
public enum LangEnum {
    English(Locale.ENGLISH, "英文"),
    Simplified_Chinese(Locale.CHINESE, "简体中文"),
    Traditional_Chinese(new Locale("hk"), "繁體中文"),

    ;

    private final Locale locale;
    private final String name;


    public static LangEnum exist(String lang) {
        if (StringUtils.isBlank(lang)) {
            return null;
        }
        return Arrays.stream(values()).filter(v -> v.getLocale().getLanguage().equals(lang)).findFirst().orElse(null);
    }

    public static LangEnum getByRequest(HttpServletRequest request) {
        // 优先级 1：从 URL 参数获取语言（如 ?lang=en）
        String lang = ServletUtils.getParameter(request, "lang");
        // 优先级 2：从 lang 请求头解析
        lang = StringUtils.isNotBlank(lang) ? lang : ServletUtils.getHeader(request, "lang");
        // 优先级 3：从 Accept-Language 请求头解析
        lang = StringUtils.isNotBlank(lang) ? lang : ServletUtils.getHeader(request, "Accept-Language");

        // 默认en
        LangEnum langEnum = LangEnum.exist(lang);
        return langEnum == null ? Simplified_Chinese : langEnum;
    }
}
