package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.HandleTeachplanDTO;
import com.xuecheng.content.model.dto.TeachplanTreeDTO;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanyService;
import com.xuecheng.execption.XueChengException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
* @Author gc
* @Description 课程计划接口实现类
* @DateTime: 2025/5/14 23:12
**/
@Service
@Slf4j
public class TeachplanyServiceImpl implements TeachplanyService {
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanTreeDTO> queryTeachplanyTree(Long id) {
        List<TeachplanTreeDTO> teachplanTreeDTO = teachplanMapper.selectTeachplanTree(id);
        return teachplanTreeDTO;
    }

    @Override
    public HandleTeachplanDTO saveTeachplany(HandleTeachplanDTO handleTeachplanDTO) {
        Teachplan teachplan = new Teachplan();
        BeanUtils.copyProperties(handleTeachplanDTO,teachplan);
        Integer maxOrderby = getTeachplanyOrderby(teachplan.getCourseId(), teachplan.getParentid(),"max");
        teachplan.setOrderby(maxOrderby+1);
        int insert = teachplanMapper.insert(teachplan);
        if (insert<=0){
            XueChengException.cast("课程计划添加失败！请重试。。");
        }
        handleTeachplanDTO.setId(teachplan.getId());
        return handleTeachplanDTO;
    }
    /**
     * 删除课程计划
     * @param id 课程计划id
     * @return
     */
    public Object removeTeachplan(Long id){
        Integer childrenNum = getChildrenNum(id);
        if (childrenNum>0){
            XueChengException.cast("当前节点下面还有内容，不允许删除！");
        }
        int i = teachplanMapper.deleteById(id);
        if (i<=0){
            XueChengException.cast("课程计划删除失败！");
        }
        Integer integer = removeTeachplanMedia(id);
        if (integer<0){
            XueChengException.cast("课程计划删除失败！");
        }
        return id;
    }

    /**
     * 删除课程计划媒体文件
     * @param id 课程计划id
     * @return
     */
    private Integer removeTeachplanMedia(Long id){
        LambdaQueryWrapper<TeachplanMedia> lqw = new LambdaQueryWrapper<>();
        lqw.eq(TeachplanMedia::getTeachplanId,id);
        int delete = teachplanMediaMapper.delete(lqw);
        return delete;
    }



    /**
     * 获取当前节点下的子节点数量
     * @param id 课程计划id
     * @return
     */
    private Integer getChildrenNum(Long id){
        Integer result=0;
        Teachplan teachplan = teachplanMapper.selectById(id);
        if (teachplan.getGrade()==1){
            LambdaQueryWrapper<Teachplan> lqw = new LambdaQueryWrapper<>();
            lqw.eq(Teachplan::getCourseId,teachplan.getCourseId())
                    .eq(Teachplan::getParentid,teachplan.getId());
            result = teachplanMapper.selectCount(lqw);
        }
        return result;
    }

    /**
     * 查询课程计划排序序号最大值或者最小值
     * @param id 课程id
     * @param parentId 父亲课程计划id
     * @param maxOrMin 获取最大排序序号或者最小排序序号
     * @return
     */
    private Integer getTeachplanyOrderby(Long id,Long parentId,String maxOrMin){
        String sort="";
        if ("max".equals(maxOrMin)){
            sort="DESC";
        }else {
            sort="ASC";
        }
        if (parentId==null){
            parentId=0L;
        }
        Integer teachplanyOrderby=0;
        Integer rs = teachplanMapper.selectTeachplanyOrderby(id, parentId,sort);
        if (rs!=null){
            teachplanyOrderby=rs;
        }
        return teachplanyOrderby;
    }
    /**
     * 修改课程计划
     * @param handleTeachplanDTO 课程计划传输模型
     * @return
     */
    @Override
    public HandleTeachplanDTO editTeachplany(HandleTeachplanDTO handleTeachplanDTO) {
        Teachplan teachplan = new Teachplan();
        BeanUtils.copyProperties(handleTeachplanDTO,teachplan);
        int insert = teachplanMapper.updateById(teachplan);
        if (insert<=0){
            XueChengException.cast("课程计划修改失败！请重试。。");
        }
        return handleTeachplanDTO;
    }
    /**
     * 处理课程计划向下移动
     * @param handle
     * @param id
     * @return
     */
    public Object handleTeachplanMoveOderby(String handle,Long id){
        Teachplan teachplan = teachplanMapper.selectById(id);
        //获得当前计划的最大的排序序号和最小排序序号
        Integer maxOrderby = getTeachplanyOrderby(teachplan.getCourseId(), teachplan.getParentid(),"max");
        Integer minOrderby = getTeachplanyOrderby(teachplan.getCourseId(), teachplan.getParentid(), "min");
        if ("movedown".equals(handle)){
            //下移
            if (Objects.equals(teachplan.getOrderby(), maxOrderby)){
                XueChengException.cast("已经到底部了，不能在下移了！");
            }
            Integer ob=teachplan.getOrderby();
            Integer rs1 = teachplanMapper.updateTeachplanyOrderby(teachplan.getCourseId(),teachplan.getParentid(), ob + 1, ob);
            teachplan.setOrderby(ob+1);
            int rs2 = teachplanMapper.updateById(teachplan);
            if (rs1>0&&rs2>0){
                System.out.println("顺序更新成功！");
            }else {
                XueChengException.cast("下移失败，请重试！");
            }
        }
        if ("moveup".equals(handle)){
            //上移
            if (Objects.equals(teachplan.getOrderby(), minOrderby)){
                XueChengException.cast("已经到顶了，不能在上移了！");
            }
            Integer ob=teachplan.getOrderby();
            Integer rs1 = teachplanMapper.updateTeachplanyOrderby(teachplan.getCourseId(),teachplan.getParentid(), ob - 1, ob);
            teachplan.setOrderby(ob-1);
            int rs2 = teachplanMapper.updateById(teachplan);
            if (rs1>0&&rs2>0){
                System.out.println("顺序更新成功！");
            }else {
                XueChengException.cast("上移失败，请重试！");
            }

        }
        return teachplan;
    }

}
