package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;

/**
 * @description 课程预览，发布
 */
@Controller
@RequestMapping("/coursepreview")
public class CoursePublishController {

    @Resource
    CoursePublishService coursePublishService;


    @GetMapping("/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId) {

        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);

        ModelAndView modelAndView = new ModelAndView();
        // 指定模型
        modelAndView.addObject("model", coursePreviewInfo);
        // 指定模板
        modelAndView.setViewName("course_template");

        return modelAndView;
    }

}