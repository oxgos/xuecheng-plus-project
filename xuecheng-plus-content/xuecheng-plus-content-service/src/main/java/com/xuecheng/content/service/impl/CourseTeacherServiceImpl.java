package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.SaveCourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    @Override
    public void saveCourseTeacher(SaveCourseTeacherDto dto) {
        Long id = dto.getId();
        if (id == null) {
            CourseTeacher courseTeacher = new CourseTeacher();
            BeanUtils.copyProperties(dto, courseTeacher);
            courseTeacherMapper.insert(courseTeacher);
        } else {
            CourseTeacher courseTeacherOld = courseTeacherMapper.selectById(id);
            if (courseTeacherOld == null) {
                XueChengPlusException.cast("教师不存在");
            }
            BeanUtils.copyProperties(dto, courseTeacherOld);
            courseTeacherMapper.updateById(courseTeacherOld);
        }
    }

    @Override
    public void deleteCourseTeacher(Long courseId, Long teacherId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId).eq(CourseTeacher::getId, teacherId);
        courseTeacherMapper.delete(queryWrapper);
    }

    private void addCourseTeacher() {}
}
