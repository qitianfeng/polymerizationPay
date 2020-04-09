package com.polymerization.merchant.controller;

import com.polymerization.merchant.api.MerchantService;
import com.polymerization.merchant.api.dto.MerchantDTO;
import io.swagger.annotations.Api;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import sun.rmi.runtime.Log;

@RestController

@Api(value = "",tags = "",description = "")
public class MerchantController {

    @Reference
    MerchantService merchantService;

    @GetMapping("/merchants/{id}")
    public MerchantDTO queryMerchantById(@PathVariable("id")Long id){
        MerchantDTO merchantDTO = merchantService.queryMerchantById(id);
        return merchantDTO;
    }
}
