package com.polymerization.merchant.controller;

import com.polymerization.merchant.api.AppService;
import com.polymerization.merchant.api.MerchantService;
import com.polymerization.merchant.api.dto.AppDTO;
import com.polymerization.merchant.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(value = "商户平台-应用管理",tags = "商户平台-应用相关",description = "商户平台-应用相关")
public class AppController {

    @Reference
    AppService appService;

    @Reference
    MerchantService merchantService;

    @ApiOperation("商户创建应用")
//    @ApiImplicitParam(value = "appDTO",name = "应用信息",required = true,dataType = "String",paramType = "body")
    @PostMapping("my/apps")
    public AppDTO createApp(@RequestBody AppDTO appDTO){
        Long merchantId = SecurityUtil.getMerchantId();

        return appService.createApp(merchantId,appDTO);
    }

    @ApiOperation("查询商户下的应用列表")
    @GetMapping("/my/apps")
    public List<AppDTO> queryMyApps() {
        Long merchantId = SecurityUtil.getMerchantId();
        List<AppDTO> appDTOS = appService.queryAppByMerchant(merchantId);
        return appDTOS;
    }
    @ApiOperation("根据appid获取应用的详细信息")
    @ApiImplicitParam(name = "appId",value = "商户应用id",dataType = "String",paramType = "query")
    @GetMapping("/my/apps/{appId}")
    public AppDTO getApp(@PathVariable String appId){
        AppDTO appById = appService.getAppById(appId);
        return appById;
    }

}
