package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.HandleTeachplanDTO;
import com.xuecheng.content.model.dto.TeachplanTreeDTO;

import java.util.List;

/**
 * @Author gc
 * @Description 课程计划接口
 * @DateTime: 2025/5/13 18:09
 **/
public interface TeachplanyService {
    /**
     * 查询教学计划树
     * @param id 课程id
     * @return
     */
    List<TeachplanTreeDTO> queryTeachplanyTree(Long id);

    /**
     * 保存课程计划
     * @param handleTeachplanDTO 课程计划传输模型
     * @return
     */
    HandleTeachplanDTO saveTeachplany(HandleTeachplanDTO handleTeachplanDTO);

    /**
     * 修改课程计划
     * @param handleTeachplanDTO 课程计划传输模型
     * @return
     */
    HandleTeachplanDTO editTeachplany(HandleTeachplanDTO handleTeachplanDTO);

    /**
     * 处理课程计划向下移动
     * @param handle
     * @param id
     * @return
     */
    Object handleTeachplanMoveOderby(String handle,Long id);

    /**
     * 删除课程计划
     * @param id
     * @return
     */
    Object removeTeachplan(Long id);
}
