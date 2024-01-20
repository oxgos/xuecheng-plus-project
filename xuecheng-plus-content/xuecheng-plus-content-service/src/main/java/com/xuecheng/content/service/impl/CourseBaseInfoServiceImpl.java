package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Resource
    CourseBaseMapper courseBaseMapper;

    @Resource
    CourseMarketMapper courseMarketMapper;

    @Resource
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        // 详细进行分页查询的单元测试
        // 拼装查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        if (ObjectUtils.isNotEmpty(queryCourseParamsDto)) {
            // 根据名称模糊查询,在sql中拼接course_base.name like '%值%'
            queryWrapper.like(
                    StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),
                    CourseBase::getName,
                    queryCourseParamsDto.getCourseName()
            );
            // 根据课程审核状态查询 course_base.audit_status = ?
            queryWrapper.eq(
                    StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),
                    CourseBase::getAuditStatus,
                    queryCourseParamsDto.getAuditStatus()
            );
            // 根据课程发布状态查询
            queryWrapper.eq(
                    StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),
                    CourseBase::getStatus,
                    queryCourseParamsDto.getPublishStatus()
            );
        }
        // 创建page分页参数对象,参数：当前页码，每页记录数
        Page<CourseBase> page = new Page(pageParams.getPageNo(), pageParams.getPageSize());

        // 开始进行分页查询
        Page<CourseBase> courseBasePage = courseBaseMapper.selectPage(page, queryWrapper);

        // List<T> items, long counts, long page, long pageSize
        List<CourseBase> records = courseBasePage.getRecords();
        long total = courseBasePage.getTotal();
        PageResult<CourseBase> pageResult = new PageResult<>(records, total, courseBasePage.getCurrent(), courseBasePage.getSize());
        return pageResult;
    }

    // 增、岀、改都要做事务控制
    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {

        // 参数合法性校验
        if (StringUtils.isEmpty(dto.getName())) {
            throw new RuntimeException("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            throw new RuntimeException("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new RuntimeException("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            throw new RuntimeException("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            throw new RuntimeException("收费规则为空");
        }
        // 向课程基本信息表course_base写入数据
        CourseBase courseBaseNew = new CourseBase();
        // courseBase.setName(dto.getName());
        // courseBase.setDescription(dto.getDescription());
        // 上边的从原始对象中get拿数据向新对象set，比较复杂
        BeanUtils.copyProperties(dto, courseBaseNew); // 只要属性名称一致就可以拷贝
        courseBaseNew.setCompanyId(companyId);
        courseBaseNew.setCreateDate(LocalDateTime.now());
        // 审核状态默认为未提交
        courseBaseNew.setAuditStatus("202002");
        courseBaseNew.setStatus("203001");

        int insert = courseBaseMapper.insert(courseBaseNew);
        if (insert <= 0) {
            throw new RuntimeException("添加课程失败");
        }

        // 向课程营销表course_market写入数据
        CourseMarket courseMarketNew = new CourseMarket();
        // 将页面输入的数据拷贝到courseMarketNew
        BeanUtils.copyProperties(dto, courseMarketNew);
        // 主键课程的id
        Long courseId = courseBaseNew.getId(); // mybatis-plus插入成功之后就会有id, 如果不是,则需要在xml添加额外配置
        courseMarketNew.setId(courseId);
        // 保存营销信息
        saveCourseMarket(courseMarketNew);
        // 从数据库查询课程详细信息
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(courseId);
        return courseBaseInfo;
    }

    // 查询课程信息
    private CourseBaseInfoDto getCourseBaseInfo(long courseId) {
        // 从课程基本信息表查询
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            return null;
        }
        // 从课程营销表查询
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        // 组装在一起
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }
        // 通过courseCategoryMapper查询分类信息，将分类名称放在courseBaseInfoDto对象中
        CourseCategory courseCategoryMt = courseCategoryMapper.selectById(courseBaseInfoDto.getMt());
        CourseCategory courseCategorySt = courseCategoryMapper.selectById(courseBaseInfoDto.getSt());
        if (courseCategoryMt != null) {
            courseBaseInfoDto.setMtName(courseCategoryMt.getName());
        }
        if (courseCategorySt != null) {
            courseBaseInfoDto.setStName(courseCategorySt.getName());
        }
        return courseBaseInfoDto;
    }

    // 单独写一个方法保存营销信息，逻辑：存在则更新，不存在则添加
    private int saveCourseMarket(CourseMarket courseMarketNew) {
        // 参数合法性校验
        String charge = courseMarketNew.getCharge();
        if (StringUtils.isEmpty(charge)) {
            throw new RuntimeException("收费规则为空");
        }
        // 如果课程收费，价格没有填写也要抛出异常
        if (charge.equals("201001")) {
            if (courseMarketNew.getPrice() == null || courseMarketNew.getPrice() <= 0) {
                throw new RuntimeException("课程的价格不能为空并且必须大于0");
            }
        }
        Long id = courseMarketNew.getId();
        // 查询数据库，是否存在
        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        if (courseMarket == null) {
            // 插入数据库
            int insert = courseMarketMapper.insert(courseMarketNew);
            return insert;
        } else {
            // 将courseMarketNew数据拷贝给courseMarket
            BeanUtils.copyProperties(courseMarketNew, courseMarket);
            courseMarket.setId(courseMarketNew.getId());
            // 更新
            int i = courseMarketMapper.updateById(courseMarket);
            return i;
        }
    }

}
