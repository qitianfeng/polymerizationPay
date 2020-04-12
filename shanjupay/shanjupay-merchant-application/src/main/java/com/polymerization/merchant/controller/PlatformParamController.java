package com.polymerization.merchant.controller;


import com.polymerization.merchant.utils.SecurityUtil;
import com.shanjupay.common.util.StringUtil;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;
import com.shanjupay.transaction.api.dto.com.shanjupay.transaction.api.PayChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@Api(value = "商户平台‐渠道和支付参数相关", tags = "商户平台‐渠道和支付参数", description = "商户平台‐渠道和支付参数相关")
public class PlatformParamController {

    @Reference
    PayChannelService payChannelService;


    @GetMapping(value = "/my/platform-channels")
    @ApiOperation("获取平台服务类型")
    public List<PlatformChannelDTO> queryPlatformChannel() {
        return payChannelService.queryPlatformChannel();
    }

    @ApiOperation("根据平台服务类型获取支付渠道列表")
    @ApiImplicitParam(value = "服务类型编码", name = "platformChannelCode", required = true, dataType = "String", paramType = "path")
    @GetMapping(value = "/my/pay-channels/platform-channel/{platformChannelCode}")
    public List<PayChannelDTO> queryPayChannelByplatformChannel(@PathVariable("platformChannelCode") String platformChannelCode) {
        return payChannelService.queryPayChannelByPlatformChannel(platformChannelCode);
    }

    @ApiOperation("商户配置支付渠道")
    @ApiImplicitParams(
            {@ApiImplicitParam(value = "商户配置支付渠道参数", name = "payChannelParamDTO", required = true, dataType = "PayChannelParamDTO", paramType = "body")
            })
    @RequestMapping(value = "/my/pay-channels-params" ,method = {RequestMethod.POST,RequestMethod.PUT})
    public void createPayChannelParam(@RequestBody PayChannelParamDTO payChannelParamDTO) {
        Long merchantId = SecurityUtil.getMerchantId();
        payChannelParamDTO.setMerchantId(merchantId);
        payChannelService.savePayChannelParam(payChannelParamDTO);
    }
    @ApiOperation("获取指定应用指定服务类型下所包含的原始支付渠道参数列表")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "应用id",name = "appId",required = true,dataType = "String",paramType = "path"),
            @ApiImplicitParam(value = "服务类型",name = "platfoemChannel",required = true,dataType = "String",paramType = "path")
    })
    @GetMapping("/my/pay-channel-params/apps/{appId}/platform-channels/{platformChannel}")
    public List<PayChannelParamDTO> queryPayChannelParam(@PathVariable String appId,@PathVariable String platformChannel){
        return payChannelService.queryPayChannelParamByAppIdAndPlatform(appId,platformChannel);
    }
    @ApiOperation("获取指定应用指定服务类型下所包含的原始支付渠道参数列表")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "应用id",name = "appId",required = true,dataType = "String",paramType = "path"),
            @ApiImplicitParam(value = "服务类型",name = "platfoemChannel",required = true,dataType = "String",paramType = "path"),
            @ApiImplicitParam(value = "服务类型",name = "platfoemChannel",required = true,dataType = "String",paramType = "path")
    })
    @GetMapping("/my/pay-channel-params/apps/{appId}/platform-channels/{platformChannel}/pay-channels/{payChannel}")
    public PayChannelParamDTO queryPayChannelParam(@PathVariable String appId,@PathVariable String platformChannel,@PathVariable String payChannel){
        return payChannelService.queryParamByAppPlatformAndPayChannel(appId,platformChannel,payChannel);
    }



}
