package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author gc
 * @Description 课程分类服务实现类
 * @DateTime: 2025/5/13 18:09
 **/
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    CourseCategoryMapper courseCategoryMapper;
    @Override
    public List<CourseCategoryTreeDto> queryCourseCategoryTree(String id) {
        List<CourseCategoryTreeDto> list = courseCategoryMapper.selectCourseCategoryTree(id);
        //处理后的最终结果
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = new ArrayList<>();
        //将集合转换为map,方便找父节点
        Map<String, CourseCategoryTreeDto> map = list.stream()
                .filter(item -> !item.getId().equals(id) )
                .collect(Collectors.toMap(key -> key.getId(), value -> value, (k1, k2) -> k2));
       //处理list集合，将list转换为树结构
        list.stream()
                .filter(item -> !item.getId().equals(id))
                .forEach(item->{
                    if (item.getParentid().equals(id)){
                        courseCategoryTreeDtos.add(item);
                    }
                    //获取当前节点的父节点
                    CourseCategoryTreeDto parentNode = map.get(item.getParentid());
                    if (parentNode!=null){
                        if (parentNode.getChildrenTreeNodes()==null){
                            parentNode.setChildrenTreeNodes(new ArrayList<>());
                        }
                        parentNode.getChildrenTreeNodes().add(item);
                    }
                });
        return courseCategoryTreeDtos;
    }
}
