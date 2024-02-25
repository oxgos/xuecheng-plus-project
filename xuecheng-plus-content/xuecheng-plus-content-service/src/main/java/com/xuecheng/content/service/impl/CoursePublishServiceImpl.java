package com.xuecheng.content.service.impl;

import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description 课程发布相关接口
 */
@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Resource
    CourseBaseInfoService courseBaseInfoService;

    @Resource
    TeachplanService teachplanService;

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

}
