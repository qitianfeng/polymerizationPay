package com.polymerizationPay.nacos.consumer.controller;

import com.polymerizationPay.microservice.service1.api.Service1Api;
import com.polymerizationPay.microservice.service2.api.Service2Api;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@RestController
public class RestConsumerController {
    @Value("${provider.address}")
    private String providerAddress;

    @Reference
    Service2Api service2Api;

    //服务id即注册中心的服务名
    private String serviceId = "nacos-restful-provider";

    @Autowired
    LoadBalancerClient loadBalancerClient;

    @GetMapping(value = "/service")
    public String service(){
        RestTemplate restTemplate = new RestTemplate();
        //调用服务
        String s = restTemplate.getForObject("http://" + providerAddress + "/service", String.class);
        return "consumer invoke | "+ s;
    }

    @GetMapping(value = "/service1")
    public String service1(){
        RestTemplate restTemplate = new RestTemplate();
        //调用服务
        ServiceInstance choose = loadBalancerClient.choose(serviceId);
        URI uri = choose.getUri();
        String s = restTemplate.getForObject(uri+ "/service", String.class);

        return "consumer invoke | "+ s;
    }
    @GetMapping(value = "/service2")
    public String service2(){
        String s = service2Api.dubboService2();
        return "consumer invoke | "+ s;
    }
    @Reference
    Service1Api service1Api;
    @GetMapping(value = "/service3")
    public String service3(){
        String s = service1Api.dubboService1();
        return "consumer invoke | "+ s;
    }
    //注入上下文
    @Autowired
    ConfigurableApplicationContext configurableApplicationContext;
    @GetMapping(value = "/config")
    public String config(){

        return configurableApplicationContext.getEnvironment().getProperty("common.name");
    }
}
