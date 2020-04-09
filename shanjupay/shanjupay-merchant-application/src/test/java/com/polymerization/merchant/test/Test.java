package com.polymerization.merchant.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class Test {

    @Autowired
    RestTemplate restTemplate;

    @org.junit.Test
    public void tets(){

        String url =  "http://localhost:56085/sailing/generate?name=sms&effectiveTime=600";
        log.info("调用短信访问url:{}",url);
        //请求体
        Map<String,Object> body = new LinkedHashMap<>();
        String phone = "13642440515";
        body.put("mobile",phone);
        //请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        //设置数为json
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        //封装请求参数
        HttpEntity entity  = new HttpEntity<>(body,httpHeaders);

        Map responseMap = null;

        try{
            //post请求
            ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            //获取响应
            Map exchangeBody = exchange.getBody();
        }catch (Exception e){

            //取出exchangeBody的数据
            if (responseMap != null || responseMap.get("result") != null){
                Map result = (Map) responseMap.get("result");
                String string = result.get("key").toString();
                System.out.println(string);
            }
        }


    }
}
