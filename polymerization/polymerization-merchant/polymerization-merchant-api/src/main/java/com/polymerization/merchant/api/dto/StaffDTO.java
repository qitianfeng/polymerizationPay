package com.polymerization.merchant.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(value = "StaffDTO", description = "")
public class StaffDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("员工ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @ApiModelProperty("商户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long merchantId;

    @ApiModelProperty(value = "姓名")
    private String fullName;

    @ApiModelProperty(value = "职位")
    private String position;

    @ApiModelProperty(value = "用户名(关联统一用户)")
    private String username;

    @ApiModelProperty(value = "手机号(关联统一用户)")
    private String mobile;

    @ApiModelProperty(value = "员工所属门店")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long storeId;

    @ApiModelProperty(value = "最后一次登录时间")
    private LocalDateTime lastLoginTime;

    @ApiModelProperty(value = "0表示禁用，1表示启用")
    private Boolean staffStatus;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("员工所属门店名称")
    private String storeName;

    @ApiModelProperty("员工的角色")
    private List<StaffRoleDTO> roles;


}
