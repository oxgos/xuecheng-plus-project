package com.xuecheng.medis;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description 测试minio的sdk
 */
public class MinioTest {

    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.164.132:9000")
                    .credentials("minio", "miniosecret")
                    .build();

    @Test
    public void upload() {
        try {
            // 通过扩展名得到媒体资源类型 mineType 使用com.j256.simplemagic
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".txt");
            String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // 通用mimeType，字节流
            // 有匹配的类型，就重新赋值，否则用通用字节流类型
            if (extensionMatch != null) {
                mimeType = extensionMatch.getMimeType();
            }
            System.out.println("mimeType : " + mimeType);
            // 上传文件的参数信息
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket("testbucket") // 桶
                    .filename("/Users/gavin_guo/Desktop/复审.txt") // 指定本地文件路径
                    // .object("复审.txt") // 在桶根目录下存储文件
                    .object("test/复审.txt")
                    .contentType(mimeType) // 搜索mediaType.class，在springboot框架里中的，再搜索MediaType extends
                    .build();// 对象名

            // 上传文件
            minioClient.uploadObject(testbucket);
            System.out.println("上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("上传失败");
        }

    }

    // 删除文件
    @Test
    public void delete() {
        try {
            RemoveObjectArgs testbucket = RemoveObjectArgs.builder()
                    .bucket("testbucket")
                    .object("test/复审.txt")
                    .build();
            minioClient.removeObject(testbucket);
            System.out.println("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除失败");
        }
    }

    //查询文件
    @Test
    public void getFile() {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket("testbucket")
                .object("test/复审.txt")
                .build();
        try (
                // 查询远程服务获取到一个流对象
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                FileOutputStream outputStream = new FileOutputStream(new File("/Users/gavin_guo/Desktop/复审_2.txt"));
                FileInputStream fileInputStream2 = new FileInputStream(new File("/Users/gavin_guo/Desktop/复审_2.txt"));
        ) {
            IOUtils.copy(inputStream, outputStream);

            StatObjectArgs testbucket = StatObjectArgs.builder()
                    .bucket("testbucket")
                    .object("test/复审.txt")
                    .build();
            StatObjectResponse statObjectResponse = minioClient.statObject(testbucket);
            // 获取远程服务文件的etag值
            String source_md5 = statObjectResponse.etag();

            // 校验文件的完整性对文件的内容进行md5
            String local_md5 = DigestUtils.md5Hex(fileInputStream2);
            if (source_md5.equals(local_md5)) {
                System.out.println("下载成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    // 将分块文件上传到minio
    public void uploadChunk() {
        String chunkFolderPath = "/Users/gavin_guo/Desktop/study-demo/backend/java/test-big-file/chunk/";
        File chunkFolder = new File(chunkFolderPath);
        //分块文件
        File[] files = chunkFolder.listFiles();
        if (files != null) {
            List<File> fileList = Arrays.asList(files);
            List<File> filterFileList = fileList.stream().filter(file -> isNumeric(file.getName())).collect(Collectors.toList());
            for (File file : filterFileList) {
                try {
                    // 上传文件的参数信息
                    UploadObjectArgs testbucket = UploadObjectArgs.builder()
                            .bucket("testbucket") // 桶
                            .filename(file.getAbsolutePath()) // 指定本地文件路径
                            .object("chunk/" + file.getName()) // 在桶根目录下存储文件
                            .build();// 对象名

                    // 上传文件
                    minioClient.uploadObject(testbucket);
                    System.out.println("上传分块" + file.getName() + "testbucket");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 调用minio接口合并分块
    @Test
    public void testMerge() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(14)
                .map(i -> ComposeSource.builder()
                        .bucket("testbucket")
                        .object("chunk/".concat(Integer.toString(i)))
                        .build()
                )
                .collect(Collectors.toList());

        // 指定合并后的objectName等信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("01. from.mp4")
                .sources(sources)
                .build();
        minioClient.composeObject(composeObjectArgs);
    }

    // 批量清理分块文件


    public boolean isNumeric(String str) {
        return str != null && str.matches("\\d+");
    }
}
