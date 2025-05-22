package com.xuecheng.content.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.*;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.TeachplanyService;
import com.xuecheng.execption.CommonError;
import com.xuecheng.execption.XueChengException;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Author gc
 * @Description 课程发布相关实现
 * @DateTime: 2025/5/21 0:51
 **/
@Service
@Slf4j
public class CoursePublishServiceImpl implements CoursePublishService {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;
    @Autowired
    TeachplanyService teachplanyService;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseTeacherService courseTeacherService;
    //课程审核
    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    CourseCategoryMapper courseCategoryMapper;
    //课程发布
    @Autowired
    CoursePublishMapper coursePublishMapper;
    @Autowired
    MqMessageService mqMessageService;
    @Autowired
    MediaServiceClient mediaServiceClient;
    @Autowired
    SearchServiceClient searchServiceClient;

    /**
     * @description 获取课程预览信息
     * @param courseId 课程id
     * @return
     * @author Mr.M
     * @date 2022/9/16 15:36
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId){
        //课程基本信息,课程营销信息
        CourseBaseInfoDTO courseBase =new CourseBaseInfoDTO();
        AddCourseBaseDto addCourseBaseDto = courseBaseInfoService.queryCourseBaseById(courseId);
        BeanUtils.copyProperties(addCourseBaseDto,courseBase);
        //课程计划信息
        List<TeachplanTreeDTO> teachplans=teachplanyService.queryTeachplanyTree(courseId);
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBase);
        coursePreviewDto.setTeachplans(teachplans);
        return coursePreviewDto;
    }

    /**
     * 提交课程审核
     * @param courseId 课程id
     */
    @Transactional
    public void commitAudit(Long companyId, Long courseId){
        //课程基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (!companyId.equals(courseBase.getCompanyId())){
            XueChengException.cast("本机构只允许提交本机构的课程");
            return;
        }
        if (StringUtils.isEmpty(courseBase.getPic())){
            XueChengException.cast("没有上传课程图片不允许提交审核");
            return;
        }
        if ("202003".equals(courseBase.getAuditStatus())){
            XueChengException.cast("已提交审核,只有审核完才能再次提交审核");
            return;
        }
        String mtName = courseCategoryMapper.selectById(courseBase.getMt()).getName();
        String stName = courseCategoryMapper.selectById(courseBase.getSt()).getName();

        //课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //课程计划表
        List<TeachplanTreeDTO> teachplanTreeDTOS = teachplanyService.queryTeachplanyTree(courseId);
        if (teachplanTreeDTOS==null||teachplanTreeDTOS.size()<=0){
            XueChengException.cast("请先添加课程计划后，再提交审核。");
            return;
        }
        //师资信息
        List<CourseTeacher> courseTeachers = courseTeacherService.queryCourseTeacherList(courseId);
        //构建课程预发布对象
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBase,coursePublishPre);
        BeanUtils.copyProperties(courseMarket,coursePublishPre);
        ObjectMapper objectMapper = new ObjectMapper();
        //将对象转json
        String marketJson;
        String teachplanTreeJson;
        String courseTeachersJson;
        try {
            marketJson = objectMapper.writeValueAsString(courseMarket);
            teachplanTreeJson = objectMapper.writeValueAsString(teachplanTreeDTOS);
            courseTeachersJson = objectMapper.writeValueAsString(courseTeachers);
        } catch (JsonProcessingException e) {
            log.error("课程提交审核失败，失败原因：{}",e.getMessage());
            e.printStackTrace();
            return;
        }
        coursePublishPre.setId(courseBase.getId());
        coursePublishPre.setMtName(mtName);
        coursePublishPre.setStName(stName);
        coursePublishPre.setMarket(marketJson);
        coursePublishPre.setTeachplan(teachplanTreeJson);
        coursePublishPre.setTeachers(courseTeachersJson);
        coursePublishPre.setStatus("202003");
        CoursePublishPre cPP = coursePublishPreMapper.selectById(courseId);

        if(cPP==null){
            int insert = coursePublishPreMapper.insert(coursePublishPre);
            if (insert<=0){
                XueChengException.cast("课程审核提交失败！");
                return;
            }
        }else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        //更新审核状态为已提交
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }


    /**
     * 课程发布
     * @param companyId 机构id
     * @param courseId 课程id
     */
    @Transactional
    public void coursepublish(Long companyId, Long courseId){

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre==null){
            XueChengException.cast("课程已经发布了，不能重复发布！");
        }
        if (!companyId.equals(courseBase.getCompanyId())){
            XueChengException.cast("发布失败，本机构只允许发布本机构的课程");
        }
        if (!"202004".equals(coursePublishPre.getStatus())){
            XueChengException.cast("发布失败，只有课程审核通过后才能发布");
        }

        //获取发布表
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);

        if (Objects.isNull(coursePublish)){
            coursePublish = new CoursePublish();
            BeanUtils.copyProperties(coursePublishPre,coursePublish);
            coursePublish.setStatus("203002");//已发布状态
            coursePublishMapper.insert(coursePublish);
        }else {
            BeanUtils.copyProperties(coursePublishPre,coursePublish);
            coursePublish.setStatus("203002");//已发布状态
            coursePublishMapper.updateById(coursePublish);
        }

        //向消息表中写入插入需要同步的信息
        //MqMessage mqMessage = new MqMessage();
        //mqMessage.setMessageType("course_publish");
        //mqMessage.setStageState1("0");
        //mqMessage.setStageState2("0");
        //mqMessage.setStageState3("0");
        //mqMessage.setStageState4("0");
        //mqMessageMapper.insert(mqMessage);
        MqMessage mqMessage= mqMessageService.addMessage("course_publish"
                , String.valueOf(courseId)
                , null, null);

        if (mqMessage==null){
            XueChengException.cast(CommonError.UNKNOWN_ERROR);
        }
        //更新课程基本表发布状态为已发布
        //courseBase.setStatus("203002");//课程已发布状态
        courseBaseMapper.updateById(courseBase);

        //删除课程审核表（预发布表）
        coursePublishPreMapper.deleteById(courseId);

    }

    /**
     * 通过模板生成静态html
     * @param courseId 课程id
     * @return
     */
    public File GenerateHTMLByTemplate(Long courseId){
        File htmlFile=null;
        CoursePreviewDto coursePreviewInfo = getCoursePreviewInfo(courseId);
        if (coursePreviewInfo==null){
            return null;
        }
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
        String classPath = this.getClass().getResource("/").getPath();
        Writer out=null;
        try {
            cfg.setDirectoryForTemplateLoading(new File(classPath+"/templates/"));
            cfg.setDefaultEncoding("UTF-8");
            Map<String,Object> root = new HashMap<>();
            root.put("model",coursePreviewInfo);
            Template template= cfg.getTemplate("course_template.ftl");
            htmlFile = File.createTempFile("tempHtmlFile",".html");
            out = new FileWriter(htmlFile);
            template.process(root,out);
        } catch (Exception e) {
            log.error("生成静态html失败，课程id：{}",courseId,e.getMessage());
            e.printStackTrace();
            return null;
        }finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return htmlFile;
    }


    /**
     * 远程调用媒资服务上传文件
     * @param courseId 课程id
     * @param objectPath 对象存储路径
     * @return
     */
    public Boolean uploadFileToMinio(Long courseId,File file,String objectPath){
        if (file==null){
            return null;
        }
        try {
            MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
            String res = mediaServiceClient.uploadFile(multipartFile, objectPath);
            if (res==null){
                return null;
            }
        }catch (Exception e){
            log.error("文件上传失败，课程id:{}",courseId,e);
            e.printStackTrace();
            return null;
        }
        return true;
    }


    /**
     * 添加课程索引
     * @param courseIndex 课程索引传输模型
     * @return
     */
    public Boolean addCourseIndex(CourseIndex courseIndex){
        Boolean add = searchServiceClient.add(courseIndex);
        return add;
    }

}
