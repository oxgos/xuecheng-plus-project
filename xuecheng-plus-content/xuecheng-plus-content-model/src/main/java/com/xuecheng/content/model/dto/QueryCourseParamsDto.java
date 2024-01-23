package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @description 课程查询参数Dto
 */
@Data
@ApiModel(value="课程查询参数", description="课程查询参数")
public class QueryCourseParamsDto {

    @ApiModelProperty(value = "审核状态")
    private String auditStatus;

    @ApiModelProperty(value = "课程名称")
    private String courseName;

    @ApiModelProperty(value = "发布状态")
    private String publishStatus;
}
