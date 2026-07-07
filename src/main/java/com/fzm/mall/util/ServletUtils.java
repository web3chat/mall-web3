package com.fzm.mall.util;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet工具类
 */
public class ServletUtils {
    private ServletUtils() {
    }

    /**
     * 获得请求Query中的信息
     *
     * @param request 请求对象HttpServletRequest
     * @param name    KEY
     * @return Query中的信息
     */
    public static String getParameter(HttpServletRequest request, String name) {
        return getParameter(request, name, StandardCharsets.UTF_8);
    }

    public static String getParameter(HttpServletRequest request, String name, String charsetName) {
        return getParameter(request, name, StringUtils.isBlank(charsetName) ? StandardCharsets.UTF_8 : Charset.forName(charsetName));
    }

    public static String getParameter(HttpServletRequest request, String name, Charset charset) {
        String parameter = request.getParameter(name);
        if (StringUtils.isNotBlank(parameter)) {
            return new String(parameter.getBytes(StandardCharsets.ISO_8859_1), charset);
        }
        return null;
    }

    /**
     * 获得所有请求参数
     *
     * @param request 请求对象ServletRequest
     * @return Map params
     */
    public static Map<String, String[]> getParameters(ServletRequest request) {
        Map<String, String[]> map = request.getParameterMap();
        return Collections.unmodifiableMap(map);
    }

    /**
     * 获取客户端IP
     *
     * @param request          请求对象HttpServletRequest
     * @param otherHeaderNames 其他自定义头文件，通常在Http服务器（例如Nginx）中配置
     * @return IP地址 client ip
     */
    public static String getClientIP(HttpServletRequest request, String... otherHeaderNames) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        if (ArrayUtils.isNotEmpty(otherHeaderNames)) {
            ArrayUtils.addAll(headers, otherHeaderNames);
        }
        return getClientIPByHeader(request, headers);
    }

    /**
     * 获取客户端代理
     *
     * @param request 请求对象HttpServletRequest
     * @return 客户端代理 user agent
     */
    public static String getUserAgent(HttpServletRequest request) {
        return getHeader(request, "user-agent");
    }

    /**
     * 获取请求所有的头（header）信息
     *
     * @param request 请求对象HttpServletRequest
     * @return header值 header map
     */
    public static CaseInsensitiveMap<String, String> getHeaders(HttpServletRequest request) {
        CaseInsensitiveMap<String, String> headerMap = new CaseInsensitiveMap<>();

        Enumeration<String> names = request.getHeaderNames();
        String name;
        while (names.hasMoreElements()) {
            name = names.nextElement();
            headerMap.put(name, request.getHeader(name));
        }

        return headerMap;
    }

    /**
     * 获得请求header中的信息
     *
     * @param request 请求对象HttpServletRequest
     * @param name    头信息的KEY
     * @return header中的信息
     */
    public static String getHeader(HttpServletRequest request, String name) {
        return getHeader(request, name, StandardCharsets.UTF_8);
    }

    /**
     * 获得请求header中的信息
     *
     * @param request     请求对象HttpServletRequest
     * @param name        头信息的KEY
     * @param charsetName 字符集
     * @return header中的信息
     */
    public static String getHeader(HttpServletRequest request, String name, String charsetName) {
        return getHeader(request, name, StringUtils.isBlank(charsetName) ? StandardCharsets.UTF_8 : Charset.forName(charsetName));
    }

    /**
     * 获得请求header中的信息
     *
     * @param request 请求对象HttpServletRequest
     * @param name    头信息的KEY
     * @param charset 字符集
     * @return header中的信息
     */
    public static String getHeader(HttpServletRequest request, String name, Charset charset) {
        String header = request.getHeader(name);
        if (StringUtils.isNotBlank(header)) {
            return new String(header.getBytes(StandardCharsets.ISO_8859_1), charset);
        }
        return null;
    }

    /**
     * 是否为GET请求
     *
     * @param request 请求对象HttpServletRequest
     * @return 是否为GET请求 boolean
     */
    public static boolean isGetMethod(HttpServletRequest request) {
        return "GET".equals(request.getMethod());
    }

    /**
     * 是否为POST请求
     *
     * @param request 请求对象HttpServletRequest
     * @return 是否为POST请求 boolean
     */
    public static boolean isPostMethod(HttpServletRequest request) {
        return "POST".equals(request.getMethod());
    }

    /**
     * 是否为Multipart类型表单，此类型表单用于文件上传
     *
     * @param request 请求对象HttpServletRequest
     * @return 是否为Multipart类型表单 ，此类型表单用于文件上传
     */
    public static boolean isMultipart(HttpServletRequest request) {
        if (!isPostMethod(request)) {
            return false;
        }

        String contentType = request.getContentType();
        if (StringUtils.isBlank(contentType)) {
            return false;
        }
        return contentType.toLowerCase().startsWith("multipart/");
    }

    /**
     * 获得指定的Cookie
     *
     * @param httpServletRequest HttpServletRequest
     * @param name               cookie名
     * @return Cookie对象 cookie
     */
    public static Cookie getCookie(HttpServletRequest httpServletRequest, String name) {
        return getCookieMap(httpServletRequest).get(name);
    }

    /**
     * 将cookie封装到Map里面
     *
     * @param httpServletRequest HttpServletRequest
     * @return Cookie map
     */
    public static Map<String, Cookie> getCookieMap(HttpServletRequest httpServletRequest) {
        Cookie[] cookies = httpServletRequest.getCookies();
        if (ArrayUtils.isEmpty(cookies)) {
            return Collections.emptyMap();
        }
        Map<String, Cookie> map = new HashMap<>(cookies.length);
        for (Cookie cookie : cookies) {
            map.put(cookie.getName(), cookie);
        }
        return map;
    }

    /**
     * 设定返回给客户端的Cookie
     *
     * @param response 响应对象HttpServletResponse
     * @param cookie   Servlet Cookie对象
     */
    public static void addCookie(HttpServletResponse response, Cookie cookie) {
        response.addCookie(cookie);
    }

    /**
     * 设定返回给客户端的Cookie
     *
     * @param response 响应对象{@link HttpServletResponse}
     * @param name     Cookie名
     * @param value    Cookie值
     */
    public static void addCookie(HttpServletResponse response, String name, String value) {
        response.addCookie(new Cookie(name, value));
    }

    /**
     * 设定返回给客户端的Cookie
     * Path: "/"
     * No Domain
     *
     * @param response        响应对象{@link HttpServletResponse}
     * @param name            cookie名
     * @param value           cookie值
     * @param maxAgeInSeconds -1: 关闭浏览器清除Cookie. 0: 立即清除Cookie. >0 : Cookie存在的秒数.
     */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAgeInSeconds) {
        addCookie(response, name, value, maxAgeInSeconds, "/", null);
    }

    /**
     * 设定返回给客户端的Cookie
     *
     * @param response        响应对象{@link HttpServletResponse}
     * @param name            cookie名
     * @param value           cookie值
     * @param maxAgeInSeconds -1: 关闭浏览器清除Cookie. 0: 立即清除Cookie. >0 : Cookie存在的秒数.
     * @param path            Cookie的有效路径
     * @param domain          域名
     */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAgeInSeconds, String path, String domain) {
        Cookie cookie = new Cookie(name, value);
        if (domain != null) {
            cookie.setDomain(domain);
        }
        cookie.setMaxAge(maxAgeInSeconds);
        cookie.setPath(path);
        addCookie(response, cookie);
    }

    /**
     * 获取客户端IP
     *
     * @param request     请求对象HttpServletRequest
     * @param headerNames 自定义头，通常在Http服务器（例如Nginx）中配置
     * @return IP地址
     */
    private static String getClientIPByHeader(HttpServletRequest request, String... headerNames) {
        String ip;
        for (String header : headerNames) {
            ip = request.getHeader(header);
            if (isNotUnknown(ip)) {
                return getMultistageReverseProxyIp(ip);
            }
        }

        ip = request.getRemoteAddr();
        return getMultistageReverseProxyIp(ip);
    }

    /**
     * 检测给定字符串是否为未知，多用于检测HTTP请求相关
     *
     * @param checkString 被检测的字符串
     * @return 是否未知
     */
    private static boolean isNotUnknown(String checkString) {
        return StringUtils.isNotBlank(checkString) && !"unknown".equalsIgnoreCase(checkString);
    }

    /**
     * 从多级反向代理中获得第一个非unknown IP地址
     *
     * @param ip 获得的IP地址
     * @return 第一个非unknown IP地址
     */
    private static String getMultistageReverseProxyIp(String ip) {
        // 多级反向代理检测
        if (ip != null && ip.indexOf(",") > 0) {
            String[] ips = ip.trim().split(",");
            for (String subIp : ips) {
                if (isNotUnknown(subIp)) {
                    ip = subIp;
                    break;
                }
            }
        }
        return ip;
    }
}
