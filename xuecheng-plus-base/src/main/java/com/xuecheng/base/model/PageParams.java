package com.xuecheng.base.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description 分页查询通用参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageParams {

    @ApiModelProperty(value = "当前页码", example = "1")
    private Long pageNo = 1L;

    @ApiModelProperty(value = "每页记录数默认值", example = "10")
    private Long pageSize = 10L;
}
