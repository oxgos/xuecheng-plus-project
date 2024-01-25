package com.xuecheng.content.components;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.po.CourseBase;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class CourseCompanyValidator {

    @Resource
    private CourseBaseMapper courseBaseMapper;

    /**
     * 判断课程是否属于同一机构
     * @param companyId
     * @param courseId
     * @return CourseBase
     */
    public CourseBase checkIsSameCompany(Long companyId, Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            XueChengPlusException.cast("课程不存在");
        }
        // 校验本机构只能修改本机构的课程
        if (!courseBase.getCompanyId().equals(companyId)) {
            XueChengPlusException.cast("本机构只能修改本机构的课程");
        }
        return courseBase;
    }
}