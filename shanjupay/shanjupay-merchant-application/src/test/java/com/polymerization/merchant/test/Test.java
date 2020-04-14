package com.polymerization.merchant.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.polymerization.merchant.api.MerchantService;
import com.polymerization.merchant.api.dto.MerchantDTO;
import com.shanjupay.common.util.EncryptUtil;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.com.shanjupay.transaction.api.PayChannelService;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class Test {

    @Autowired
    MerchantService merchantService;

    @Autowired
    RestTemplate restTemplate;

    @org.junit.Test
    public void tets() {

        String url = "http://localhost:56085/sailing/generate?name=sms&effectiveTime=600";
        log.info("调用短信访问url:{}", url);
        //请求体
        Map<String, Object> body = new LinkedHashMap<>();
        String phone = "13642440515";
        body.put("mobile", phone);
        //请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        //设置数为json
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        //封装请求参数
        HttpEntity entity = new HttpEntity<>(body, httpHeaders);

        Map responseMap = null;

        try {
            //post请求
            ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            //获取响应
            responseMap = exchange.getBody();
        } catch (Exception e) {

            throw new RuntimeException("访问出错");
        }
        //取出exchangeBody的数据
        if (responseMap != null || responseMap.get("result") != null) {
            Map result = (Map) responseMap.get("result");
            String string = result.get("key").toString();
            System.out.println(string);
        }


    }

    @org.junit.Test
    public void test1() {
        Long merchantId = 1248523480777908226L;
        MerchantDTO merchantDTO = merchantService.queryMerchantById(merchantId);
        System.err.println(merchantDTO);
        JSONObject token = new JSONObject();
        token.put("mobile",merchantDTO.getMobile());
        token.put("merchantId",merchantDTO.getId());
        token.put("username",merchantDTO.getUsername());
        String jwt_token = "Bearer " + EncryptUtil.encodeBase64(JSON.toJSONString(token).getBytes());
        System.out.println(token);
        System.out.println(jwt_token);
    }

    @Autowired
    PayChannelService payChannelService;
    @org.junit.Test
    public void test3(){

        //测试根据服务类型查询支付渠道 queryPayChannelByPlatformChannel
        List<PayChannelDTO> shanju_c2b = payChannelService.queryPayChannelByPlatformChannel("shanju_c2b");
        System.out.println(JSON.toJSONString(shanju_c2b));

    }
}

