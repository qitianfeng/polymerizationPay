package com.polymerization.merchant.service;

public interface SmsService {
    /**
     * 获取短信验证码
     * @param phone
     * @return
     */
    String sendMsg(String phone);

    /**
     * 校验验证码，抛出异常则校验无效
     * @param verifyKey
     * @param verifyCode
     */
    void checkVerifyCode(String verifyKey, String verifyCode);
}
