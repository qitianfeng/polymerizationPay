package com.polymerization.transaction.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@Controller
public class PayTestController {

    String APP_ID = "2016102200736883";
    String APP_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzMfrcHRdgGLDX3senqSco+x8I3/SDRpy5PFvl0563Jc47K/9f2/ns4DnBxIPGsP1trXsZzUifZa2/eycGcqfm/PEKwbMwaBS5dVh/lJN1onp2K9HXV2Z+n1nsmbmSeXRY2UQpca+cNJSAXuRn/o9+DJgzDS3Bh/kztQo9BuxEucAi0UmnZXq6HkE5FzHPs59J3JncaSfB+wJFPYufSUm2Sl9R4qpzGhncFbLjTJNvUHu+n7TkLpBEwatacClhbe8FTsTIQwb6R88FfYxT2TVysgGk0I7hATBcCUK0idx1mDUju3wbvAIkTtcu3JEsXyN7ZFwRq2mdYV+KgjI0ahpsQIDAQAB\n";
    String APP_PRIVATE_KEY = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDMx+twdF2AYsNfex6epJyj7Hwjf9INGnLk8W+XTnrclzjsr/1/b+ezgOcHEg8aw/W2texnNSJ9lrb97JwZyp+b88QrBszBoFLl1WH+Uk3WienYr0ddXZn6fWeyZuZJ5dFjZRClxr5w0lIBe5Gf+j34MmDMNLcGH+TO1Cj0G7ES5wCLRSadleroeQTkXMc+zn0ncmdxpJ8H7AkU9i59JSbZKX1HiqnMaGdwVsuNMk29Qe76ftOQukETBq1pwKWFt7wVOxMhDBvpHzwV9jFPZNXKyAaTQjuEBMFwJQrSJ3HWYNSO7fBu8AiRO1y7ckSxfI3tkXBGraZ1hX4qCMjRqGmxAgMBAAECggEBAJ8qyPIIUGCo40iEjuEp+2JiPn7tK9vpceJHUsAdTNvGYoV9qSBzX3mjOG7Dd6ALLiIMtynlH+DT03n8fbyCRJrX1uLGj0H0llp0B43AdSXK3ksAugx042jSLlZ5KofHrH+AH3dqyb0xjHIAlMjunVob9ibi1ltZUOP9/bL3DyQrqXDkdwCwRZpgJTqnz4K/Ee0nusdyVH0nmo77dStrpYHnPn4B3h7bIw6y0OZ8KLcYxzY7seMi3mZDJtcY3Xo6VTa/I59T6Am1mnN1QICw7RkJ/wv0aCEKpF+FlLtjr8SMvemxL4i64hNyB63SpWm5tMgCSIzEKhDKHghiUYWUI00CgYEA+fHkXPdnjo+eERlIdajKvdok6zshGFrInAM+q4kwqiHABBDNZlmx83BEOLqLLpcOCfKdGLRCv4C8heQ02OiqZ1ndY+2SSjuDZehGUKIBCLbipVYXfofX8PElYPQ3heBS3h1BferSPFdBEvwgGdoCdDCPK+v2lNOpZMjEnRnKStcCgYEA0b3uDF5VXj0OCOG8ErFOTIfgxC3Nx6kAeCNASwS3ZWeliaa/nWSawJoCBWpe9TL4Z6Q7+CZDpOLEZFm6rOQs8zBzd/UGXRnOnVvHSYRbyUngLtPDKjHMr/WbycPC/KPWf2PVI30X8FlWRsC4sudILGo5/kgqv9nA93rI3Nd3JrcCgYB9cn/YfUAXmFOQZ20ryK0BPsS637GLpLz9OM+yjqfDLC9QmxJMZYZgFZ/YDSCgIKamLYPVi1vY/Aci+Ffh6lzkhIEOj4WWBmq7singfH8iXZBBxYUDN7EVOCM3lztq0R0mZ+6gAUTFjlGV8r812mrS4DBrrgHTzqw9blroMrKKfwKBgQCahd5KWHl2a27alkw0TVKNSZILll9D2LvSxs3INxpSaDDqH+KNt5/Xg983VC3PYhTmbnYMFUzCvprH/99rHtQK+sgnOWrNzoSNJB/Hhu8EdhvnA/aGMJhHjqCO3l3aOW5+/fL6KWAon/jTOYsZqxFeP4ioUUzFeU0URu6S3V8YMwKBgQDFFsEMF34HsG90JBv/BotYqmB3RLecSAiLMTKXOpP0B7tOkSHLHr2bGaeKN+WF7xCOhB/nOeo/58OFSl4MDwpACIru/7TBKxj50TFF3UNWeQbqF/pRr4ZN11GzHHYvB2HjAVPUNP7WJDWnjl8H8XUXNEhXT3BNVg7usmbZgCDZEw==";
    String CHARSET = "utf-8";
    String serverUrl = "https://openapi.alipaydev.com/gateway.do";//正式测试环境https://openapi.alipay.com/gateway.do
    @GetMapping("/alipayTest")
    public void doPost(HttpServletRequest httpRequest,
                       HttpServletResponse httpResponse) throws ServletException, IOException {
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipaydev.com/gateway.do", APP_ID, APP_PRIVATE_KEY, "json", CHARSET, APP_PUBLIC_KEY, "RSA2"); //获得初始化的AlipayClient
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
        alipayRequest.setNotifyUrl("http://domain.com/CallBack/notify_url.jsp");//在公共参数中设置回跳和通知地址
        alipayRequest.setBizContent("{" +
                " \"out_trade_no\":\"20150320010101222\"," +
                " \"total_amount\":\"88.88\"," +
                " \"subject\":\"Iphone6 16G\"," +
                " \"product_code\":\"QUICK_WAP_PAY\"" +
                " }");//填充业务参数
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }

}
