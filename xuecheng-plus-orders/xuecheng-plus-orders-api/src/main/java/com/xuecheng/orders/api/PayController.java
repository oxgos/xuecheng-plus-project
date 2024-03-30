package com.xuecheng.orders.api;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.xuecheng.orders.config.AlipayCustomConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class PayController {

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;


    @RequestMapping("/alipaytest")
    public void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws AlipayApiException, IOException {
        AlipayConfig alipayConfig = getAlipayConfig();
        // 构造client
        AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        // 异步接收地址，仅支持http/https，公网可访问
        request.setNotifyUrl("");
        // 同步跳转地址，仅支持http/https
        request.setReturnUrl("");
        /******必传参数******/
        JSONObject bizContent = new JSONObject();
        // 商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", "20210817010101004");
        // 支付金额，最小值0.01元
        bizContent.put("total_amount", 1);
        // 订单标题，不可使用特殊符号
        bizContent.put("subject", "测试商品");
        // 电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        /******可选参数******/
        //bizContent.put("time_expire", "2022-08-01 22:00:00");

        //// 商品明细信息，按需传入
        //JSONArray goodsDetail = new JSONArray();
        //JSONObject goods1 = new JSONObject();
        //goods1.put("goods_id", "goodsNo1");
        //goods1.put("goods_name", "子商品1");
        //goods1.put("quantity", 1);
        //goods1.put("price", 0.01);
        //goodsDetail.add(goods1);
        //bizContent.put("goods_detail", goodsDetail);

        //// 扩展信息，按需传入
        //JSONObject extendParams = new JSONObject();
        //extendParams.put("sys_service_provider_id", "2088511833207846");
        //bizContent.put("extend_params", extendParams);

        request.setBizContent(bizContent.toString());
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "POST");
        // 如果需要返回GET请求，请使用
//        AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "GET");
        String pageRedirectionData = response.getBody();
        System.out.println(pageRedirectionData);
        httpResponse.setContentType("text/html;charset=" + AlipayCustomConfig.CHARSET);
        httpResponse.getWriter().write(pageRedirectionData); // 直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();

        if (response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }

    }

    @NotNull
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
