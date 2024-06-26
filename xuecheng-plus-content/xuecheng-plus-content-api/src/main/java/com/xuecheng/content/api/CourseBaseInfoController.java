package com.xuecheng.content.api;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.*;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Api(value = "课程信息编辑接口", tags = "课程信息编辑接口")
@RequestMapping("/course")
public class CourseBaseInfoController {

    @Resource
    CourseBaseInfoService courseBaseInfoService;

    @Resource
    CoursePublishService coursePublishService;

    @ApiOperation("课程查询接口")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')") // 拥有课程列表查询的权限方可访问
    @PostMapping("/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParams) {
        Long companyId = getUserCompanyId();
        PageResult<CourseBase> pageResult = courseBaseInfoService.queryCourseBaseList(companyId, pageParams, queryCourseParams);
        return pageResult;
    }

    @ApiOperation("根据课程id查询接口")
    @ApiImplicitParam(value = "courseId", name = "课程Id", required = true, dataType = "Long", paramType = "path", example = "1")
    @GetMapping("/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId) {
        //取出当前用户身份
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        System.out.println(principal);
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        System.out.println(user);

        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.getCourseBaseInfo(courseId);
        return courseBaseInfoDto;
    }

    @ApiOperation("新增课程接口")
    @PostMapping()
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidationGroups.Insert.class) AddCourseDto addCourseDto) {
        // 获取用户所属机构的id
        Long companyId = getUserCompanyId();

        CourseBaseInfoDto courseBase = courseBaseInfoService.createCourseBase(companyId, addCourseDto);

        return courseBase;
    }

    @ApiOperation("修改课程接口")
    @PutMapping()
    public CourseBaseInfoDto updateCourseBase(@RequestBody @Validated(ValidationGroups.Update.class) EditCourseDto editCourseDto) {
        // 获取用户所属机构的id
        Long companyId = getUserCompanyId();

        CourseBaseInfoDto courseBase = courseBaseInfoService.updateCourseBase(companyId, editCourseDto);

        return courseBase;
    }

    @ApiOperation("删除课程接口")
    @DeleteMapping("/{courseId}")
    public void deleteCourseBase(@PathVariable Long courseId) {

        courseBaseInfoService.deleteCourseBase(courseId);

    }

    @ApiOperation("获取课程发布信息")
    @ResponseBody
    @GetMapping("/whole/{courseId}")
    public CoursePreviewDto getCoursePublish(@PathVariable("courseId") Long courseId) {
        //查询课程发布信息
        CoursePublish coursePublish = coursePublishService.getCoursePublishCache(courseId);
        if (coursePublish == null) {
            return new CoursePreviewDto();
        }

        //课程基本信息
        CourseBaseInfoDto courseBase = new CourseBaseInfoDto();
        BeanUtils.copyProperties(coursePublish, courseBase);
        //课程计划
        List<TeachplanDto> teachplans = JSON.parseArray(coursePublish.getTeachplan(), TeachplanDto.class);
        CoursePreviewDto coursePreviewInfo = new CoursePreviewDto();
        coursePreviewInfo.setCourseBase(courseBase);
        coursePreviewInfo.setTeachplans(teachplans);
        return coursePreviewInfo;
    }

    private Long getUserCompanyId() {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = null;
        if (!StringUtils.isEmpty(user.getCompanyId())) {
            companyId = Long.parseLong(user.getCompanyId());
        }
        return companyId;
    }

}
