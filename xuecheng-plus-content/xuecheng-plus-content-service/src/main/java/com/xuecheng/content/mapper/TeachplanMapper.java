package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.TeachplanTreeDTO;
import com.xuecheng.content.model.po.Teachplan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {
    /**
     * 课程计划节点树查询
     * @param id 课程id
     * @return
     */
    List<TeachplanTreeDTO> selectTeachplanTree(Long id);


    /**
     * 查询课程计划最新的排序序号
     * @param id 课程id
     * @param parentId 父亲课程计划id
     * @return
     */
    Integer selectTeachplanyOrderby(@Param("id") Long id, @Param("parentId") Long parentId,@Param("sort") String sort);

    /**
     * 更新章节或者小节的排序字段
     * @param id 课程id
     * @param parentId 父母节点
     * @param orderBy 排序字段
     * @param orderByNUm 更新的具体值
     * @return
     */
    Integer updateTeachplanyOrderby(@Param("id") Long id,
                                    @Param("parentId") Long parentId,
                                    @Param("orderBy") Integer orderBy,
                                    @Param("orderByNUm") Integer orderByNUm);
}
