package com.xuecheng.content.api;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.model.dto.SaveCourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Api(value = "课程老师接口", tags = "课程老师接口")
@RequestMapping("/courseTeacher")
public class CourseTeacherController {

    @Resource
    CourseTeacherService courseTeacherService;

    @ApiOperation("查询课程教师列表")
    @ApiImplicitParam(value = "courseId", name = "课程Id", required = true, dataType = "Long", paramType = "path", example = "1")
    @GetMapping("/list/{courseId}")
    public List<CourseTeacher> list(@PathVariable Long courseId) {
        List<CourseTeacher> courseTeachers = courseTeacherService.queryCourseTeacherList(courseId);
        return courseTeachers;
    }

    @ApiOperation("新增课程教师")
    @PostMapping()
    public void saveCourseTeacher(@RequestBody @Validated SaveCourseTeacherDto dto) {
        courseTeacherService.saveCourseTeacher(dto);
    }
}
