package com.xuecheng.medis;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 测试大文件上传
 */
public class BigFileTest {

    // 测试分块
    @Test
    public void testChunk() throws IOException {
        // 源文件
        File sourceFile = new File("/Users/gavin_guo/Desktop/study-demo/backend/java/test-big-file/01. from the Heart.mp4");
        // 分块文件存储路径
        String chunkFilePath = "/Users/gavin_guo/Desktop/study-demo/backend/java/test-big-file/chunk/";
        // 分块大小5MB
        int chunkSize = 1024 * 1024 * 5;
        // 分块文件个数
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        // 使用流从源文件读数据，向分块文件中写数据
        // RandomAccessFile随机流可写可读
        // 源数据读取流
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile, "r");
        // 缓存区
        byte[] bytes = new byte[1024];
        for (int i = 0; i < chunkNum; i++) {
            File chunkFile = new File(chunkFilePath + i);
            // 分块写入流
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            // raf_r.read(bytes)作用：把bytes大小的数据读取到bytes里，如果没有数据，返回则为-1，否则返回读取数据大小
            while ((len = raf_r.read(bytes)) != -1) {
                // raf_rw.write(bytes, 0, len)作用：把在bytes里从0至len位置数据，写入chunkFile
                raf_rw.write(bytes, 0, len);
                if (chunkFile.length() >= chunkSize) {
                    break;
                }
            }
            raf_rw.close();
        }
        raf_r.close();
    }

    // 测试合并
    @Test
    public void testMerge() throws IOException {
        // 分块文件目录
        File chunkFolder = new File("/Users/gavin_guo/Desktop/study-demo/backend/java/test-big-file/chunk/");
        // 源文件
        File sourceFile = new File("/Users/gavin_guo/Desktop/study-demo/backend/java/test-big-file/01. from the Heart.mp4");
        // 合并后文件
        File mergeFile = new File("/Users/gavin_guo/Desktop/study-demo/backend/java/test-big-file/01. from the Heart_2.mp4");

        // 取出所有分块文件
        File[] files = chunkFolder.listFiles();
        if (files != null) {
            // 将数组转为list
            List<File> filesList = Arrays.asList(files);
            // 过滤非数字命名的文件
            List<File> filterFilesList = filesList.stream()
                    .filter(file -> isNumeric(file.getName()))
                    .collect(Collectors.toList());

            // 用Collections工具类排序
            Collections.sort(filterFilesList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
                }
            });

            // 向合并文件写的流
            RandomAccessFile raf_rw = new RandomAccessFile(mergeFile, "rw");
            // 缓冲区
            byte[] bytes = new byte[1024];
            // 遍历分块文件，向合并的文件写
            for (File file : filterFilesList) {
                // 读分块的流
                RandomAccessFile raf_r = new RandomAccessFile(file, "r");
                int len = -1;
                while ((len = raf_r.read(bytes)) != -1) {
                    // 0和len均表示: 从bytes的0位置到len位置写入到mergeFile里
                    raf_rw.write(bytes, 0, len);
                }
                raf_r.close();
            }
            raf_rw.close();
            // 合并文件完成后对合并的文件进行校验
            try (
                    FileInputStream fileInputStream_merge = new FileInputStream(mergeFile);
                    FileInputStream fileInputStream_source = new FileInputStream(sourceFile);
            ) {
                String merge_md5 = DigestUtils.md5Hex(fileInputStream_merge);
                String source_md5 = DigestUtils.md5Hex(fileInputStream_source);
                Assertions.assertEquals(merge_md5, source_md5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("文件夹为空");
        }
    }

    public boolean isNumeric(String str) {
        return str != null && str.matches("\\d+");
    }

    @Test
    public void testRandomAccessFile() {
        // 源文件
        File sourceFile = new File("/Users/gavin_guo/Desktop/study-demo/backend/java/test-big-file/01. from the Heart.mp4");
        // 写入文件
        File file = new File("/Users/gavin_guo/Desktop/study-demo/backend/java/test-big-file/test.mp4");
        try (
                RandomAccessFile raf_r = new RandomAccessFile(sourceFile, "r");
                RandomAccessFile raf_rw = new RandomAccessFile(file, "rw");
        ) {
            // 每次读取1024字节的buffer
            byte[] bytes = new byte[1024];
            int len;
            // 利用raf_r.read(bytes)读取1024字节数据，如果没有数据，返回则为-1，否则返回读取数据大小
            while ((len = raf_r.read(bytes)) != -1) {
                // // 0和len均表示: 从bytes的0位置到len位置写入到mergeFile里
                raf_rw.write(bytes, 0, len);
                if (file.length() >= sourceFile.length()) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
