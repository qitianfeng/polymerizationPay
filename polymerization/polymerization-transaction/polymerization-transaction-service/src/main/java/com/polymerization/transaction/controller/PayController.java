package com.polymerization.transaction.controller;

import com.alibaba.fastjson.JSON;
import com.polymerization.merchant.api.AppService;
import com.polymerization.merchant.api.dto.AppDTO;
import com.polymerization.common.domain.BusinessException;
import com.polymerization.common.domain.CommonErrorCode;
import com.polymerization.common.util.AmountUtil;
import com.polymerization.common.util.EncryptUtil;
import com.polymerization.common.util.IPUtil;
import com.polymerization.common.util.ParseURLPairUtil;
import com.polymerization.paymentagent.api.dto.PaymentResponseDTO;
import com.polymerization.transaction.api.dto.PayOrderDTO;
import com.polymerization.transaction.api.TransactionService;
import com.polymerization.transaction.vo.OrderConfirmVO;
import com.polymerization.transaction.common.util.BrowserType;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Controller
@Slf4j
public class PayController {


    @Reference
    AppService appService;

    @Reference
    TransactionService transactionService;

    /**
     * 支付入口
     *
     * @param ticket
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/pay-entry/{ticket}")
    public String payEntry(@PathVariable String ticket, HttpServletRequest request, HttpServletResponse response) {

        //将ticket还原base64编码
        String ticketStr = EncryptUtil.decodeUTF8StringBase64(ticket);
        PayOrderDTO payOrderDTO = JSON.parseObject(ticketStr, PayOrderDTO.class);
        //将对象转化为url
        String params = ParseURLPairUtil.parseURLPair(payOrderDTO);

        //得到客户端类型
        BrowserType browserType = BrowserType.valueOfUserAgent(request.getHeader("user-agent"));

        switch (browserType) {
            case ALIPAY:
                return "forward:/pay-page?" + ParseURLPairUtil.parseURLPair(payOrderDTO);
            case WECHAT:
                return "forward:/pay-page?" + transactionService.getWXOAuth2Code(payOrderDTO);
            default:

        }

        return "forward:/pay-page" + params;
    }

    @ApiOperation("支付宝门店下单付款")
    @PostMapping("/createAliPayOrder")
    public void createAliPayOrderForStore(OrderConfirmVO orderConfirmVO, HttpServletRequest request, HttpServletResponse response) throws BusinessException {

        if (StringUtils.isBlank(orderConfirmVO.getAppId())) {
            throw new BusinessException(CommonErrorCode.E_300003);
        }
        PayOrderDTO payOrderDTO = new PayOrderDTO();
        BeanUtils.copyProperties(orderConfirmVO, payOrderDTO);

        payOrderDTO.setTotalAmount(Integer.valueOf(AmountUtil.changeY2F(orderConfirmVO.getTotalAmount())));


        payOrderDTO.setClientIp(IPUtil.getIpAddr(request));

        AppDTO appDTO = appService.getAppById(payOrderDTO.getAppId());

        //设置所属商户
        payOrderDTO.setMerchantId(appDTO.getMerchantId());

        PaymentResponseDTO paymentResponseDTO = transactionService.submitOrderByAli(payOrderDTO);

        String content = (String) paymentResponseDTO.getContent();
        log.info(" 支付宝H5支付响应的结果{}", content);

        response.setContentType("text/html;charset=utf-8");
        try {
            response.getWriter().write(content);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    /**
     * 获取微信openId
     *
     * @param code  授权id
     * @param state 应用id
     * @return
     */

    @ApiOperation("微信授权码回调")
    @GetMapping("/wx-oauth-code-return")
    public String wxOAuth2CodeReturn(@RequestParam String code, @RequestParam String state) {

        //将之前state中保存的订单信息读取出来
        PayOrderDTO payOrderDTO = JSON.parseObject(EncryptUtil.decodeUTF8StringBase64(state), PayOrderDTO.class);

        //应用id
        String appId = payOrderDTO.getAppId();

        //获取openId
        String openId = transactionService.getWXOAuthOpenId(code, appId);

        try {
            String orderInfo = ParseURLPairUtil.parseURLPair(payOrderDTO);

            //返回所有参数到支付页面
            return String.format("forward:/pay-page?openId=%s&%s", openId, orderInfo);
        } catch (BusinessException e) {
            e.printStackTrace();
            return "forward:/pay-page-error";
        }

    }

    @ApiOperation("微信门店下单付款")
    @PostMapping("wxjsapi")
    public ModelAndView createWXOrderForStore(OrderConfirmVO orderConfirmVO, HttpServletRequest request) throws BusinessException {
        PayOrderDTO payOrderDTO = new PayOrderDTO();

        BeanUtils.copyProperties(orderConfirmVO, payOrderDTO);

        if (StringUtils.isBlank(orderConfirmVO.getOpenId())) {
            throw new BusinessException(CommonErrorCode.E_300002);
        }

        String appId = payOrderDTO.getAppId();
        String openId = payOrderDTO.getOpenId();
        AppDTO appDTO = appService.getAppById(appId);
        payOrderDTO.setMerchantId(appDTO.getMerchantId());

        payOrderDTO.setClientIp(IPUtil.getIpAddr(request));
        //将前端的元转化为分
        payOrderDTO.setTotalAmount(Integer.parseInt(AmountUtil.changeY2F(orderConfirmVO.getTotalAmount().toString())));

        Map<String,String> paymentResponseDTO = transactionService.submitOrderByWechat(payOrderDTO);

        return new ModelAndView("wxpay",paymentResponseDTO);

    }

}
