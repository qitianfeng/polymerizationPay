package com.polymerization.common.domain;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "RestErrorResponse",description = "错误响应参数包装")
public class RestErrorResponse {
    private String errCode;
    private String errMessage;

    public RestErrorResponse(String errCode, String errMessage) {
        this.errCode = errCode;
        this.errMessage = errMessage;
    }
}
