package com.polymerization.merchant.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "AppDTO",description = "")
@Data
public class AppDTO implements Serializable {
    @ApiModelProperty("应用id，新增无需传入")
    private String appId;

    @ApiModelProperty("应用名称")
    private String appName;

    @ApiModelProperty("商户ID")
    private Long merchantId;

    @ApiModelProperty("应用公钥")
    private String publicKey;

    @ApiModelProperty("支付回调应用url，创建时可不填")
    private String notifyUrl;

}
