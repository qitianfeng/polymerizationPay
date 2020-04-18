package com.polymerization.transaction.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.polymerization.merchant.api.AppService;
import com.polymerization.merchant.api.MerchantService;
import com.polymerization.common.domain.BusinessException;
import com.polymerization.common.domain.CommonErrorCode;
import com.polymerization.common.util.AmountUtil;
import com.polymerization.common.util.EncryptUtil;
import com.polymerization.paymentagent.api.PayChannelAgentService;
import com.polymerization.paymentagent.api.conf.AliConfigParam;
import com.polymerization.paymentagent.api.conf.WXConfigParam;
import com.polymerization.paymentagent.api.dto.AlipayBean;
import com.polymerization.paymentagent.api.dto.PaymentResponseDTO;
import com.polymerization.paymentagent.api.dto.WeChatBean;
import com.polymerization.transaction.api.dto.PayChannelParamDTO;
import com.polymerization.transaction.api.dto.PayOrderDTO;
import com.polymerization.transaction.api.dto.QRCodeDto;
import com.polymerization.transaction.api.dto.com.shanjupay.transaction.api.PayChannelService;
import com.polymerization.transaction.api.dto.com.shanjupay.transaction.api.TransactionService;
import com.polymerization.transaction.entity.PayOrder;
import com.polymerization.transaction.mapper.PayOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    @Value("@{shanjupay.url}")
    String payUrl;

    @Value("${weixin.oauth2RequestUrl}")
    String oauth2RequestUrl;

    @Value("${weixin.oauth2CodeReturnUrl}")
    String oauth2CodeReturnUrl;

    @Value("${weixin.oauth2Token}")
    String oauth2Token;

    @Reference
    MerchantService merchantService;
    @Reference
    AppService appService;

    @Autowired
    PayOrderMapper payOrderMapper;

    @Reference
    PayChannelService payChannelService;

    @Reference
    PayChannelAgentService payChannelAgentService;

    @Autowired
    RestTemplate restTemplate;

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
        verifyAppAndStore(qrCodeDto.getMerchantId(), qrCodeDto.getAppId(), qrCodeDto.getStoreId());

        //生成支付信息
        PayOrderDTO payOrderDTO = new PayOrderDTO();
        payOrderDTO.setMerchantId(qrCodeDto.getMerchantId());
        payOrderDTO.setAppId(qrCodeDto.getAppId());
        payOrderDTO.setStoreId(qrCodeDto.getStoreId());
        payOrderDTO.setSubject(qrCodeDto.getSubject());//显示订单标题
        payOrderDTO.setChannel("shanju_c2b");//服务类型
        payOrderDTO.setBody(qrCodeDto.getBody());//订单内容
        String jsonString = JSON.toJSONString(payOrderDTO);
        log.info("transaction service createStoreQRCode,JsonString is {}", jsonString);
        String ticket = EncryptUtil.encodeUTF8StringBase64(jsonString);
        String payEntryUrl = payUrl + ticket;
        return payEntryUrl;
    }

    @Override
    public PaymentResponseDTO submitOrderByAli(PayOrderDTO payOrderDTO) throws BusinessException {

        //保存订单
        payOrderDTO.setPayChannel("ALIPAY_WAP");
        payOrderDTO = save(payOrderDTO);
        return alipayH5(payOrderDTO.getTradeNo());
    }

    @Override
    public void updateOrderTradeNoAndTradeState(String tradeNo, String payChannelTradeNo, String state) {
        if (state != null && "2".equals(state)) {

            payOrderMapper.update(null, new LambdaUpdateWrapper<PayOrder>()
                    .eq(PayOrder::getTradeNo, tradeNo).set(PayOrder::getPayChannelTradeNo, payChannelTradeNo).set(PayOrder::getPaySuccessTime, LocalDateTime.now()));
        }
    }

    /**
     * 获取微信授权码
     *
     * @param order
     * @return
     * @throws BusinessException
     */
    @Override
    public String getWXOAuth2Code(PayOrderDTO order) throws BusinessException {

        //j将订单信息封装到state中
        String state = EncryptUtil.encodeUTF8StringBase64(JSON.toJSONString(order));

        //应用id
        String appId = order.getAppId();

        //服务类型
        String channel = order.getChannel();

        PayChannelParamDTO wx_jsapi = payChannelService.queryParamByAppPlatformAndPayChannel(appId, channel, "WX_JSAPI");
        if (wx_jsapi == null) {
            throw new BusinessException(CommonErrorCode.E_300007);
        }

        //支付渠道
        String param = wx_jsapi.getParam();

        WXConfigParam wxConfigParam = JSON.parseObject(param, WXConfigParam.class);

        try {
            String url = String.format("%s?appid=%s&scope=snsapi_base&state=%s&redirect_uri=%s", oauth2RequestUrl, wxConfigParam.getAppId(), state, URLEncoder.encode(oauth2CodeReturnUrl, "utf-8"));
            return "redirect:" + url;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        return null;
    }

    /**
     * 获取微信openId
     *
     * @param code
     * @param apId
     * @return
     */
    @Override
    public String getWXOAuthOpenId(String code, String apId) throws BusinessException {

        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(apId, "shanju_c2b", "WX_JSAPI");

        if (payChannelParamDTO == null) {
            throw new BusinessException(CommonErrorCode.E_300007);
        }

        //支付参数
        String param = payChannelParamDTO.getParam();
        WXConfigParam wxConfigParam = JSON.parseObject(param, WXConfigParam.class);
        String appSecret = wxConfigParam.getAppSecret();

        //获取openId地址
        String url = String.format("%s?appid=%s&secret=%s&code=%s&grant_type=authorization_code", oauth2Token, wxConfigParam.getAppId(), wxConfigParam.getAppSecret(), code);
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

        String body = exchange.getBody();

        return JSONObject.parseObject(body).getString("openid");

    }

    /**
     * 微信确认支付按钮
     *
     * @param payOrderDTO
     * @return
     * @throws BusinessException
     */
    @Override
    public Map<String, String> submitOrderByWechat(PayOrderDTO payOrderDTO) throws BusinessException {

        //微信openId
        String openId = payOrderDTO.getOpenId();
        payOrderDTO.setChannel("WX_JSAPI");

        //保存订单
        PayOrderDTO save = save(payOrderDTO);
        String tradeNo = save.getTradeNo();
        return wxChatJsApi(openId, tradeNo);
    }



    /**
     * 微信jsapi调用支付接口
     *
     * @param openId
     * @param tradeNo
     * @return
     */
    private Map<String, String> wxChatJsApi(String openId, String tradeNo) {
        PayOrderDTO payOrderDTO = queryPayOrder(tradeNo);
        if (payOrderDTO == null) {
            throw new BusinessException(CommonErrorCode.E_400001);
        }

        //构造微信订单参数

        WeChatBean weChatBean = new WeChatBean();

        weChatBean.setOpenId(openId);
        weChatBean.setSpbillCreateIp(payOrderDTO.getClientIp());
        weChatBean.setTotalFee(payOrderDTO.getTotalAmount());
        weChatBean.setBody(payOrderDTO.getBody());
        weChatBean.setNotifyUrl("");
        weChatBean.setOutTradeNo(payOrderDTO.getTradeNo());


        //根据应用、服务类型、支付渠道查询支付渠道参数

        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(payOrderDTO.getAppId(), "shanju_c2b", "WX_JSAPI");
        if (payChannelParamDTO == null) {
            throw new BusinessException(CommonErrorCode.E_300007);
        }

        WXConfigParam wxConfigParam = JSON.parseObject(payChannelParamDTO.getParam(), WXConfigParam.class);
        return payChannelAgentService.createPayOrderByWeChatJSAPI(wxConfigParam, weChatBean);

    }

    /**
     * 保存支付信息
     *
     * @param payOrderDTO
     * @return
     * @throws BusinessException
     */
    private PayOrderDTO save(PayOrderDTO payOrderDTO) throws BusinessException {
        PayOrder entity = new PayOrder();
        BeanUtils.copyProperties(payOrderDTO, entity);

        //订单创建时间
        entity.setCreateTime(LocalDateTime.now());

        //设置过期时间
        entity.setExpireTime(LocalDateTime.now().plus(30, ChronoUnit.MINUTES));

        entity.setCurrency("CNY"); //设置支付币

        entity.setTradeState("0");

        int insert = payOrderMapper.insert(entity);

        BeanUtils.copyProperties(entity, payOrderDTO);
        return payOrderDTO;
    }

    private PaymentResponseDTO alipayH5(String tradeNo) {
        //构建支付实体
        AlipayBean alipayBean = new AlipayBean();

        //根据订单号查询订单详情
        PayOrderDTO payOrderDTO = queryPayOrder(tradeNo);

        alipayBean.setOutTradeNo(tradeNo);
        alipayBean.setSubject(payOrderDTO.getSubject());

        String totalAmount = null;

        try {
            AmountUtil.changeF2Y(payOrderDTO.getCurrency());

        } catch (Exception e) {
            e.printStackTrace();
        }

        alipayBean.setTotalAmount(totalAmount);
        alipayBean.setBody(payOrderDTO.getBody());
        alipayBean.setStoreId(payOrderDTO.getStoreId());
        alipayBean.setExpireTime("30m");


        //根据应用、服务类型、支付渠道查询支付渠道参数
        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(payOrderDTO.getAppId(), payOrderDTO.getChannel(), "ALIPAY_WAP");

        if (payChannelParamDTO == null) {
            throw new BusinessException(CommonErrorCode.E_300007);
        }


        //支付渠道参数
        AliConfigParam aliConfigParam = JSON.parseObject(payChannelParamDTO.getParam(), AliConfigParam.class);


        //字符编码
        aliConfigParam.setCharest("UTF-8");

        PaymentResponseDTO payOrderByAliPayWAP = payChannelAgentService.createPayOrderByAliPayWAP(aliConfigParam, alipayBean);

        return payOrderByAliPayWAP;

    }

    /**
     * 据订单号查询订单详情
     *
     * @param tradeNo
     * @return
     */
    private PayOrderDTO queryPayOrder(String tradeNo) {
        PayOrder payOrder = payOrderMapper.selectOne(new LambdaQueryWrapper<PayOrder>().eq(PayOrder::getOutTradeNo, tradeNo));
        PayOrderDTO payOrderDTO = new PayOrderDTO();
        BeanUtils.copyProperties(payOrder, payOrderDTO);
        return payOrderDTO;
    }

    private void verifyAppAndStore(Long merchantId, String appId, Long storeId) {

        //判断应用是否属于当前用户
        Boolean containers = appService.queryAppInMerchant(appId, merchantId);
        if (containers) {
            throw new BusinessException(CommonErrorCode.E_200005);
        }
        Boolean aBoolean = merchantService.queryStoreInMerchant(storeId, merchantId);
        if (aBoolean) {
            throw new BusinessException(CommonErrorCode.E_200006);
        }
    }
}
