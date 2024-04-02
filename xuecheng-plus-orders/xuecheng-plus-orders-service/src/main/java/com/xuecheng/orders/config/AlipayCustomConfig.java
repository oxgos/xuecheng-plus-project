package com.xuecheng.orders.config;

import com.alipay.api.AlipayConfig;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description 支付宝配置参数
 */
@Data
@Configuration
public class AlipayCustomConfig {

    @Value("${pay.alipay.APP_ID}")
    public String APP_ID;

    @Value("${pay.alipay.APP_PUBLIC_KEY}")
    public String APP_PUBLIC_KEY;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    public String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    public String ALIPAY_PUBLIC_KEY;

    // 服务器异步通知页面路径 需http://或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public String notify_url = "http://商户网关地址/alipay.trade.wap.pay-JAVA-UTF-8/notify_url.jsp";
    // 页面跳转同步通知页面路径 需http://或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问 商户可以自定义同步跳转地址
    public String return_url = "http://商户网关地址/alipay.trade.wap.pay-JAVA-UTF-8/return_url.jsp";

    // 请求网关地址
    public String URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    // 编码
    public String CHARSET = "UTF-8";
    // 返回格式
    public String FORMAT = "json";
    // 日志记录目录
    public String log_path = "/log";
    // RSA2
    public String SIGNTYPE = "RSA2";

    @Bean
    public AlipayConfig alipayConfig() {
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl(URL);
        alipayConfig.setAppId(APP_ID);
        alipayConfig.setPrivateKey(APP_PRIVATE_KEY);
        alipayConfig.setFormat(FORMAT);
        alipayConfig.setCharset(CHARSET);
        alipayConfig.setAlipayPublicKey(ALIPAY_PUBLIC_KEY);
        alipayConfig.setSignType(SIGNTYPE);
        return alipayConfig;
    }
}
