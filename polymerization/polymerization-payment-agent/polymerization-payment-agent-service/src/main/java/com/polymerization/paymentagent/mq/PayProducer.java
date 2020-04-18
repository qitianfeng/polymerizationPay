package com.polymerization.paymentagent.mq;

import com.alibaba.fastjson.JSON;
import com.polymerization.paymentagent.api.dto.PaymentResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PayProducer {


    //消息topic
    private static final String TOPIC_ORDER = "TP_PAYMENT_ORDER";

    @Autowired
    RocketMQTemplate rocketMQTemplate;
    public void payOrderNotice(PaymentResponseDTO result){

        MessageBuilder<PaymentResponseDTO> message = MessageBuilder.withPayload(result);
        SendResult sendResult = rocketMQTemplate.syncSend(TOPIC_ORDER, message);

    }

    //订单结果 主题
    private static final String TOPIC_RESULT = "TP_PAYMENT_RESULT";

    /**
     * 发送支付信息
     * @param result
     */
    public void payResultNotice(PaymentResponseDTO result){
        rocketMQTemplate.convertAndSend(TOPIC_ORDER,result);
        log.info("支付渠道代理服务向mq支付结果消息：{}", JSON.toJSONString(result));

    }
}
