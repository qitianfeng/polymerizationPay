package com.polymerization.transaction.api.dto.com.shanjupay.transaction.api;

import com.polymerization.common.domain.BusinessException;
import com.polymerization.paymentagent.api.dto.PaymentResponseDTO;
import com.polymerization.transaction.api.dto.PayOrderDTO;
import com.polymerization.transaction.api.dto.QRCodeDto;

import java.util.Map;

/**
 * 订单交易接口
 */
public interface TransactionService {
    /**
     * 生成门店二维码
     *
     * @param qrCodeDto
     * @return
     * @throws BusinessException
     */
    String createStoreQRCode(QRCodeDto qrCodeDto) throws BusinessException;

    PaymentResponseDTO submitOrderByAli(PayOrderDTO payOrderDTO) throws BusinessException;

    void updateOrderTradeNoAndTradeState(String tradeNo, String payChannelTradeNo, String state);

    /**
     * 获取微信授权码
     *
     * @param order
     * @return
     * @throws BusinessException
     */
    String getWXOAuth2Code(PayOrderDTO order) throws BusinessException;

    /**
     * 获取微信openId
     *
     * @param code
     * @param apId
     * @return
     */
    String getWXOAuthOpenId(String code, String apId) throws BusinessException;

    /**
     * 微信确认支付按钮
     * @param payOrderDTO
     * @return
     * @throws BusinessException
     */
    Map<String, String> submitOrderByWechat(PayOrderDTO payOrderDTO) throws BusinessException;

}
