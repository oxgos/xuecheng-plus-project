package com.xuecheng.content.service;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.TeachplanMedia;

import java.util.List;

/**
 * @description 课程基本信息管理业务接口
 */
public interface TeachplanService {

    /**
     * @description 查询课程计划树型结构
     * @param courseId  课程id
     * @return List<TeachplanDto>
     */
    public List<TeachplanDto> findTeachplanTree(Long courseId);

    public void saveTeachplan(SaveTeachplanDto teachplan);

    public void deleteTeachplan(Long treeplanId);

    /**
     * 删除课程下所有课程计划
     * @param courseId
     */
    public void deleteAllTeachPLan(Long courseId);

    public void moveTeachplan(String direction, Long treeplanId);

    /**
     * @description 教学计划绑定媒资
     * @param bindTeachplanMediaDto
     * @return com.xuecheng.content.model.po.TeachplanMedia
     */
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    /**
     * @description 解除教学计划绑定媒资
     * @param treeplanId
     * @param mediaId
     */
    public void deleteAssociationMedia(Long treeplanId, String mediaId);
}
