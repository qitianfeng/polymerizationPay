package com.shanjupay.transaction.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.shanjupay.transaction.config.WXPayConfigCustom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@Slf4j
public class WxPayController {
    String appId = "";
    String mchId = "";
    String appSecret = "";
    String key = "";

    //申请授权地址
    String wxOAth2CodeReQuestUrl = "http://open.weixin.qq.com/connect/oauth2/authorize";
    //授权回调地址
    String wxOAuth2CodeReturnUrl = "\"http://xfc.nat300.top/transaction/wx‐oauth‐code‐return\n";

    String state = "";

    @GetMapping("/getWXOAth2Code")
    public String getWXOAuth2Code(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws UnsupportedEncodingException {
        java.lang.String url = String.format("%s?appid=%s&scope=snsapi_base&state=%s&redirect_uri=%s", wxOAth2CodeReQuestUrl, appId, state, URLEncoder.encode(wxOAuth2CodeReturnUrl, "utf-8"));
        return "redirect:" + url;
    }

    //授权通过跳转到/wx‐oauth‐code‐return‐test?code=CODE&state=STATE
    @GetMapping("/wx-oauth-cod-‐return-test?code=CODE&state=STATE")
    public String wxOAuth2CodeReturn(@RequestParam String code, @RequestParam String state) {
        String tokenUrl = String.format("https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code", appId, appSecret, "utf-8");
        ResponseEntity<String> exchange = new RestTemplate().exchange(tokenUrl, HttpMethod.GET, null, String.class);
        String body = exchange.getBody();
        String openid = JSONObject.parseObject(body).getString("openid");
        return "redirect:http://xfc.nat300.top/transaction/wxjspay?openid=" + openid;
    }

    //微信统一下单
    @RequestMapping("/wxjsapi")
    public ModelAndView wxjsapi(HttpServletRequest request, HttpServletResponse response) {
        WXPay wxPay = new WXPay(new WXPayConfigCustom());
        //按照微信统一下单接口要求构造请求参数
        //https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_1
        Map<String, String> requestParam = new HashMap<String, String>();
        requestParam.put("body", "iphone8");//订单描述
        requestParam.put("out_trade_no", "1234567");//订单号
        requestParam.put("fee_type", "CNY");//人民币
        requestParam.put("total_fee", String.valueOf(1));//金额
        requestParam.put("spbill_create_ip", "127.0.0.1");//客户端ip
        requestParam.put("notify_url", "none");//微信异步通知支付结果接口，暂时不用
        requestParam.put("trade_type", "JSAPI");
        requestParam.put("openid", request.getParameter("openid"));


        //调用统一微信下单API
        try {
            Map<String, String> resp = wxPay.unifiedOrder(requestParam);
            //根据返回预付单信息生成JSAPI页面调用的支付参数并签名
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            Map<String, String> jsapiPayParam = new HashMap<>();
            jsapiPayParam.put("appId", resp.get("appid"));
            jsapiPayParam.put("package", "prepay_id=" + resp.get("prepay_id"));
            jsapiPayParam.put("timeStamp", timestamp);
            jsapiPayParam.put("nonceStr", UUID.randomUUID().toString());
            jsapiPayParam.put("signType", "HMAC‐SHA256");
            jsapiPayParam.put("paySign",
                    WXPayUtil.generateSignature(jsapiPayParam, key,
                            WXPayConstants.SignType.HMACSHA256));
            return new ModelAndView("wxpay",jsapiPayParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
return null;
    }
}
