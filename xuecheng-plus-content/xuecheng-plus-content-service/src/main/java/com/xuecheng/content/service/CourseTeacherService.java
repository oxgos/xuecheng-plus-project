package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

public interface CourseTeacherService {

    public List<CourseTeacher> queryCourseTeacherList(Long courseId);
}