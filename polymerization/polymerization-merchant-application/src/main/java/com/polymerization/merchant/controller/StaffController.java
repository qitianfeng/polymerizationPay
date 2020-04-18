package com.polymerization.merchant.controller;

import com.polymerization.merchant.api.MerchantService;
import com.polymerization.merchant.api.dto.StaffDTO;
import com.polymerization.merchant.common.utils.SecurityUtil;
import com.polymerization.common.domain.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.web.bind.annotation.*;

@Api(value = "商户平台-成员(员工)管理", tags = "商户平台-成员管理", description = "商户平台-员工的增删改查")
@RestController
public class StaffController {
    @Reference
    private MerchantService merchantService;

    @ApiOperation("分页查询商户下员工")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "staffDTO",value = "员工参数",required = true,dataType = "StaffDTO",paramType = "body"),
            @ApiImplicitParam(name = "pageNo",value = "页码",required = true,dataType = "Integer",paramType = "query"),
            @ApiImplicitParam(name = "pageSize",value = "每页记录数",required = true,dataType = "Integer",paramType = "query")
    })
    @PostMapping("/my/staff/page")
    public PageVO<StaffDTO>  queryStaffByPage(@RequestBody StaffDTO staffDTO, @RequestParam Integer pageNo, @RequestParam Integer pageSize){
        Long merchantId = SecurityUtil.getMerchantId();
        staffDTO.setMerchantId(merchantId);
        PageVO<StaffDTO> staff = merchantService.queryStaffByPage(staffDTO, pageNo, pageSize);
        return staff;
    }
    @ApiOperation("在某商户下创建员工和账号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "staff", value = "员工信息", required = true, dataType = "StaffDTO", paramType = "body"),
            @ApiImplicitParam(name = "roleCodes", value = "角色编码", required = true, allowMultiple = true, dataType = "String", paramType = "query")})
    @PostMapping(value = "/my/staffs")
    public void createStaffAndAccount(@RequestBody StaffDTO staff, @RequestParam String[] roleCodes) {
        Long merchantId = SecurityUtil.getMerchantId();
        staff.setMerchantId(merchantId);
        merchantService.createStaffAndAccount(staff, roleCodes);
    }

    @ApiOperation("修改某员工信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "staff", value = "员工信息,账号和用户名不可修改", required = true, dataType = "StaffDTO", paramType = "body"),
            @ApiImplicitParam(name = "roleCodes", value = "角色编码", allowMultiple = true, dataType = "String", paramType = "query")})
    @PutMapping("/my/staffs")
    public void modifyStaff(@RequestBody StaffDTO staff, @RequestParam String[] roleCodes) {
        merchantService.modifyStaff(staff, roleCodes);
    }
    @ApiOperation("删除某员工")
    @ApiImplicitParam(name = "id", value = "员工id", required = true, dataType = "Long", paramType = "path")
    @DeleteMapping("/my/staffs/{id}")
    public void removeStaff(@PathVariable Long id) {
        merchantService.removeStaff(id);
    }

}
