package com.xuecheng.media;

import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaProcessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @Author 作者 TODO
 * @Description 描述 TODO
 * @DateTime: 2025/5/18 23:30
 **/
@SpringBootTest
public class MediaProcessTest {
    @Autowired
    MediaProcessService processService;
    @Test
    void testGetMediaProcessListByShardIndex(){
        List<MediaProcess> mediaProcessListByShardIndex = processService.getMediaProcessListByShardIndex(0, 2, 3);
        System.out.println(mediaProcessListByShardIndex);
    }

}
