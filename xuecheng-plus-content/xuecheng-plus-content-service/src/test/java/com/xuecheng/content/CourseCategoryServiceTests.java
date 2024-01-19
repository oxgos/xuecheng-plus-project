package com.xuecheng.content;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class CourseCategoryServiceTests {

    @Resource
    CourseCategoryService courseCategoryService;

    @Test
    public void testCourseCategoryService() {
        List<CourseCategoryTreeDto> categoryTreeDtoList = courseCategoryService.queryCourseCategory("1");
        System.out.println(categoryTreeDtoList);
    }

}
