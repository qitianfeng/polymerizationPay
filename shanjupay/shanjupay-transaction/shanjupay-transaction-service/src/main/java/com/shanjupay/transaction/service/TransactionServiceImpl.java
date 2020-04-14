package com.shanjupay.transaction.service;

import com.alibaba.fastjson.JSON;
import com.polymerization.merchant.api.AppService;
import com.polymerization.merchant.api.MerchantService;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.EncryptUtil;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.api.dto.QRCodeDto;
import com.shanjupay.transaction.api.dto.com.shanjupay.transaction.api.TransactionService;
import io.lettuce.core.dynamic.CommandCreationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Value;

import static com.alibaba.com.caucho.hessian.io.HessianInputFactory.log;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    @Value("@{shanjupay.url}")
    String payUrl;

    @Reference
    MerchantService merchantService;
    @Reference
    AppService appService;


    /**
     * 生成门店二维码
     *
     * @param qrCodeDto
     * @return
     * @throws BusinessException
     */
    @Override
    public String createStoreQRCode(QRCodeDto qrCodeDto) throws BusinessException {
//校验应用和门店
        verifyAppAndStore(qrCodeDto.getMerchantId(),qrCodeDto.getAppId(),qrCodeDto.getStoreId());

        //生成支付信息
         PayOrderDTO payOrderDTO = new PayOrderDTO();
         payOrderDTO.setMerchantId(qrCodeDto.getMerchantId());
         payOrderDTO.setAppId(qrCodeDto.getAppId());
         payOrderDTO.setStoreId(qrCodeDto.getStoreId());
         payOrderDTO.setSubject(qrCodeDto.getSubject());//显示订单标题
        payOrderDTO.setChannel("shanju_c2b");//服务类型
        payOrderDTO.setBody(qrCodeDto.getBody());//订单内容
        String jsonString = JSON.toJSONString(payOrderDTO);
        log.info("transaction service createStoreQRCode,JsonString is {}",jsonString);
        String ticket = EncryptUtil.encodeUTF8StringBase64(jsonString);
        String payEntryUrl = payUrl + ticket;
        return payEntryUrl;
    }

    private void verifyAppAndStore(Long merchantId, String appId, Long storeId) {

        //判断应用是否属于当前用户
        Boolean containers = appService.queryAppInMerchant(appId,merchantId);
        if (containers) {
            throw new BusinessException(CommonErrorCode.E_200005);
        }
        Boolean aBoolean = merchantService.queryStoreInMerchant(storeId, merchantId);
        if (aBoolean){
            throw new BusinessException(CommonErrorCode.E_200006);
        }
    }
}
