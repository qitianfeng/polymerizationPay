package com.polymerization.transaction.mq;

import com.alibaba.fastjson.JSON;
import com.polymerization.paymentagent.api.dto.PaymentResponseDTO;
import com.polymerization.paymentagent.api.dto.TradeStatus;
import com.polymerization.transaction.api.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RocketMQTransactionListener
@RocketMQMessageListener(topic = "TP_PAYMENT_RESULT", consumerGroup = "CID_ORDER_CONSUMER")
public class TransactionPayConsumer implements RocketMQListener<MessageExt> {

    @Autowired
    TransactionService transactionService;

    @Override
    public void onMessage(MessageExt messageExt) {

        //取出消息内容
        String body = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        PaymentResponseDTO paymentResponseDTO = JSON.parseObject(body, PaymentResponseDTO.class);
        log.info("交易中心消费方接收支付结果信息：{}", paymentResponseDTO);

        TradeStatus tradeState = paymentResponseDTO.getTradeState();
        String tradeNo = paymentResponseDTO.getTradeNo();
        String outTradeNo = paymentResponseDTO.getOutTradeNo();
        switch (tradeState) {
            case SUCCESS:
                //支付成功，修改订单状态信息
                transactionService.updateOrderTradeNoAndTradeState(outTradeNo, tradeNo, "2");
                return;
            case FAILED:
                transactionService.updateOrderTradeNoAndTradeState(outTradeNo, tradeNo, "4");
                return;
            case REVOKED:
                transactionService.updateOrderTradeNoAndTradeState(outTradeNo, tradeNo, "5");
                return;
            default:
                throw new RuntimeException(String.format("无法解析支付结果:%s",body));

        }

    }
}
