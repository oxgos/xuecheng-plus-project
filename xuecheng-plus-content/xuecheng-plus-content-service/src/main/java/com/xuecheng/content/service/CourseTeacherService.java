package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveCourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

public interface CourseTeacherService {

    public List<CourseTeacher> queryCourseTeacherList(Long courseId);

    public CourseTeacher saveCourseTeacher(Long companyId, SaveCourseTeacherDto dto);

    public void deleteCourseTeacher(Long courseId, Long teacherId);
}
