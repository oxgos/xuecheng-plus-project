package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @description 课程分类管理业务接口
*/
public interface CourseCategoryService {

    /**
     * 课程分类查询
     * @return
     */
    public List<CourseCategoryTreeDto> queryCourseCategory(String id);
}
