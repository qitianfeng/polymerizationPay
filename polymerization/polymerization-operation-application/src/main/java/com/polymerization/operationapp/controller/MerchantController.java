package com.polymerization.operationapp.controller;

import com.polymerization.merchant.api.MerchantService;
import com.polymerization.merchant.api.dto.MerchantDTO;
import com.polymerization.merchant.api.dto.MerchantQueryDTO;
import com.polymerization.common.domain.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

@Api(value = "运营平台-企业管理审核", tags = "企业管理审核", description = "企业管理审核")
@RestController
public class MerchantController {

    @Reference
    private MerchantService merchantService;

    @ApiOperation("商户列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "merchantQueryDTO", value = "商户查询条件", required = true, dataType = "MerchantQueryDTO", paramType = "body"),
            @ApiImplicitParam(name = "pageNo", value = "页码", required = true, dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页记录数", required = true, dataType = "Integer", paramType = "query")
    })
    @PostMapping("/m/merchants/page")
    public PageVO<MerchantDTO> queryMerchant(@RequestBody MerchantQueryDTO merchantQueryDTO, @RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {
        return merchantService.queryMerchantPage(merchantQueryDTO, pageNo, pageSize);

    }

    @ApiOperation("根据id商户获取商户信息")
    @ApiImplicitParam(name = "id", value = "商户ID", required = true, dataType = "Long", paramType = "query")
    public MerchantDTO getMerchant(@RequestParam("id") Long id) {
        return merchantService.queryMerchantById(id);
    }

    @ApiOperation("商户资质申请审核")
    @PostMapping("/m/merchant/audit/{auditStatus}")
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(name = "merchantId",value = "商户ID",required = true,dataType = "Long",paramType = "query"),
                    @ApiImplicitParam(name = "auditStatus",value = "审核状态 2-审核通过,3-审核拒绝",required = true,dataType = "String",paramType = "path")
            }
    )
    public void auditStatus(@RequestParam("merchantId") Long merchantId, @PathVariable String auditStatus) {
        merchantService.verifyMerchant(merchantId,auditStatus);
    }

}
