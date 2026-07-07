package com.fzm.mall.configuration.log;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;

/**
 * 获取请求参数
 */
public class ParameterUtils {

    /**
     * 获取请求参数
     */
    public static JSONObject getParameter(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Object[] args = joinPoint.getArgs();
        JSONObject params = new JSONObject(args.length);

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
            if (requestBody != null) {
                // 将RequestBody参数对项转换为map并添加至请求参数map
                Object arg = args[i];
                if (arg.getClass().isArray() || arg instanceof List) {
                    params.put("array", JSON.parseArray(JSON.toJSONString(arg)));
                } else {
                    params.putAll(JSON.parseObject(JSON.toJSONString(arg), new TypeReference<HashMap<String, Object>>() {
                    }));
                }
            }

            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
            if (requestParam != null) {
                String key = parameters[i].getName();
                if (StringUtils.isNotBlank(requestParam.value())) {
                    key = requestParam.value();
                }
                params.put(key, args[i]);
            }

            PathVariable pathVariable = parameters[i].getAnnotation(PathVariable.class);
            if (pathVariable != null) {
                String key = parameters[i].getName();
                if (StringUtils.isNotBlank(pathVariable.value())) {
                    key = pathVariable.value();
                }
                params.put(key, args[i]);
            }
        }
        return params;
    }
}
