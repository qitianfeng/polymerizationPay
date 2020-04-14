package com.shanjupay.transaction.config;

import com.github.wxpay.sdk.WXPayConfig;

import java.io.InputStream;

public class WXPayConfigCustom implements WXPayConfig {
    @Override
    public String getAppID() {
        return getAppID();
    }

    @Override
    public String getMchID() {
        return null;
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public InputStream getCertStream() {
        return null;
    }

    @Override
    public int getHttpConnectTimeoutMs() {
        return 0;
    }

    @Override
    public int getHttpReadTimeoutMs() {
        return 0;
    }
}
