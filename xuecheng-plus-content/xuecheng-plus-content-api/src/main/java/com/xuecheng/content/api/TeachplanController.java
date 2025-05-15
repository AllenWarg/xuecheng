package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.HandleTeachplanDTO;
import com.xuecheng.content.model.dto.TeachplanTreeDTO;
import com.xuecheng.content.service.TeachplanyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description 课程信息编辑接口
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
@Slf4j
@Api(value = "课程计划接口",tags = "课程计划接口")
@RestController
public class TeachplanController {
    @Autowired
    TeachplanyService teachplanyService;
    /**
     * 查询课程计划接口
     * @param id 课程id
     * @return
     */
    @ApiOperation("查询课程计划接口")
    @GetMapping("/teachplan/{id}/tree-nodes")
    public List<TeachplanTreeDTO> getTeachplanTreeDTO(@PathVariable("id") Long id){
        return teachplanyService.queryTeachplanyTree(id);
    }

    @ApiOperation("添加课程计划接口")
    @PostMapping("/teachplan")
    public HandleTeachplanDTO getTeachplanTreeDTO(@RequestBody HandleTeachplanDTO handleTeachplanDTO ){
        HandleTeachplanDTO rs = new HandleTeachplanDTO();
        if (handleTeachplanDTO.getId()!=null){
            rs=teachplanyService.editTeachplany(handleTeachplanDTO);
        }else {
            rs=teachplanyService.saveTeachplany(handleTeachplanDTO);
        }
        return rs;
    }


    @ApiOperation("课程计划上下移动接口")
    @PostMapping("/teachplan/{move}/{id}")
    public Object TeachplanMoveOderby(@PathVariable("move") String move,@PathVariable("id") Long id){
        return teachplanyService.handleTeachplanMoveOderby(move,id);
    }

    @ApiOperation("删除课程计划接口")
    @DeleteMapping("/teachplan/{id}")
    public Object removeTeachplan(@PathVariable("id") Long id){
        return teachplanyService.removeTeachplan(id);
    }
}
