package com.xuecheng.orders;


import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Map;

@SpringBootTest
public class AlipayTradeQueryTest {

    @Resource
    AlipayConfig alipayConfig;

    @Test
    public void AlipayTradeQuery() throws AlipayApiException {
        // 初始化SDK
        AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);

        // 构造请求参数以调用接口
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();

        // 设置订单支付时传入的商户订单号
        model.setOutTradeNo("1774818719167922176");

        // 设置查询选项
//        List<String> queryOptions = new ArrayList<String>();
//        queryOptions.add("trade_settle_info");
//        model.setQueryOptions(queryOptions);

        // 设置支付宝交易号
//        model.setTradeNo("2014112611001004680 073956707");

        request.setBizModel(model);
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        String body = response.getBody();
        Map<String, ?> map = JSON.parseObject(body, Map.class);
        Map<String, Object> alipay_trade_query_response = (Map<String, Object>) map.get("alipay_trade_query_response");
        String total_amount = (String) alipay_trade_query_response.get("total_amount");
        String trade_status = (String) alipay_trade_query_response.get("trade_status");
        String trade_no = (String) alipay_trade_query_response.get("trade_no");

        if (response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
            // sdk版本是"4.38.0.ALL"及以上,可以参考下面的示例获取诊断链接
            // String diagnosisUrl = DiagnosisUtils.getDiagnosisUrl(response);
            // System.out.println(diagnosisUrl);
        }
    }

}
