package com.shanjupay.transaction.api.dto.com.shanjupay.transaction.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.transaction.api.dto.QRCodeDto;

/**
 * 订单交易接口
 */
public interface TransactionService {
    /**
     * 生成门店二维码
     * @param qrCodeDto
     * @return
     * @throws BusinessException
     */
    String createStoreQRCode(QRCodeDto qrCodeDto) throws BusinessException;
}
