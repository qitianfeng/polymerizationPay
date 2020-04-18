package com.polymerization.paymentagent.config;
import com.github.wxpay.sdk.WXPayConfig;
import com.polymerization.paymentagent.api.conf.WXConfigParam;
import org.springframework.util.Assert;

import java.io.InputStream;

/**
 * 微信支付参数
 */
public class WXSDKConfig implements WXPayConfig {

	private WXConfigParam param;

	public WXSDKConfig(WXConfigParam param) {
		Assert.notNull(param, "微信支付参数不能为空");
		this.param = param;
	}


	@Override
	public String getAppID() {
		return param.getAppId();
	}

	@Override
	public String getMchID() {
		return param.getMchId();
	}

	@Override
	public String getKey() {
		return param.getKey();
	}

	@Override
	public InputStream getCertStream() {
		return null;
	}

	public int getHttpConnectTimeoutMs() {
		return 8000;
	}

	public int getHttpReadTimeoutMs() {
		return 10000;
	}
}
