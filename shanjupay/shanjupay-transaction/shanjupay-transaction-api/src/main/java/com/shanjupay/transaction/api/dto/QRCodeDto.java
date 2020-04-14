package com.shanjupay.transaction.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@NoArgsConstructor
@Data
public class QRCodeDto implements Serializable {
    @ApiModelProperty("商户id")
    private Long merchantId;
    @ApiModelProperty("应用id")
    private String appId;
    @ApiModelProperty("门店id")
    private Long storeId;
    @ApiModelProperty("商品标题")
    private String subject;
    @ApiModelProperty("订单描述")
    private String body;
}
