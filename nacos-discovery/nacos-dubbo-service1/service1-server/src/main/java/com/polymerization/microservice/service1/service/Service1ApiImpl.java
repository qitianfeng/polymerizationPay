package com.polymerization.microservice.service1.service;


import com.polymerizationPay.microservice.service1.api.Service1Api;
import com.polymerizationPay.microservice.service2.api.Service2Api;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;

@Service
public class Service1ApiImpl implements Service1Api {

    @Reference
    Service2Api service2Api;
    public String dubboService1() {

        String s = service2Api.dubboService2();
        return "dubboService1|"+s;
    }
}
