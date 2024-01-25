package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(value="新增课程教师参数", description="新增课程教师参数")
public class SaveCourseTeacherDto {

    @ApiModelProperty(value = "课程id", required = true)
    @NotNull(message = "课程id不能为空")
    private Long courseId;

    @ApiModelProperty(value = "教师名称", required = true)
    @NotEmpty(message = "教师名称不能为空")
    private String teacherName;

    @ApiModelProperty(value = "教师职位", required = true)
    @NotEmpty(message = "教师职位不能为空")
    private String position;

    @ApiModelProperty(value = "教师简介")
    private String introduction;

    @ApiModelProperty(value = "教师照片")
    private String photograph;
}
