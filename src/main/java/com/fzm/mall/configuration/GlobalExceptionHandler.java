package com.fzm.mall.configuration;

import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.constant.response.ValidateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.EOFException;
import java.sql.SQLException;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 自定义异常
     */
    @ExceptionHandler(ValidateException.class)
    public ResponseEntity<Object> handleValidateException(ValidateException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error(e.getSource(), e.getArgs()));
    }


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error(e.getMessage()));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error(e.getMessage()));
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ResponseVO<Object>> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error(e.getMessage()));
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error(e.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error(e.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error(e.getMessage()));
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error(e.getMessage()));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error(e.getMessage()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error(ResponseEnum.not_found));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error(e.getMessage()));
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error(e.getMessage()));
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<Object> handleErrorResponseException(ErrorResponseException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error(e.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error(e.getMessage()));
    }

    @ExceptionHandler(ConversionNotSupportedException.class)
    public ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error(e.getMessage()));
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(TypeMismatchException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error(e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error("Failed to read request"));
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error("Failed to write request"));
    }

    @ExceptionHandler(MethodValidationException.class)
    public ResponseEntity<Object> handleMethodValidationException(MethodValidationException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error("Method Validation failed"));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Object> handleMultipartException(MultipartException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error(e.getMessage()));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception e) {
        if (e instanceof EOFException
                || e.toString().contains("Connection reset by peer")
                || e.toString().contains("Broken pipe")) {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error(ResponseEnum.too_many_requests_plz_try_again_later));
        }

        if (e instanceof SQLException) {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error("Database error"));
        }

        log.error("Internal error.", e);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseUtils.error("Internal error"));
    }
}
