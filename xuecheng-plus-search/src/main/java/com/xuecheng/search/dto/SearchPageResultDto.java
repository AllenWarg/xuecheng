package com.xuecheng.search.dto;

import com.xuecheng.model.PageResult;
import lombok.Data;

import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description
 * @date 2022/9/25 17:51
 */

@Data
public class SearchPageResultDto<T> extends PageResult<T>{

    //大分类列表
    List<String> mtList;
    //小分类列表
    List<String> stList;
    public SearchPageResultDto(List<T> items, long counts, long page, long pageSize) {
        super(items, counts, page, pageSize);
    }

}
