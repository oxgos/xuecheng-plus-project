package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.SaveCourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Resource
    CourseTeacherMapper courseTeacherMapper;

    @Override
    public List<CourseTeacher> queryCourseTeacherList(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(queryWrapper);
        return courseTeachers;
    }

    @Override
    public void saveCourseTeacher(SaveCourseTeacherDto dto) {
        CourseTeacher courseTeacher = new CourseTeacher();
        BeanUtils.copyProperties(dto, courseTeacher);
        courseTeacherMapper.insert(courseTeacher);
    }
}
