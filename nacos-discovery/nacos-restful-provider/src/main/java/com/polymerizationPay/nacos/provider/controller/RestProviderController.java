package com.polymerizationPay.nacos.provider.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestProviderController {
    @GetMapping(value = "/service")
    public String service(){
        System.out.println("provider invoke");
        return "provice invoke";
    }
}
