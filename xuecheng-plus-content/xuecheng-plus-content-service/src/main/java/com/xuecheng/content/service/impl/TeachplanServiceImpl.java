package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description 课程计划service接口实现类
 */
@Slf4j
@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Resource
    TeachplanMapper teachplanMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    /**
     * 新增、修改课程计划
     * @param teachplan
     */
    @Transactional
    @Override
    public void saveTeachplan(SaveTeachplanDto teachplan) {
        // 通过课程计划id判断是新增和修改
        Long teachplanId = teachplan.getId();
        if (teachplanId == null) {
            // 新增
            Teachplan teachplanNew = new Teachplan();
            BeanUtils.copyProperties(teachplan, teachplanNew);
            // 确定排序字段, 取出同父同级别的课程计划数量
            int count = getTeachplanCount(teachplan.getCourseId(), teachplan.getParentid());
            teachplanNew.setOrderby(count + 1);

            teachplanMapper.insert(teachplanNew);
        } else {
            // 修改
            Teachplan teachplanOld = teachplanMapper.selectById(teachplanId);
            if (teachplanOld == null) {
                XueChengPlusException.cast("课程不存在");
            }
            BeanUtils.copyProperties(teachplan, teachplanOld);
            teachplanMapper.updateById(teachplanOld);
        }
    }

    /**
     * @description 获取最新的排序号
     * @param courseId  课程id
     * @param parentId  父课程计划id
     * @return int 最新排序号
     */
    private int getTeachplanCount(Long courseId, Long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(Teachplan::getCourseId, courseId)
                .eq(Teachplan::getParentid, parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count;
    }
}
