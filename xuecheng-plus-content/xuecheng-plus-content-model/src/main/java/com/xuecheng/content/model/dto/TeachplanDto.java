package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @description 课程计划信息模型类
 */
@Data
public class TeachplanDto extends Teachplan {
    // 课程计划关联的媒资信息
    private TeachplanMedia teachplanMedia;

    // 小章节list
    ArrayList<TeachplanDto> teachPlanTreeNodes;
}
