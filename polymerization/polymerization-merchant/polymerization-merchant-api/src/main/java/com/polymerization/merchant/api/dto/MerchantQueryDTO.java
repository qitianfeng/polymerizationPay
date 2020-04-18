package com.polymerization.merchant.api.dto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * # 商户查询DTO
 *
 */
@ApiModel(value = "MerchantQueryDTO", description = "商户查询条件")
@Data
public class MerchantQueryDTO implements Serializable {

    @ApiModelProperty("商户名称")
    private String merchantName;

    @ApiModelProperty("手机号")
    private String mobile;

    @ApiModelProperty("商户类型")
    private String merchantType;

    @ApiModelProperty("审核状态,0-未申请,1-已申请待审核,2-审核通过,3-审核拒绝")
    private String auditStatus;
}