package com.polymeization.gateway.filter;


import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.polymerization.common.util.EncryptUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.HashMap;
import java.util.Map;

public class AuthFilter extends ZuulFilter {

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return 0;
	}

	@Override
	public Object run() throws ZuulException {
		RequestContext ctx = RequestContext.getCurrentContext();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (!(authentication instanceof OAuth2Authentication)) { // 无token访问网关内资源的情况，目前仅有uaa服务直接暴露
			return null;
		}

		OAuth2Authentication oauth2Authentication = (OAuth2Authentication) authentication;
		Authentication userAuthentication = oauth2Authentication.getUserAuthentication();

		Map<String, String> jsonToken = new HashMap<>(
				oauth2Authentication.getOAuth2Request().getRequestParameters());
		if (userAuthentication != null) {
			jsonToken.put("user_name", userAuthentication.getName());
		}
		ctx.addZuulRequestHeader("jsonToken", EncryptUtil.encodeUTF8StringBase64(JSON.toJSONString(jsonToken)));
		return null;
	}

}
