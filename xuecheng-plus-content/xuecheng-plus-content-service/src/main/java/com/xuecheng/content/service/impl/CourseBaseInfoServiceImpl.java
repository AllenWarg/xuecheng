package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.AddCourseBaseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.execption.XueChengException;
import com.xuecheng.model.PageParams;
import com.xuecheng.model.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author gc
 * @Description 课程信息表管理接口实现类
 * @DateTime: 2025/5/12 15:46
 **/
@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CourseCategoryMapper courseCategoryMapper;
    @Autowired
    CourseTeacherMapper courseTeacherMapper;
    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;
    @Autowired
    TeachplanMapper teachplanMapper;


    @Override
    public PageResult<CourseBase> queryCourseBaseList(Long companyId,PageParams pageParams, QueryCourseParamsDto queryCourseParams) {
        //构建分页
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //构建查询条件
        LambdaQueryWrapper<CourseBase> qw = new LambdaQueryWrapper<>();
        qw.eq(CourseBase::getCompanyId,companyId);
        qw.eq(StringUtils.isNotEmpty(queryCourseParams.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParams.getAuditStatus())
                .eq(StringUtils.isNotEmpty(queryCourseParams.getPublishStatus()), CourseBase::getStatus, queryCourseParams.getPublishStatus())
                .like(StringUtils.isNotEmpty(queryCourseParams.getCourseName()), CourseBase::getName, queryCourseParams.getCourseName());
        //分页查询
        Page<CourseBase> pages = courseBaseMapper.selectPage(page, qw);
        //得到查询记录
        List<CourseBase> courseBaseList = pages.getRecords();
        //封装返回结果
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(courseBaseList, pages.getTotal(), pages.getCurrent(), pages.getSize());
        return courseBasePageResult;
    }

    public AddCourseBaseDto queryCourseBaseById(Long id){
        AddCourseBaseDto addCourseBaseDto = new AddCourseBaseDto();
        CourseBase courseBase = courseBaseMapper.selectById(id);
        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        if (courseBase==null||courseMarket==null){
            return null;
        }
        BeanUtils.copyProperties(courseBase,addCourseBaseDto);
        BeanUtils.copyProperties(courseMarket,addCourseBaseDto);
        addCourseBaseDto.setId(courseBase.getId());
        CourseCategory mtCourseCategory = courseCategoryMapper.selectById(courseBase.getMt());
        CourseCategory stCourseCategory = courseCategoryMapper.selectById(courseBase.getSt());
        if (mtCourseCategory==null||stCourseCategory==null){
            return null;
        }
        addCourseBaseDto.setMtName(mtCourseCategory.getName());
        addCourseBaseDto.setStName(stCourseCategory.getName());
        return addCourseBaseDto;
    }

    @Transactional
    @Override
    public AddCourseBaseDto addCourseBaseInfo(AddCourseBaseDto addCourseBaseDto,Long companyId) {
        CourseBase courseBase = new CourseBase();
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(addCourseBaseDto, courseBase);
        BeanUtils.copyProperties(addCourseBaseDto, courseMarket);
        courseBase.setAuditStatus("202002"); //课程审核状态
        courseBase.setStatus("203001"); //课程发布状态
        courseBase.setCompanyId(companyId);
        if (StringUtils.isEmpty(courseBase.getName())){
            XueChengException.cast("课程名称不能为空！");
        }
        int insert = courseBaseMapper.insert(courseBase);
        if (insert<=0){
            throw new RuntimeException("添加课程失败！");
        }
        Long id = courseBase.getId();
        courseMarket.setId(id);
        //添加课程营销信息
        saveCourseMarket(courseMarket);
        addCourseBaseDto.setId(id);
        return addCourseBaseDto;
    }

    private void saveCourseMarket(CourseMarket courseMarket){
        if (StringUtils.isEmpty(courseMarket.getCharge())){
            throw new XueChengException("收费规则不能为空！");
        }
        if ("201001".equals(courseMarket.getCharge())){
            if (courseMarket.getPrice()<0||courseMarket.getPrice()==null){
                throw new XueChengException("收费价格不能为空或者为负数！");
            }
        }
        int insert1 = courseMarketMapper.insert(courseMarket);
        if (insert1<=0){
            throw new RuntimeException("添加课程营销失败！");
        }
    }

    @Transactional
    public AddCourseBaseDto editCourseBaseInfo(AddCourseBaseDto addCourseBaseDto){
        CourseBase courseBase = new CourseBase();
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(addCourseBaseDto,courseBase);
        BeanUtils.copyProperties(addCourseBaseDto,courseMarket);
        courseMarket.setId(courseBase.getId());
        courseBase.setId(courseBase.getId());
        int cb = courseBaseMapper.updateById(courseBase);
        int cm = courseMarketMapper.updateById(courseMarket);
        if (cb<=0||cm<=0){
            XueChengException.cast("课程修改失败！");
            return null;
        }
        return addCourseBaseDto;
    }

    @Transactional
    @Override
    public void removeCourseBaseInfo(Long id) {
        CourseBase courseBase = courseBaseMapper.selectById(id);
        if (!"202002".equals(courseBase.getAuditStatus())){
            XueChengException.cast("只能删除未提交审核的课程信息！");
        }
        //删除课程教师
        Integer integer = removeCourseTeacher(id);
        //删除课程计划媒体
        Integer integer1 = removeTeachplanMedia(id);
        //删除课程计划
        Integer integer2 = removeTeachplan(id);
        //删除课程营销
        Integer integer3 = removeCourseMarket(id);
        //删除课程基本信息
        Integer integer4 = courseBaseMapper.deleteById(id);
        if (integer<0||integer1<0||integer2<0||integer3<0||integer4<0){
            XueChengException.cast("删除课程信息失败！");
        }
        log.info("课程信息删除成功！");
    }

    private Integer removeCourseTeacher(Long id){
        LambdaQueryWrapper<CourseTeacher> qw = new LambdaQueryWrapper<>();
        qw.eq(CourseTeacher::getCourseId,id);
        int delete = courseTeacherMapper.delete(qw);
        return delete;
    }
    private Integer removeTeachplanMedia(Long id){
        LambdaQueryWrapper<TeachplanMedia> qw = new LambdaQueryWrapper<>();
        qw.eq(TeachplanMedia::getCourseId,id);
        int delete = teachplanMediaMapper.delete(qw);
        return delete;
    }
    private Integer removeTeachplan(Long id){
        LambdaQueryWrapper<Teachplan> qw = new LambdaQueryWrapper<>();
        qw.eq(Teachplan::getCourseId,id);
        int delete = teachplanMapper.delete(qw);
        return delete;
    }
    private Integer removeCourseMarket(Long id){
        LambdaQueryWrapper<CourseMarket> qw = new LambdaQueryWrapper<>();
        qw.eq(CourseMarket::getId,id);
        int delete = courseMarketMapper.delete(qw);
        return delete;
    }

}
