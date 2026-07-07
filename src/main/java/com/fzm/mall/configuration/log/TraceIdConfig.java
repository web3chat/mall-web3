package com.fzm.mall.configuration.log;

import jakarta.servlet.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class TraceIdConfig {
    @Configuration
    public static class TraceIdFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            MDC.put("traceId", UUID.randomUUID().toString().substring(0, 8));
            try {
                chain.doFilter(request, response);
            } finally {
                MDC.clear();
            }
        }
    }

    @Aspect
    @Component
    public static class TraceIdTaskAspect {
        @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
        public Object addTraceId(ProceedingJoinPoint joinPoint) throws Throwable {
            MDC.put("traceId", UUID.randomUUID().toString().substring(0, 8));
            try {
                return joinPoint.proceed();
            } finally {
                MDC.clear();
            }
        }
    }

    @Aspect
    @Component
    public static class AsyncTraceIdAspect {
        @Around("@annotation(org.springframework.scheduling.annotation.Async)")
        public Object addTraceIdForAsync(ProceedingJoinPoint joinPoint) throws Throwable {
            // 从父线程复制MDC（如存在）
            Map<String, String> parentContextMap = MDC.getCopyOfContextMap();
            try {
                if (parentContextMap == null) {
                    MDC.put("traceId", UUID.randomUUID().toString().substring(0, 8));
                } else {
                    MDC.setContextMap(parentContextMap);
                }
                return joinPoint.proceed();
            } finally {
                MDC.clear();
            }
        }
    }
}
