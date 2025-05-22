package com.xuecheng.content.viewAndModel;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @Author gc
 * @Description 模板视图
 * @DateTime: 2025/5/20 22:12
 **/
@Controller
public class FreemarkerController {
    @GetMapping("/test")
    public ModelAndView test(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name","小明");
        modelAndView.setViewName("test");
        return modelAndView;
    }
}
