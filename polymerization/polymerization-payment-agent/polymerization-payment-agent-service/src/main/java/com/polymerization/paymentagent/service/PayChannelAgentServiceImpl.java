package com.polymerization.paymentagent.service;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.polymerization.common.domain.BusinessException;
import com.polymerization.common.domain.CommonErrorCode;
import com.polymerization.paymentagent.api.PayChannelAgentService;
import com.polymerization.paymentagent.api.conf.AliConfigParam;
import com.polymerization.paymentagent.api.conf.WXConfigParam;
import com.polymerization.paymentagent.api.dto.AlipayBean;
import com.polymerization.paymentagent.api.dto.PaymentResponseDTO;
import com.polymerization.paymentagent.api.dto.TradeStatus;
import com.polymerization.paymentagent.api.dto.WeChatBean;
import com.polymerization.paymentagent.common.constant.AliPayCodeConstant;
import com.polymerization.paymentagent.config.WXSDKConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class PayChannelAgentServiceImpl implements PayChannelAgentService {

    String appId = "";
    String mchId = "";
    String appSecret = "";
    String key = "";

    /**
     * 调用支付宝接口
     *
     * @param aliConfigParam 支付渠道参数
     * @param alipayBean     请求支付参数
     * @return
     */
    @Override
    public PaymentResponseDTO createPayOrderByAliPayWAP(AliConfigParam aliConfigParam, AlipayBean alipayBean) throws BusinessException {
        log.info("支付宝参数{}", alipayBean.toString());

        //配置支付宝渠道参数

        String geteway = aliConfigParam.getUrl();//获取网关
        String appId = aliConfigParam.getAppId();//获取应用id
        String alipayPublicKey = aliConfigParam.getAlipayPublicKey(); //获取公钥
        String rsaPrivateKey = aliConfigParam.getRsaPrivateKey();//获取私钥
        String format = aliConfigParam.getFormat();//数据格式json
        String charest = aliConfigParam.getCharest();//字符编码
        String signtype = aliConfigParam.getSigntype();//签名算法类型
        String returnUrl = aliConfigParam.getReturnUrl();//支付完成返回地址
        String notifyUrl = aliConfigParam.getNotifyUrl();//支付完成通知地址

        //支付宝SDK客户端
        AlipayClient alipayClient = new DefaultAlipayClient(geteway, appId, rsaPrivateKey, format, format, alipayPublicKey, signtype);

        //封装请求支付信息
        AlipayTradeWapPayRequest alipayTradeWapPayRequest = new AlipayTradeWapPayRequest();

        AlipayTradeWapPayModel alipayTradeWapPayModel = new AlipayTradeWapPayModel();
        alipayTradeWapPayModel.setOutTradeNo(alipayBean.getOutTradeNo());
        alipayTradeWapPayModel.setSubject(alipayBean.getSubject());
        alipayTradeWapPayModel.setTotalAmount(alipayBean.getTotalAmount());
        alipayTradeWapPayModel.setBody(alipayBean.getBody());
        alipayTradeWapPayModel.setProductCode(alipayBean.getProductCode());
        alipayTradeWapPayModel.setTimeExpire(alipayBean.getExpireTime());

        alipayTradeWapPayRequest.setBizModel(alipayTradeWapPayModel);

        String jsonString = JSON.toJSONString(alipayBean);

        log.info("createPayOrderByAliPay:{}", jsonString);

        //设置同步地址
        alipayTradeWapPayRequest.setNotifyUrl(notifyUrl);

        //设置异步地址
        alipayTradeWapPayRequest.setReturnUrl(returnUrl);

        try {
            //调用SDK条件表单
            AlipayTradeWapPayResponse response = alipayClient.pageExecute(alipayTradeWapPayRequest);
            log.info("支付宝手机网站支付预支付订单信息" + response);
            PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO();
            paymentResponseDTO.setContent(response.getBody());
            return paymentResponseDTO;
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_400002);//支付宝确认支付失败
        }
    }

    /**
     * 支付宝交易状态查询
     *
     * @param aliConfigParam
     * @param outTradeNo
     * @return
     */
    @Override
    public PaymentResponseDTO queryPayOrderByAliPay(AliConfigParam aliConfigParam, String outTradeNo) throws BusinessException {

        if (aliConfigParam == null || StringUtils.isBlank(outTradeNo)) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }

        //配置支付宝渠道参数

        String geteway = aliConfigParam.getUrl();//获取网关
        String appId = aliConfigParam.getAppId();//获取应用id
        String alipayPublicKey = aliConfigParam.getAlipayPublicKey(); //获取公钥
        String rsaPrivateKey = aliConfigParam.getRsaPrivateKey();//获取私钥
        String format = aliConfigParam.getFormat();//数据格式json
        String charest = aliConfigParam.getCharest();//字符编码
        String signtype = aliConfigParam.getSigntype();//签名算法类型
        String returnUrl = aliConfigParam.getReturnUrl();//支付完成返回地址
        String notifyUrl = aliConfigParam.getNotifyUrl();//支付完成通知地址
        //支付宝SDK客户端
        AlipayClient alipayClient = new DefaultAlipayClient(geteway, appId, rsaPrivateKey, format, format, alipayPublicKey, signtype);

        //封装请求支付信息
        AlipayTradeWapPayRequest alipayTradeWapPayRequest = new AlipayTradeWapPayRequest();

        AlipayTradeWapPayModel alipayTradeWapPayModel = new AlipayTradeWapPayModel();

        //平台订单号
        alipayTradeWapPayModel.setOutTradeNo(outTradeNo);

        alipayTradeWapPayRequest.setBizModel(alipayTradeWapPayModel);

        PaymentResponseDTO paymentResponseDTO = null;
        try {
            AlipayTradeWapPayResponse execute = alipayClient.execute(alipayTradeWapPayRequest);

            if (AliPayCodeConstant.SUCCESSCODE.equals(execute.getCode())) {
                TradeStatus tradeStatus = covertAliPayTradeStatusToStatus(execute.getTradeNo());
                paymentResponseDTO = PaymentResponseDTO.success(execute.getTradeNo(), execute.getOutTradeNo(),
                        tradeStatus, execute.getMsg() + "" + JSON.toJSONString(paymentResponseDTO));
                return paymentResponseDTO;
            }

        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return paymentResponseDTO;
    }

    /**
     * 微信jsapi下单接口
     *
     * @param wxConfigParam
     * @param weChatBean
     * @return
     */
    @Override
    public Map<String, String> createPayOrderByWeChatJSAPI(WXConfigParam wxConfigParam, WeChatBean weChatBean) {

        WXSDKConfig config = new WXSDKConfig(wxConfigParam);

        WXPay wxPay = new WXPay(config);

        Map<String, String> requestParam = new HashMap<>();
        requestParam.put("body", weChatBean.getBody());//订单描述
        requestParam.put("out_trade_no", weChatBean.getOutTradeNo());//订单号
        requestParam.put("fee_type", "CNY");//人民币
        requestParam.put("total_fee", String.valueOf(weChatBean.getTotalFee()));//金额
        requestParam.put("spbill_create_ip", weChatBean.getSpbillCreateIp());//客户端ip
        requestParam.put("notify_url", weChatBean.getNotifyUrl());//微信异步通知支付结果接口，暂时不用
        requestParam.put("trade_type", "JSAPI");
        requestParam.put("openid", weChatBean.getOpenId());


        try {
            //调用统一微信下单API
            Map<String, String> resp = wxPay.unifiedOrder(requestParam);
            //根据返回预付单信息生成JSAPI页面调用的支付参数并签名
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            Map<String, String> jsapiPayParam = new HashMap<>();
            jsapiPayParam.put("appId", resp.get("appid"));
            jsapiPayParam.put("package", "prepay_id=" + resp.get("prepay_id"));
            jsapiPayParam.put("timeStamp", timestamp);
            jsapiPayParam.put("nonceStr", UUID.randomUUID().toString());
            jsapiPayParam.put("signType", "HMAC‐SHA256");
            jsapiPayParam.put("paySign", WXPayUtil.generateSignature(jsapiPayParam, key, WXPayConstants.SignType.HMACSHA256));
            return jsapiPayParam;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_400001);
        }
    }

    private TradeStatus covertAliPayTradeStatusToStatus(String aliPayTradeStatus) {
        switch (aliPayTradeStatus) {
            case AliPayCodeConstant.WAIT_BUTER_PAY:
                return TradeStatus.USERPAYING;
            case AliPayCodeConstant.TRADE_SUCCESS:
                return TradeStatus.SUCCESS;
            case AliPayCodeConstant.TRADE_FINISHED:
                return TradeStatus.SUCCESS;
            default:
                return TradeStatus.FAILED;
        }
    }

    /**
     * 查询微信支付结果
     *
     * @param wxConfigParam
     * @param outTradeNo
     * @return
     * @throws BusinessException
     */
    @Override
    public PaymentResponseDTO queryPayOrderByWeChat(WXConfigParam wxConfigParam, String outTradeNo) throws BusinessException {

        WXSDKConfig config = new WXSDKConfig(wxConfigParam);


        Map<String, String> resp = null;

        try {
            WXPay wxPay = new WXPay(config);
            Map<String, String> data = new HashMap<String, String>();

            data.put("out_trade_no", outTradeNo);
            resp = wxPay.orderQuery(data);
        } catch (Exception e) {
            e.printStackTrace();
            return PaymentResponseDTO.fail("调用微信查询订单异常", outTradeNo, TradeStatus.FAILED);
        }

        String returnCode = resp.get("return_code");
        String resultCode = resp.get("result_code");
        String tradeState = resp.get("trade_state");
        String transactionId = resp.get("transaction_id");
        String tradeType = resp.get("trade_type");
        String returnMsg = resp.get("return_msg");

        if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)) {
            if ("SUCCESS".equals(tradeState)) {
                return PaymentResponseDTO.success(transactionId, outTradeNo, TradeStatus.SUCCESS, returnMsg);
            } else if ("USERPAYING".equals(tradeState)) {
                return PaymentResponseDTO.success(transactionId, outTradeNo, TradeStatus.USERPAYING, returnMsg);
            } else if ("PAYERROR".equals(tradeState)) {
                return PaymentResponseDTO.success(transactionId, outTradeNo, TradeStatus.FAILED, returnMsg);
            } else if ("CLOSED".equals(tradeState)) {
                return PaymentResponseDTO.success(transactionId, outTradeNo, TradeStatus.REVOKED, returnMsg);
            }
        }


        return null;
    }
}
