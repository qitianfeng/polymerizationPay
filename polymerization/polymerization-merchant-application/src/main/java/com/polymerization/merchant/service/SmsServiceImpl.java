package com.polymerization.merchant.service;

import com.polymerization.common.domain.BusinessException;
import com.polymerization.common.domain.CommonErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Value("${sms.url}")
    private String smsUrl;

    @Value("${sms.effectiveTime}")
    private String effectiveTime;

    @Autowired
    RestTemplate restTemplate;

    /**
     * 获取短信验证码
     *
     * @param phone
     * @return
     */
    @Override
    public String sendMsg(String phone) {
        String url = smsUrl + "/generate?name=sms&effectiveTime=" + effectiveTime;//验证码过期时间为600秒  10分钟
        log.info("调用短信微服务发送验证码：url:{}", url);
        //请求体
        Map<String, Object> body = new LinkedHashMap<>();
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

            throw new RuntimeException("发送验证码出错");
        }
        //取出exchangeBody的数据
        if (responseMap != null || responseMap.get("result") != null) {
            Map result = (Map) responseMap.get("result");
            String string = result.get("key").toString();
            System.out.println(string);
            return string;
        }
        return null;
    }

    /**
     * 校验验证码，抛出异常则校验无效
     *
     * @param verifyKey
     * @param verifyCode
     */
    @Override
    public void checkVerifyCode(String verifyKey, String verifyCode) throws BusinessException {
        //http://localhost:56085/sailing/verify?name=sms&verificationCode=286157&verificationKey=sms%3Af07c7b7a827a48f8a13af3de90e383f0
        String url = smsUrl + "verify?name=sms&verificationCode=" + verifyCode + "&verificationKey=" + verifyKey;
        log.info("调用短信微服务校验验证码：url:{}", url);
        Map responseMap = null;
        try {
            //请求校验验证码
            ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, HttpEntity.EMPTY, Map.class);
            responseMap = exchange.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_100102);
//            throw new RuntimeException("验证码错误");
        }
        if (responseMap == null || responseMap.get("result") == null || !(Boolean) responseMap.get("result")) {
//            throw new RuntimeException("验证码错误");
            throw new BusinessException(CommonErrorCode.E_100102);
        }

    }
}
