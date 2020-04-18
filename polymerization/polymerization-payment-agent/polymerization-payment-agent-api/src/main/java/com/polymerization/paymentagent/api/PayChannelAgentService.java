package com.polymerization.paymentagent.api;

import com.polymerization.common.domain.BusinessException;
import com.polymerization.paymentagent.api.conf.AliConfigParam;
import com.polymerization.paymentagent.api.conf.WXConfigParam;
import com.polymerization.paymentagent.api.dto.AlipayBean;
import com.polymerization.paymentagent.api.dto.PaymentResponseDTO;
import com.polymerization.paymentagent.api.dto.WeChatBean;

import java.util.Map;

public interface PayChannelAgentService {

    /**
     * 调用支付宝接口
     *
     * @param aliConfigParam
     * @param alipayBean
     * @return
     */
    PaymentResponseDTO createPayOrderByAliPayWAP(AliConfigParam aliConfigParam, AlipayBean alipayBean) throws BusinessException;

    /**
     * 支付宝交易状态查询
     * @param aliConfigParam
     * @param outTradeNo
     * @return
     */
    PaymentResponseDTO queryPayOrderByAliPay(AliConfigParam aliConfigParam,String outTradeNo) throws BusinessException;

    /**
     * 微信jsapi下单接口
     * @param wxConfigParam
     * @param weChatBean
     * @return
     */
    Map<String,String> createPayOrderByWeChatJSAPI(WXConfigParam wxConfigParam, WeChatBean weChatBean);

    PaymentResponseDTO queryPayOrderByWeChat(WXConfigParam wxConfigParam, String outTradeNo) throws BusinessException;
}
