package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @description 课程发布相关接口
 */
@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Resource
    CourseBaseMapper courseBaseMapper;

    @Resource
    CourseMarketMapper courseMarketMapper;

    @Resource
    CoursePublishPreMapper coursePublishPreMapper;

    @Resource
    CoursePublishMapper coursePublishMapper;

    @Resource
    CourseBaseInfoService courseBaseInfoService;

    @Resource
    TeachplanService teachplanService;

    @Resource
    CourseTeacherService courseTeacherService;

    @Resource
    MediaServiceClient mediaServiceClient;

    @Resource
    MqMessageService mqMessageService;

    @Resource
    RedissonClient redissonClient;

    @Resource
    RedisTemplate redisTemplate;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {

        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        // 课程基本信息，营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfo);
        // 课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        coursePreviewDto.setTeachplans(teachplanTree);

        return coursePreviewDto;
    }

    @Transactional
    @Override
    public void commitAudit(Long companyId, Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        // 课程审核状态
        String auditStatus = courseBase.getAuditStatus();
        // 当前审核状态为已提交不允许再次提交
        if ("202003".equals(auditStatus)) {
            XueChengPlusException.cast("当前为等待审核状态，审核完成可以再次提交。");
        }

        // 本机构只允许提交本机构的课程
        if (!courseBase.getCompanyId().equals(companyId)) {
            XueChengPlusException.cast("不允许提交其它机构的课程。");
        }
        // 课程图片是否填写
        if (StringUtils.isEmpty(courseBase.getPic())) {
            XueChengPlusException.cast("提交失败，请上传课程图片");
        }
        // 查询课程计划
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if (teachplanTree == null || teachplanTree.size() == 0) {
            // 课程计划为空
            XueChengPlusException.cast("请编写课程计划");
        }
        // 查询教师
        List<CourseTeacher> courseTeachers = courseTeacherService.queryCourseTeacherList(courseId);
        if (courseTeachers == null || courseTeachers.size() == 0) {
            // 课程教师为空
            XueChengPlusException.cast("请添加课程教师");
        }

        // 查询到课程基本信息、营销信息、计划等信息插入到课程预发布表
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //课程基本信息加部分营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        // 设置机构id
        coursePublishPre.setCompanyId(companyId);
        //课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        // 转json
        // 营销信息
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);
        // 课程计划
        String teachplanTreeString = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeString);
        // 教师信息
        String courseTeachersString = JSON.toJSONString(courseTeachers);
        coursePublishPre.setTeachers(courseTeachersString);
        // 状态为已提交
        coursePublishPre.setStatus("202003");
        // 创建时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        // 查询预发布表，如果有记录则更新，没有则插入
        CoursePublishPre coursePublishPreOld = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreOld == null) {
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        // 更新课程基本信息表的审核状态为已提交
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {
        // 查询预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            XueChengPlusException.cast("课程没有审核记录，无法发布");
        }

        //本机构只允许提交本机构的课程
        if (!coursePublishPre.getCompanyId().equals(companyId)) {
            XueChengPlusException.cast("不允许提交其它机构的课程。");
        }

        // 课程如果没有审核通过，不允许发布
        String status = coursePublishPre.getStatus();
        if (!"202004".equals(status)) {
            XueChengPlusException.cast("课程没有通过审核，不允许发布");
        }

        // 向课程发布表写入数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);

        // 先查询课程发布，如果有则更新，没有再添加
        CoursePublish coursePublishOld = coursePublishMapper.selectById(courseId);
        if (coursePublishOld == null) {
            coursePublishMapper.insert(coursePublish);
        } else {
            coursePublishMapper.updateById(coursePublish);
        }

        // 向消息表写入数据
        saveCoursePublishMessage(courseId);

        // 删除预发布表数据
        coursePublishPreMapper.deleteById(courseId);

    }

    @Override
    public File generateCourseHtml(Long courseId) {
        //静态化文件
        File htmlFile = null;

        try {
            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
//            String classpath = this.getClass().getResource("/").getPath();
//            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));

            //更改为如下方式
            configuration.setTemplateLoader(new ClassTemplateLoader(this.getClass().getClassLoader(), "/templates"));

            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            //创建静态化文件
            htmlFile = File.createTempFile("course", ".html");
            log.debug("课程静态化，生成静态文件:{}", htmlFile.getAbsolutePath());
            //输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("课程静态化异常:{}", e.toString());
            XueChengPlusException.cast("课程静态化异常");
        }

        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        try {
            // 将File转成MultipartFile
            MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
            String course = mediaServiceClient.uploadFile(multipartFile, "course/" + courseId + ".html");
            if (course == null) {
                log.debug("远程调用走降级逻辑得到上传的结果为null,课程id:{}", courseId);
                XueChengPlusException.cast("上传静态文件异常");
            }
        } catch (Exception e) {
            e.printStackTrace();
            XueChengPlusException.cast("上传静态文件异常");
        }

    }

    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        return coursePublish;
    }

//    @Override
//    public CoursePublish getCoursePublishCache(Long courseId) {
//        String cacheKey = "course_publish_" + courseId;
//        Object object = redisTemplate.opsForValue().get(cacheKey);
//        CoursePublish coursePublish;
//        if (object != null) {
//            String objectString = object.toString();
//            if ("null".equals(objectString)) {
//                return null;
//            }
//            coursePublish = JSON.parseObject(objectString, CoursePublish.class);
//            return coursePublish;
//        } else {
//            // 同步锁，防止缓存击穿,只有一个线程执行，其他线程都会阻塞(性能一般)
//            // 注意锁的范围和粒度，只锁查询数据库代码即可
//            synchronized (this) {
//                // 第一个线程查询完数据库，已经把数据缓存，所以其他线程可以读缓存数据，不用查询数据库
//                object = redisTemplate.opsForValue().get(cacheKey);
//                if (object != null) {
//                    String objectString = object.toString();
//                    if ("null".equals(objectString)) {
//                        return null;
//                    }
//                    coursePublish = JSON.parseObject(objectString, CoursePublish.class);
//                    return coursePublish;
//                }
//                System.out.println("=================从数据库查询=================");
//                coursePublish = getCoursePublish(courseId);
//                // 设置不同缓存时间，防止缓存雪崩(同一时间，缓存过期失效)
//                redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(coursePublish), 300 + new Random().nextInt(100), TimeUnit.SECONDS);
//                return coursePublish;
//            }
//        }
//    }

    // Redisson分布式锁
    public CoursePublish getCoursePublishCache(Long courseId) {
        //查询缓存
        String jsonString = (String) redisTemplate.opsForValue().get("course:" + courseId);
        if (StringUtils.isNotEmpty(jsonString)) {
            if (jsonString.equals("null")) {
                return null;
            }
            CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
            return coursePublish;
        } else {
            //每门课程设置一个锁
            RLock lock = redissonClient.getLock("coursequerylock:" + courseId);
            //获取锁
            lock.lock();
            try {
                jsonString = (String) redisTemplate.opsForValue().get("course:" + courseId);
                if (StringUtils.isNotEmpty(jsonString)) {
                    CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
                    return coursePublish;
                }
                System.out.println("=========从数据库查询==========");
                // 测试看门狗是否会自动续期锁
//                try {
//                    Thread.sleep(60000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
                //从数据库查询
                CoursePublish coursePublish = getCoursePublish(courseId);
                redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish), 10, TimeUnit.SECONDS);
                return coursePublish;
            } finally {
                //释放锁
                lock.unlock();
            }
        }
    }


    /**
     * @param courseId 课程id
     * @return void
     * @description 保存消息表记录
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null) {
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
    }

}
