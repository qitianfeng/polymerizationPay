package com.polymerization.merchant.common.intercept;

import com.netflix.client.http.HttpResponse;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.ErrorCode;
import com.shanjupay.common.domain.RestErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;


@ControllerAdvice //全局异常处理器 可以实现对controller面向切面编程
public class GlobalExceptionHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    //捕获异常后处理方法
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse processException(HttpServletRequest request, HttpResponse response,Exception e){

        //如果是自定义异常则直接取出异常
        if (e instanceof BusinessException){
            LOGGER.info(e.getMessage());
            BusinessException businessException = (BusinessException)e;
            ErrorCode errorCode = businessException.getErrorCode();
            return new RestErrorResponse(errorCode.getDesc(),String.valueOf(errorCode.getCode()));
        }
        LOGGER.info("系统异常：",e);

        return new RestErrorResponse(CommonErrorCode.UNKOWN.getDesc(),String.valueOf(CommonErrorCode.UNKOWN.getCode()));
    }
}
