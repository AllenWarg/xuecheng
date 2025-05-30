package com.xuecheng.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.po.XcCourseTables;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface XcCourseTablesMapper extends BaseMapper<XcCourseTables> {
    List<XcCourseTables> selectMyCourseTables(IPage<XcCourseTables> page, @Param("params") MyCourseTableParams params);
}
