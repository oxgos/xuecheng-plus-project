package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 执行任务类
 */
@Slf4j
@Component
public class VideoJob {

    @Resource
    MediaFileProcessService mediaFileProcessService;

    @Resource
    MediaFileService mediaFileService;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegpath;

    /**
     * 视频处理任务
     */
    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex(); // 执行器的序号，从0开始
        int shardTotal = XxlJobHelper.getShardTotal(); // 执行器总数


        List<MediaProcess> mediaProcessList = null;

        int size = 0;
        try {
            //取出cpu核心数作为一次处理数据的条数
            int processors = Runtime.getRuntime().availableProcessors();
            // 查询待处理任务, 根据cpu核数获取任务(4核代表只能同时4个线程处理4个任务)
            mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
            // 任务数量
            size = mediaProcessList.size();
            log.debug("取出待处理视频任务{}条", size);
            if (size < 0) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // 创建一个线程池
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        // 使用计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcessList.forEach(mediaProcess -> {
            // 将任务加入线程池
            executorService.execute(() -> {
                // 下载minio视频到本地
                File originalFile = null;
                // 先创建一个临时文件，作为转换后的文件
                File mp4File = null;
                Long taskId = null;
                String fileId = null;
                try {
                    // 任务id
                    taskId = mediaProcess.getId();
                    // 开启任务
                    boolean b = mediaFileProcessService.startTask(taskId);
                    if (!b) {
                        log.debug("抢占任务失败,任务id:{}", taskId);
                        return;
                    }
                    // 文件id就是md5
                    fileId = mediaProcess.getFileId();
                    // 桶
                    String bucket = mediaProcess.getBucket();
                    // objectName
                    String filePath = mediaProcess.getFilePath();
                    // 下载minio视频到本地
                    originalFile = mediaFileService.downloadFileFromMinIO(bucket, filePath);
                    if (originalFile == null) {
                        log.debug("下载视频出错,任务id:{}，bucket:{},objectName:{}", taskId, bucket, filePath);
                        // 保存任务处理失败的结果
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载视频到本地失败");
                        return;
                    }

                    // 源avi视频的路径
                    String video_path = originalFile.getAbsolutePath();
                    // 转换后mp4文件的名称
                    String mp4_name = fileId + ".mp4";
                    try {
                        mp4File = File.createTempFile("minio", ".mp4");
//                        mp4File.deleteOnExit();
                    } catch (IOException e) {
                        log.debug("创建临时文件异常,{}", e.getMessage());
                        // 保存任务处理失败的结果
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件异常");
                        return;
                    }

                    String mp4_path = mp4File.getAbsolutePath();
                    // 创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, video_path, mp4_name, mp4_path);
                    // 开始视频转换，成功将返回success
                    String result = videoUtil.generateMp4();
                    if (!result.equals("success")) {
                        log.debug("视频转换失败,bucket:{},objectName:{},原因:{}", bucket, filePath, result);
                        // 保存任务处理失败的结果
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, result);
                        return;
                    }

                    String objectName = getFilePath(fileId, ".mp4");
                    // 上传到minio
                    boolean b1 = mediaFileService.addMediaFilesToMinIO2(mp4_path, "video/mp4", bucket, objectName);
                    if (!b1) {
                        log.debug("上传mp4到minio失败, taskId:{}", taskId);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, "", "上传mp4到minio失败");
                        return;
                    }
                    //访问url
                    String url = "/" + bucket + "/" + objectName;
                    // 保存任务处理结果
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, null);
                } catch (Exception e) {
                    log.debug("视频转码任务调度失败, taskId:{}, fileId:{}, 原因:{}, ", taskId, fileId, e.getMessage());
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, "", e.getMessage());
                } finally {
                    // 删除临时文件
                    if (originalFile != null) {
                        originalFile.delete();
                    }
                    if (mp4File != null) {
                        mp4File.delete();
                    }
                    // 计算器减去1
                    countDownLatch.countDown();
                }
            });
        });
        //等待,给一个充裕的超时时间,防止无限等待，到达超时时间还没有处理完成则结束任务,这里设置了30分钟
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    private String getFilePath(String fileMd5, String fileExt){
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }
}
