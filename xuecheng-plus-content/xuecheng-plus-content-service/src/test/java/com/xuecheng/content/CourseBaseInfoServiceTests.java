package com.xuecheng.content;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@SpringBootTest
public class CourseBaseInfoServiceTests {

    @Resource
    CourseBaseInfoService courseBaseInfoService;

    @Test
    public void testQueryCourseBaseList() {
        PageParams pageParams = new PageParams();
        pageParams.setPageNo(1L);
        pageParams.setPageSize(5L);
        QueryCourseParamsDto queryCourseParamsDto = new QueryCourseParamsDto();
        queryCourseParamsDto.setCourseName("java");
        queryCourseParamsDto.setAuditStatus("202004"); // 202004表示课程审核通过
        PageResult<CourseBase> pageResult = courseBaseInfoService.queryCourseBaseList(null, pageParams, queryCourseParamsDto);
        System.out.println(pageResult);
    }


    @Test
    @Transactional
    public void testCreateCourseBaseValidation() {
        AddCourseDto dto = new AddCourseDto();// 初始化你的DTO对象

        // 测试空名称
        dto.setName("");
        assertValidationFails(dto, "课程名称为空");
        dto.setName("123");

        // 测试空课程分类
        dto.setMt("");
        assertValidationFails(dto, "课程分类为空");
        dto.setMt("1-1");

        // 测试空课程分类
        dto.setSt("");
        assertValidationFails(dto, "课程分类为空");
        dto.setSt("1-1-1");

        // 测试空课程等级
        dto.setGrade("");
        assertValidationFails(dto, "课程等级为空");
        dto.setGrade("204001");

        // 测试空教育模式
        dto.setTeachmode("");
        assertValidationFails(dto, "教育模式为空");
        dto.setTeachmode("200002");

        // 测试空适应人群
        dto.setUsers("");
        assertValidationFails(dto, "适应人群为空");
        dto.setUsers("具有web开发基础");

        // 测试空收费规则
        dto.setCharge("");
        assertValidationFails(dto, "收费规则为空");
        dto.setCharge("201000");

        // 测试通过的情况
        Long companyId = 1232141425L;
        CourseBaseInfoDto courseBase = courseBaseInfoService.createCourseBase(companyId, dto);
        System.out.println(courseBase);
        Assertions.assertNotNull(courseBase);
    }

    private void assertValidationFails(AddCourseDto dto, String expectedErrorMessage) {
        try {
            Long companyId = 1232141425L;
            courseBaseInfoService.createCourseBase(companyId, dto);
        } catch (RuntimeException e) {
            Assertions.assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

}
