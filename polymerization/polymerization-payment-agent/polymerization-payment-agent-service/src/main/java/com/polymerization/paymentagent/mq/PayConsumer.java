package com.polymerization.paymentagent.mq;


import com.alibaba.fastjson.JSON;
import com.polymerization.paymentagent.api.PayChannelAgentService;
import com.polymerization.paymentagent.api.conf.AliConfigParam;
import com.polymerization.paymentagent.api.conf.WXConfigParam;
import com.polymerization.paymentagent.api.dto.PaymentResponseDTO;
import com.polymerization.paymentagent.api.dto.TradeStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RocketMQMessageListener(topic = "TP_PAYMENT_ORDER",consumerGroup = "CID_PAYMENT_CONSUMER")
public class PayConsumer implements RocketMQListener<MessageExt> {

    @Autowired
    RocketMQTemplate rocketMQTemplate;
    @Autowired
    PayProducer payProducer;

    @Autowired
    PayChannelAgentService payChannelAgentService;
    @Override
    public void onMessage(MessageExt messageExt) {
        log.info("开始消费支付结果查询消息{}" ,messageExt);

        //取出消息内容
        String body = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        PaymentResponseDTO paymentResponseDTO = JSON.parseObject(body, PaymentResponseDTO.class);
        String outTradeNo = paymentResponseDTO.getOutTradeNo();
        String content = String.valueOf(paymentResponseDTO.getContent());
        String msg = paymentResponseDTO.getMsg();

     ;

        PaymentResponseDTO result = new PaymentResponseDTO();

        //判断是用哪种客户端支付
        if ("ALIPAY_WAP".equals(msg)) {
            AliConfigParam aliConfigParam = JSON.parseObject(content, AliConfigParam.class);
             result = payChannelAgentService.queryPayOrderByAliPay(aliConfigParam, outTradeNo);

        } else  if("WX_JSAPI".equals(msg)) {
            WXConfigParam wxConfigParam = JSON.parseObject(content, WXConfigParam.class);
            result = payChannelAgentService.queryPayOrderByWeChat(wxConfigParam,outTradeNo);
        }
        //返回查询获得的支付状态
        if (TradeStatus.UNKNOWN.equals(result.getTradeState()) || TradeStatus.USERPAYING.equals(result.getTradeState())){
            throw  new RuntimeException("支付状态未知，等待重新操作");
        }

        payProducer.payOrderNotice(result);



    }
}
