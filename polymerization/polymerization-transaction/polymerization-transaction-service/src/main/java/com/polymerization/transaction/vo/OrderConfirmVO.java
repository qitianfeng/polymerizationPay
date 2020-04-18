package com.polymerization.transaction.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel(value="OrderConfirmVO",description="订单确认信息")
@Data
public class OrderConfirmVO {

    private String appId;//应用id
    private String tradeNO;//交易单号
    private String openId;//微信openId
    private String channel;//门店id
    private String body;//服务类型
    private String subject;//订单标题
    private String totalAmount;//金额
}
