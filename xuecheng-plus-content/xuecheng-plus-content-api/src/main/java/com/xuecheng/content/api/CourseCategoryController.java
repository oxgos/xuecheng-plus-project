package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Api(value = "课程分类接口", tags = "课程分类接口")
@RequestMapping("/course-category")
public class CourseCategoryController {

    @Resource
    CourseCategoryService courseCategoryService;

    @ApiOperation("课程分类查询接口")
    @GetMapping("/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        List<CourseCategoryTreeDto> result =  courseCategoryService.queryCourseCategory("1");
        return result;
    }

}
