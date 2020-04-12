package com.polymerization.merchant.controller;


import com.shanjupay.transaction.api.dto.com.shanjupay.transaction.api.PayChannelService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlatformParamController {

    @Reference
    PayChannelService payChannelService;

}
