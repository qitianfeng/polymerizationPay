package com.polymerization.merchant.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data

@ApiModel(value = "StoreDTO",description = "")
public class StoreDTO {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("门店Id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 门店名称
     */
    @ApiModelProperty("门店名称")
    private String storeName;

    /**
     * 门店编号
     */
    @ApiModelProperty("门店编号")
    private Long storeNumber;

    /**
     * 所属商户
     */
    @ApiModelProperty("所属商户")
    private Long merchantId;

    /**
     * 父门店
     */
    @ApiModelProperty("父门店")
    private Long parentId;

    /**
     * 0表示禁用，1表示启用
     */
    @ApiModelProperty("0表示禁用，1表示启用")
    private Boolean storeStatus;



    /**
     * 门店地址
     */
    @ApiModelProperty("门店地址")
    private String storeAddress;

    @ApiModelProperty("门店管理员")
    private List<StaffDTO> staffs;
}
