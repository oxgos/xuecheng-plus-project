package com.xuecheng.content.service;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
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

    public void moveTeachplan(String direction, Long treeplanId);
}
