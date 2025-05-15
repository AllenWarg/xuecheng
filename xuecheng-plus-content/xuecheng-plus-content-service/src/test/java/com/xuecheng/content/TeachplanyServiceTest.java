package com.xuecheng.content;

import com.xuecheng.content.model.dto.TeachplanTreeDTO;
import com.xuecheng.content.service.TeachplanyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class TeachplanyServiceTest {
    @Autowired
    TeachplanyService teachplanyService;

    @Test
    void testQueryTeachplanyTree(){
        List<TeachplanTreeDTO> teachplanTreeDTO = teachplanyService.queryTeachplanyTree(117L);
        System.out.println(teachplanTreeDTO);

    }

}
