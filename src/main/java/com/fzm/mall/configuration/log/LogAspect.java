package com.fzm.mall.configuration.log;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fzm.mall.constant.SystemConstant;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.constant.response.ValidateException;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.entity.dataobject.UserAdminDO;
import com.fzm.mall.entity.dataobject.UserAdminLogDO;
import com.fzm.mall.mapper.UserAdminLogMapper;
import com.fzm.mall.mapper.UserAdminMapper;
import com.fzm.mall.util.TimeUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 接口调用日志
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LogAspect {
    private static final ThreadLocal<Long> local = new ThreadLocal<>();
    private final static String front_uri_prefix = "/v/";
    private final HttpServletRequest request;
    private final UserAdminMapper userAdminMapper;
    private final UserAdminLogMapper userAdminLogMapper;

    @Pointcut(value = "execution(public * com.fzm.mall.controller..*.*(..))")
    public void logPointCut() {
    }

    @Before("logPointCut()")
    public void logBefore() {
        local.set(Instant.now().toEpochMilli());
    }

    @AfterReturning(pointcut = "logPointCut()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log(joinPoint, result, null);
    }

    @AfterThrowing(pointcut = "logPointCut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        log(joinPoint, null, e);
    }

    private void log(JoinPoint joinPoint, Object result, Throwable e) {
        // 请求参数
        JSONObject paramsObj = ParameterUtils.getParameter(joinPoint);
        // 响应参数
        JSONObject resultObj;
        if (e == null) {
            if (result instanceof ResponseVO) {
                resultObj = JSON.parseObject(JSON.toJSONString(result));
            } else {
                resultObj = new JSONObject();
            }
        } else {
            if (e instanceof ValidateException ve) {
                resultObj = JSON.parseObject(JSON.toJSONString(ResponseUtils.error(ve.getSource(), ve.getArgs())));
            } else {
                resultObj = new JSONObject();
                resultObj.put("error", e.getMessage());
            }
        }

        // 响应时间
        long duration = Instant.now().toEpochMilli() - local.get();

        console(duration, paramsObj.clone(), resultObj.clone());

        saveAdminLog(duration, paramsObj.clone(), resultObj.clone(), e);
    }

    private void console(long duration, JSONObject paramsObj, JSONObject resultObj) {
        resultObj.remove("data");

        Map<String, Object> logMsg = new HashMap<>();
        logMsg.put("clientIp", ThreadInfo.getInfo().getClientIp());
//        logMsg.put("userAgent", ThreadInfo.getInfo().getUserAgent());
//        logMsg.put("lang", ThreadInfo.getInfo().getLangEnum().getLocale().getLanguage());

        logMsg.put("uid", ThreadInfo.getInfo().getUid());
        logMsg.put("address", ThreadInfo.getInfo().getAddress());

        logMsg.put("method", request.getMethod());
        logMsg.put("uri", request.getRequestURI());
        logMsg.put("duration", duration);

        logMsg.put("params", paramsObj);
        logMsg.put("result", resultObj);

        log.info(JSON.toJSONString(logMsg));
    }

    private void saveAdminLog(long duration, JSONObject paramsObj, JSONObject resultObj, Throwable e) {
        String method = request.getMethod();
        if (method.equals("GET")) {
            return;
        }
        String uri = request.getRequestURI();
        if (uri.contains(front_uri_prefix) || uri.contains("/page") || uri.contains("/list")) {
            return;
        }

        UserAdminLogDO logDO = new UserAdminLogDO();

        logDO.setAddress(ThreadInfo.getInfo().getAddress());
        logDO.setParentAddress(ThreadInfo.getInfo().getParentAddress());
        logDO.setLang(ThreadInfo.getInfo().getLangEnum().getLocale().getLanguage());
        logDO.setDuration(duration);
        logDO.setMethod(method);
        logDO.setUri(uri);
        logDO.setParamsJson(paramsObj.toJSONString());
        logDO.setResultJson(resultObj.toJSONString());
        logDO.setClientIp(ThreadInfo.getInfo().getClientIp());
        logDO.setUserAgent(ThreadInfo.getInfo().getUserAgent());
        logDO.setCreateTime(TimeUtils.nowTimestamp());
        if (uri.contains("/login")) {
            if (e == null) {
                String address = paramsObj.getString("address");
                logDO.setAddress(address);
                logDO.setParentAddress(address);
                UserAdminDO adminDO = userAdminMapper.getByAddress(address);
                if (adminDO != null) {
                    logDO.setParentAddress(adminDO.getParentAddress());
                }
            }
            // 信息脱敏
            if (StringUtils.isNotBlank(paramsObj.getString("signature"))) {
                paramsObj.put("signature", "******");
                logDO.setParamsJson(paramsObj.toJSONString());
            }
            JSONObject dataObj = resultObj.getJSONObject("data");
            if (dataObj != null && StringUtils.isNotBlank(dataObj.getString(SystemConstant.Header.authorization_name))) {
                dataObj.put(SystemConstant.Header.authorization_name, "******");
                resultObj.put("data", dataObj);
                logDO.setResultJson(resultObj.toJSONString());
            }
        }

        if (StringUtils.isBlank(logDO.getAddress())) {
            return;
        }

        userAdminLogMapper.insert(logDO);
    }
}
