package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description 媒资服务
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Resource
    MediaFilesMapper mediaFilesMapper;

    @Resource
    MediaProcessMapper mediaProcessMapper;

    @Resource
    MinioClient minioClient;

    @Resource
    MediaFileService currentProxy;

    // 存储普通文件
    @Value("${minio.bucket.files}")
    private String bucket_mediafiles;

    // 存储视频文件
    @Value("${minio.bucket.videofiles}")
    private String bucket_videofiles;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        // 构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(MediaFiles::getFilename, queryMediaParamsDto.getFilename())
                .eq(MediaFiles::getFileType, queryMediaParamsDto.getFileType())
                .eq(MediaFiles::getAuditStatus, queryMediaParamsDto.getAuditStatus());
        // 分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    // 因为这方法有通过网络上传文件到minio，可能会很慢，导致数据库阻塞，占用数据库资源，所以不在这里添加@Transactional，而在addMediaFilesToDb添加
    // @Transactional
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, MultipartFile file) {

        // 将文件上传到minio
        // 文件名
        String filename = uploadFileParamsDto.getFilename();
        // 先获取扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        // 获取mineType
        String mimeType = getMimeType(extension);
        // 获取文件路径
        String defaultFolderPath = getDefaultFolderPath();
        // 文件的md5值
        String fileMd5 = getFileMd5(file);
        String objectName = defaultFolderPath + fileMd5 + extension;

        boolean result = addMediaFilesToMinIO(file, mimeType, bucket_mediafiles, objectName);
        if (!result) {
            XueChengPlusException.cast("上传文件失败");
        }
        // 入库文件信息
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_mediafiles, objectName);
        if (mediaFiles == null) {
            XueChengPlusException.cast("文件上传后保存信息失败");
        }
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        // 将文件信息保存到数据库

        return uploadFileResultDto;
    }

    /**
     * @param companyId           机构id
     * @param fileMd5             文件md5值
     * @param uploadFileParamsDto 上传文件的信息
     * @param bucket              桶
     * @param objectName          对象名称
     * @return com.xuecheng.media.model.po.MediaFiles
     * @description 将文件信息添加到文件表
     */
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        // 将文件信息保存到数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            // 文件id
            mediaFiles.setId(fileMd5);
            // 机构id
            mediaFiles.setCompanyId(companyId);
            // 桶
            mediaFiles.setBucket(bucket);
            // file_path
            mediaFiles.setFilePath(objectName);
            // file_id
            mediaFiles.setFileId(fileMd5);
            // url
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            // 上传时间
            mediaFiles.setCreateDate(LocalDateTime.now());
            // 状态
            mediaFiles.setStatus("1");
            // 审核状态
            mediaFiles.setAuditStatus("002003");
            // 插入数据库
            int insert = mediaFilesMapper.insert(mediaFiles);

            if (insert < 0) {
                log.debug("向数据库保存文件失败, bucket: {}, objectName: {}", bucket, objectName);
                return null;
            }

            // 记录待处理任务
            addWaitingTask(mediaFiles);
        }
        return mediaFiles;
    }

    /**
     * 记录待处理任务
     * @param mediaFiles
     */
    private void addWaitingTask(MediaFiles mediaFiles) {
        // 通过mimetype判断如果是avi视频才写入待处理任务
        String filename = mediaFiles.getFilename();
        // 文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        // 如果是avi视频
        if (mimeType.equals("video/x-msvideo")) {
            MediaProcess mediaProcess = new MediaProcess();
            mediaProcess.setFileId(mediaFiles.getFileId());
            mediaProcess.setFilename(mediaFiles.getFilename());
            mediaProcess.setBucket(mediaFiles.getBucket());
            mediaProcess.setFilePath(mediaFiles.getFilePath());
            // 状态未处理
            mediaProcess.setStatus("1");
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);
            mediaProcessMapper.insert(mediaProcess);
        }
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        // 先查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        // 如果数据存在再查询Minio
        if (mediaFiles != null) {
            String bucket = mediaFiles.getBucket();
            String filePath = mediaFiles.getFilePath();
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build();
            // 查询远程服务获取到一个流对象
            try {
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if (inputStream != null) {
                    // 文件存在
                    return RestResponse.validfail("文件已存在");
                }
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 文件不存在
        return RestResponse.success(true);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        // 分块存储路径是：md5前两位为两个目录，chunk存储分块文件
        // 根据md5得到分块文件的路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5) + chunkIndex;

        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket_videofiles)
                .object(chunkFileFolderPath)
                .build();
        // 查询远程服务获取到一个流对象
        try {
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            if (inputStream != null) {
                // 文件存在
                return RestResponse.success(true);
            }
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 文件不存在
        return RestResponse.success(false);
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, MultipartFile file) {
        // 分块文件的路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5) + chunk;
        // 获取mimeType
        String mimeType = getMimeType(null);
        // 将分块文件上传到Minio
        boolean b = addMediaFilesToMinIO(file, mimeType, bucket_videofiles, chunkFileFolderPath);
        if (!b) {
            return RestResponse.validfail(false, "上传分块文件失败");
        }
        // 上传成功
        return RestResponse.success(true);
    }

    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        // 分块文件所在目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        // 源文件名
        String filename = uploadFileParamsDto.getFilename();
        // 扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        // 合并后的文件的objectName
        String objectName = getFilePathByMd5(fileMd5, extension);
        // 找到所有的分块文件， Stream.iterate类似for(int i = 0; i < chunkTotal; i++)
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder()
                        .bucket(bucket_videofiles)
                        .object(chunkFileFolderPath + i)
                        .build()
                )
                .collect(Collectors.toList());
        // 指定合并后文件的object信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_videofiles)
                .object(objectName)
                .sources(sources)
                .build();
        // ====================合并文件====================
        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件到minio出错,bucket:{},objectName:{},错误原因:{}", bucket_videofiles, objectName, e.getMessage());
            return RestResponse.validfail(false, "合并文件异常");
        }

        // ====================校验合并后的和源文件是否一致，视频上传才成功====================
        // TODO: 如果文件大，下载非常消耗服务器io
        File file = downloadFileFromMinIO(bucket_videofiles, objectName);
        // 计算合并后文件的md5
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String mergeFile_md5 = DigestUtils.md5Hex(fileInputStream);
            // 比较原始的md5和合并后文件的md5
            if (!fileMd5.equals(mergeFile_md5)) {
                log.error("校验合并文件md5值不一致,原始文件:{},合并文件:{}", fileMd5, mergeFile_md5);
                return RestResponse.validfail(false, "文件校验失败");
            }
            // 文件大小
            uploadFileParamsDto.setFileSize(file.length());
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return RestResponse.validfail(false, "文件校验失败");
        }
        // TODO: minio的etag与原文件的不一致()，可能算法不一样
        //        StatObjectArgs testbucket = StatObjectArgs.builder()
        //                .bucket("testbucket")
        //                .object("test/复审.txt")
        //                .build();
        //        try {
        //            StatObjectResponse statObjectResponse = minioClient.statObject(testbucket);
        //            // 获取远程服务文件的etag值
        //            String mergeFile_md5 = statObjectResponse.etag();
        //            if (!fileMd5.equals(mergeFile_md5)) {
        //                log.error("校验合并文件md5值不一致,原始文件:{},合并文件:{}", fileMd5, mergeFile_md5);
        //                return RestResponse.validfail(false, "文件校验失败");
        //            }
        //            // 文件大小
        //            uploadFileParamsDto.setFileSize(statObjectResponse.size());
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //            return RestResponse.validfail(false, "文件校验失败");
        //        }

        // ====================将文件信息入库====================
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_videofiles, objectName);
        if (mediaFiles == null) {
            return RestResponse.validfail(false, "文件入库失败");
        }
        // ====================清理分块文件====================
        clearChunkFiles(chunkFileFolderPath, chunkTotal);

        return RestResponse.success(true);
    }

    /**
     * 清除分块文件
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal 分块文件总数
     */
    private void clearChunkFiles(String chunkFileFolderPath, int chunkTotal){

        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                    .collect(Collectors.toList());

            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(bucket_videofiles).objects(deleteObjects).build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            results.forEach(r->{
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("清楚分块文件失败,objectname:{}",deleteError.objectName(),e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("清楚分块文件失败,chunkFileFolderPath:{}",chunkFileFolderPath,e);
        }
    }

    /**
     * 从minio下载文件
     * @param bucket 桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    public File downloadFileFromMinIO(String bucket, String objectName){
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile = File.createTempFile("minio", ".merge");
            // jvm退出时自动删除文件(可以手动删除minioFile.delete())
            minioFile.deleteOnExit();
            outputStream = new FileOutputStream(minioFile);
            // 用流把下载的文件复制给临时文件
            IOUtils.copy(stream, outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public MediaFiles getFileById(String mediaId) {
        return mediaFilesMapper.selectById(mediaId);
    }

    //得到分块文件的目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    /**
     * 得到合并后的文件的地址
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePathByMd5(String fileMd5, String fileExt){
        return fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 + fileExt;
    }

    // 获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/") + "/";
        return folder;
    }

    // 获取文件的md5
    private String getFileMd5(MultipartFile file) {
        String fileMd5 = null;
        try {
            fileMd5 = DigestUtils.md5Hex(file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileMd5;
    }

    private String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }
        // 通过扩展名得到媒体资源类型 mineType 使用com.j256.simplemagic
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // 通用mimeType，字节流
        // 有匹配的类型，就重新赋值，否则用通用字节流类型
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    /**
     * @param file       文件
     * @param bucket     桶
     * @param objectName 对象名称
     * @return boolean
     * @description 将文件写入minIO
     */
    public boolean addMediaFilesToMinIO(MultipartFile file, String mimeType, String bucket, String objectName) {
        try {
            PutObjectArgs testbucket = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(mimeType)
                    .build();
            minioClient.putObject(testbucket);
            log.debug("上传文件到minio成功,bucket:{},objectName:{}", bucket, objectName);
            System.out.println("上传成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}", bucket, objectName, e.getMessage(), e);
            XueChengPlusException.cast("上传文件到文件系统失败");
        }
        return false;
    }

    /**
     * @description 将文件写入minIO
     * @param localFilePath  文件地址
     * @param bucket  桶
     * @param objectName 对象名称
     * @return void
     * @author Mr.M
     * @date 2022/10/12 21:22
     */
    public boolean addMediaFilesToMinIO2(String localFilePath,String mimeType,String bucket, String objectName) {
        try {
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(localFilePath)
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(testbucket);
            log.debug("上传文件到minio成功,bucket:{},objectName:{}",bucket,objectName);
            System.out.println("上传成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}",bucket,objectName,e.getMessage(),e);
            XueChengPlusException.cast("上传文件到文件系统失败");
        }
        return false;
    }
}
