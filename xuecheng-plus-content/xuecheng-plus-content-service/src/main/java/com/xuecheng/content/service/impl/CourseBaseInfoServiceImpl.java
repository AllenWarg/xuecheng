package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseBaseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
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

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParams) {
        //构建分页
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //构建查询条件
        LambdaQueryWrapper<CourseBase> qw = new LambdaQueryWrapper<>();
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
    public AddCourseBaseDto addCourseBaseInfo(AddCourseBaseDto addCourseBaseDto) {
        CourseBase courseBase = new CourseBase();
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(addCourseBaseDto, courseBase);
        BeanUtils.copyProperties(addCourseBaseDto, courseMarket);
        courseBase.setAuditStatus("203001");
        courseBase.setStatus("202002");
        courseBase.setCompanyId(594000L);
        courseBase.setCompanyName("我就是000");
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
}
