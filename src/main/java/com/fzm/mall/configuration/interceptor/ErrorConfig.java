package com.fzm.mall.configuration.interceptor;

import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/exception", produces = MediaType.APPLICATION_JSON_VALUE)
public class ErrorConfig {
    @RequestMapping("/404")
    public ResponseVO<Object> notFount(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
        return ResponseUtils.error(ResponseEnum.not_found);
    }
}
