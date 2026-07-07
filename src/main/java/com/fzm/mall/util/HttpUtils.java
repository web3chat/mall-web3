package com.fzm.mall.util;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * http工具类
 */
@Slf4j
public class HttpUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final MediaType JSON_UTF8 = MediaType.parse("application/json;charset=UTF-8");
    private final OkHttpClient DEFAULT_CLIENT = new OkHttpClient().newBuilder()
//            .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890)))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    private String url;
    private OkHttpClient client;
    private Map<String, Object> headers;
    private Map<String, Object> params;
    private MethodEnum methodEnum = MethodEnum.GET;
    private MediaTypeEnum mediaTypeEnum = MediaTypeEnum.JSON;

    private HttpUtils() {
    }

    public static HttpUtils url(String url) {
        HttpUtils httpUtils = new HttpUtils();
        httpUtils.url = url;
        return httpUtils;
    }

    public HttpUtils client(OkHttpClient client) {
        this.client = client;
        return this;
    }

    public HttpUtils addHeader(String key, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(key, value);
        return this;
    }

    public HttpUtils setHeaders(Map<String, Object> headers) {
        this.headers = headers;
        return this;
    }

    @SuppressWarnings("unchecked")
    public HttpUtils setHeaders(Object obj) {
        if (obj == null) {
            this.headers = new HashMap<>();
        } else if (obj instanceof Map) {
            this.headers = (Map<String, Object>) obj;
        } else {
            this.headers = objectMapper.convertValue(obj, objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
        }
        return this;
    }

    public HttpUtils clearHeaders() {
        this.headers = null;
        return this;
    }

    public HttpUtils addParam(String key, Object value) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(key, value);
        return this;
    }

    public HttpUtils setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    @SuppressWarnings("unchecked")
    public HttpUtils setParams(Object obj) {
        if (obj == null) {
            this.params = new HashMap<>();
        } else if (obj instanceof Map) {
            this.params = (Map<String, Object>) obj;
        } else {
            this.params = objectMapper.convertValue(obj, objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
        }
        return this;
    }

    public HttpUtils clearParams() {
        this.params = null;
        return this;
    }

    public HttpUtils setMethod(MethodEnum methodEnum) {
        this.methodEnum = methodEnum;
        return this;
    }

    public HttpUtils setMediaType(MediaTypeEnum mediaTypeEnum) {
        this.mediaTypeEnum = mediaTypeEnum;
        return this;
    }

    public String execute() {
        Request.Builder requestBuilder = new Request.Builder();
        if (headers != null) {
            headers.forEach((k, v) -> requestBuilder.addHeader(k, v.toString()));
        }

        // POST
        if (methodEnum == MethodEnum.POST) {
            // Form
            if (mediaTypeEnum == MediaTypeEnum.FORM) {
                FormBody.Builder formBuilder = new FormBody.Builder();
                if (params != null) {
                    params.forEach((k, v) -> formBuilder.add(k, v.toString()));
                }
                FormBody body = formBuilder.build();

                requestBuilder.url(url).post(body);
            }
            // 默认：JSON
            else {
                String json = params == null ? "{}" : JSON.toJSONString(params);
                RequestBody body = RequestBody.create(json, JSON_UTF8);
                requestBuilder.url(url).post(body);
            }
        }
        // 默认：GET
        else {
            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
            if (params != null) {
                params.forEach((k, v) -> urlBuilder.addQueryParameter(k, v.toString()));
            }
            requestBuilder.url(urlBuilder.build()).get();
        }

        if (client == null) {
            client = DEFAULT_CLIENT;
        }

        try (Response response = client.newCall(requestBuilder.build()).execute()) {
            if (response.isSuccessful()) {
                try (ResponseBody body = response.body()) {
                    return body == null ? "" : body.string();
                }
            } else {
                logError(response.message());
            }
        } catch (Exception e) {
            logError(e.getMessage());
        }
        return null;
    }

    private void logError(String error) {
        Map<String, Object> map = new HashMap<>();
        map.put("url", url);
        map.put("headers", headers);
        map.put("params", params);
        map.put("method", methodEnum.name());
        map.put("mediaType", mediaTypeEnum.name());
        map.put("error", error);

        log.error("HTTP Execute Error. {}", JSON.toJSONString(map));
    }

    public enum MethodEnum {
        GET, POST
    }

    public enum MediaTypeEnum {
        JSON, FORM
    }
}
