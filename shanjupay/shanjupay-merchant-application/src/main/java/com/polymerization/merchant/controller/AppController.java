package com.polymerization.merchant.controller;

import com.polymerization.merchant.api.AppService;
import com.polymerization.merchant.api.MerchantService;
import com.polymerization.merchant.api.dto.AppDTO;
import com.polymerization.merchant.common.utils.SecurityUtil;
import com.shanjupay.transaction.api.dto.com.shanjupay.transaction.api.PayChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(value = "商户平台-应用管理", tags = "商户平台-应用相关", description = "商户平台-应用相关")
public class AppController {

    @Reference
    AppService appService;

    @Reference
    MerchantService merchantService;
    @Reference
    PayChannelService payChannelService;

    @ApiOperation("商户创建应用")
//    @ApiImplicitParam(value = "appDTO",name = "应用信息",required = true,dataType = "String",paramType = "body")
    @PostMapping("my/apps")
    public AppDTO createApp(@RequestBody AppDTO appDTO) {
        Long merchantId = SecurityUtil.getMerchantId();

        return appService.createApp(merchantId, appDTO);
    }

    @ApiOperation("查询商户下的应用列表")
    @GetMapping("/my/apps")
    public List<AppDTO> queryMyApps() {
        Long merchantId = SecurityUtil.getMerchantId();
        List<AppDTO> appDTOS = appService.queryAppByMerchant(merchantId);
        return appDTOS;
    }

    @ApiOperation("根据appid获取应用的详细信息")
    @ApiImplicitParam(name = "商户应用id", value = "appId", dataType = "String", paramType = "query")
    @GetMapping("/my/apps/{appId}")
    public AppDTO getApp(@PathVariable String appId) {
        AppDTO appById = appService.getAppById(appId);
        return appById;
    }

    @ApiOperation("为应用绑定服务类型")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "应用Id", name = "appId", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(value = "platformChannelCodes", name = "服务类型code", required = true, dataType = "String", paramType = "query")
    })
    @PostMapping("/my/apps/{appId}/platform-channels")
    public void bindPlatformForApp(@PathVariable String appId, @RequestParam("platformChannelCodes") String platformChannelCodes) {
        payChannelService.bindPlatformChannelForApp(appId, platformChannelCodes);
    }

    @ApiOperation("查询应用是否绑定了某个服务类型")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "应用ID" ,name = "appId",required = true,dataType = "String",paramType = "query"),
            @ApiImplicitParam(value = "服务类型",name = "platformChannel",required = true,dataType = "String",paramType = "query")
    })
    @GetMapping("/my/merchants/apps/platformchannels")
    public Integer queryAppBindPlatformChannel(@RequestParam("appId") String appId, @RequestParam("platformChannel") String platformChannel) {
        return payChannelService.queryAppBindPlatformChannel(appId, platformChannel);
    }

}
