package com.xuecheng.content;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;

/**
 * @description 测试使用feign远程上传文件
 */
@SpringBootTest
public class FeignUploadTest {

    @Resource
    MediaServiceClient mediaServiceClient;

    //远程调用，上传文件
    @Test
    public void test() {
        // 将File转成MultipartFile
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("/Users/gavin_guo/Desktop/study-demo/backend/java/2.html"));
        String result = mediaServiceClient.uploadFile(multipartFile, "course/2.html");
        if (result == null) {
            System.out.println("进入了熔断降级");
        }
    }

}