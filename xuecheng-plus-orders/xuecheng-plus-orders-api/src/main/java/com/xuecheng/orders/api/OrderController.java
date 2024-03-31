package com.xuecheng.orders.api;


import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.orders.config.AlipayCustomConfig;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import com.xuecheng.orders.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Api(value = "订单支付接口", tags = "订单支付接口")
@Slf4j
@Controller
public class OrderController {
    @Resource
    OrderService orderService;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    @ApiOperation("扫码下单接口")
    @GetMapping("/requestpay")
    public void requestpay(String payNo, HttpServletResponse httpResponse) throws IOException, AlipayApiException {
        // 传入了支付记录号，判断支付记录号是否存在
        XcPayRecord payRecordByPayno = orderService.getPayRecordByPayno(payNo);
        if (payRecordByPayno == null) {
            throw new XueChengPlusException("支付记录不存在");
        }
        String status = payRecordByPayno.getStatus();
        if("601002".equals(status)){
            XueChengPlusException.cast("订单已支付，请勿重复支付。");
        }

        AlipayConfig alipayConfig = getAlipayConfig();
        // 构造client
        AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        // 异步接收地址，仅支持http/https，公网可访问
//        request.setNotifyUrl("http://192.168.0.102:63030/orders/paynotify");
        // 同步跳转地址，仅支持http/https
//        request.setReturnUrl("");
        /******必传参数******/
        JSONObject bizContent = new JSONObject();
        // 商户订单号，商家自定义，保持唯一性 已支付: 20210817010101004
        bizContent.put("out_trade_no", payNo);
        // 支付金额，最小值0.01元
        bizContent.put("total_amount", payRecordByPayno.getTotalPrice());
        // 订单标题，不可使用特殊符号
        bizContent.put("subject", payRecordByPayno.getOrderName());
        // 电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        request.setBizContent(bizContent.toString());
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "POST");
        String pageRedirectionData = response.getBody();
        System.out.println(pageRedirectionData);
        httpResponse.setContentType("text/html;charset=" + AlipayCustomConfig.CHARSET);
        httpResponse.getWriter().write(pageRedirectionData); // 直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
    }

    @ApiOperation("生成支付二维码")
    @PostMapping("/generatepaycode")
    @ResponseBody
    public PayRecordDto generatePayCode(@RequestBody AddOrderDto addOrderDto) {
        //登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            XueChengPlusException.cast("请登录后继续选课");
        }
        return orderService.createOrder(user.getId(), addOrderDto);

    }

    private AlipayConfig getAlipayConfig() {
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl(AlipayCustomConfig.URL);
        alipayConfig.setAppId(APP_ID);
        alipayConfig.setPrivateKey(APP_PRIVATE_KEY);
        alipayConfig.setFormat(AlipayCustomConfig.FORMAT);
        alipayConfig.setCharset(AlipayCustomConfig.CHARSET);
        alipayConfig.setAlipayPublicKey(ALIPAY_PUBLIC_KEY);
        alipayConfig.setSignType(AlipayCustomConfig.SIGNTYPE);
        return alipayConfig;
    }
}
