package com.xuecheng.learning.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(value = "学习过程管理接口", tags = "学习过程管理接口")
@RestController
@RequestMapping("/open")
public class MyLearningOpenController {

    @Resource
    LearningService learningService;

    @ApiOperation("获取视频")
    @GetMapping("/learn/getvideo/{courseId}/{teachplanId}/{mediaId}")
    public RestResponse<String> getVideo(@PathVariable("courseId") Long courseId, @PathVariable("teachplanId") Long teachplanId, @PathVariable("mediaId") String mediaId) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String userId = null;
        if(user != null){
            userId = user.getId();
        }
        //获取视频
        RestResponse<String> video = learningService.getVideo(userId, courseId, teachplanId, mediaId);

        return video;

    }
}
