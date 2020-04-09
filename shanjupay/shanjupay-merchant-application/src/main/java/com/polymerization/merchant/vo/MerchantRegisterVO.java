package com.polymerization.merchant.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "MerchantRegisterVO",description = "商户注册信息")
@Data
public class MerchantRegisterVO {

    @ApiModelProperty("商户手机号")
    private String mobile;

    @ApiModelProperty("商户用户名")
    private String username;
    @ApiModelProperty("商户密码")
    private String password;
    @ApiModelProperty("验证码的Key")
    private String verifyKey;
    @ApiModelProperty("验证码")
    private String verifyCode;
}
