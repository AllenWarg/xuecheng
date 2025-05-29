package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.AddCourseBaseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.util.SecurityUtil;
import com.xuecheng.execption.ValidationGroups;
import com.xuecheng.model.PageParams;
import com.xuecheng.model.PageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @description 课程信息编辑接口
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
@Slf4j
@Api(value = "课程信息编辑接口",tags = "课程信息编辑接口")
@RestController
public class CourseBaseInfoController {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @PreAuthorize("hasAuthority('xc_teachmanager_course')")
    @ApiOperation("课程查询列表接口")
    @RequestMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required=false) QueryCourseParamsDto queryCourseParams){
        String companyId = SecurityUtil.getUser().getCompanyId();
        if (companyId==null){
            return null;
        }
        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(Long.valueOf(companyId),pageParams, queryCourseParams);
        return courseBasePageResult;
    }


    @ApiOperation("通过id查询课程接口")
    @RequestMapping("/course/{id}")
    public AddCourseBaseDto list(@PathVariable("id") Long id){
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        System.out.println(user);
        return courseBaseInfoService.queryCourseBaseById(id);
    }

    @ApiOperation("课程添加接口")
    @PostMapping("/course")
    public AddCourseBaseDto addCourseBaseInfo(@RequestBody @Validated(ValidationGroups.Inster.class) AddCourseBaseDto addCourseBaseDto){
        Long companyId=594000L;
        return courseBaseInfoService.addCourseBaseInfo(addCourseBaseDto,companyId);
    }

    @ApiOperation("课程修改接口")
    @PutMapping ("/course")
    public AddCourseBaseDto editCourseBaseInfo(@RequestBody @Validated(ValidationGroups.Update.class) AddCourseBaseDto addCourseBaseDto){
        return courseBaseInfoService.editCourseBaseInfo(addCourseBaseDto);
    }

    /**
     * 删除课程
     * @param id 课程信息id
     */
    @ApiOperation("删除课程接口")
    @DeleteMapping ("/course/{id}")
    public void removeCourseBaseInfo(@PathVariable Long id){
        courseBaseInfoService.removeCourseBaseInfo(id);
    }




}
