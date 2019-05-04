package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.*;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.TeachplanMediaPub;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.config.CourseClient;
import com.xuecheng.manage_course.dao.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CourseService {

    @Autowired
    TeachPlanNodeMapper teachPlanNodeMapper;
    @Autowired
    TeachplanRepository teachplanRepository;
    @Autowired
    CategoryNodeMapper categoryNodeMapper;
    @Autowired
    CourseMapper courseMapper;
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    CoursepicRepository coursepicRepository;
    @Autowired
    CoursepicMapper coursepicMapper;
    @Autowired
    CourseMarketRepository courseMarketRepository;
    @Autowired
    CourseClient courseClient;
    @Autowired
    SysDictionaryRepository sysDictionaryRepository;
    @Autowired
    CoursePubRepository coursePubRepository;
    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;
    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;

    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;


    //课程计划
    public TeachplanNode findNodeById(String courseId){
        TeachplanNode teachplanNode = teachPlanNodeMapper.findNodeById(courseId);
/*        String s = JSON.toJSONString(teachplanNode);
        Optional<CoursePub> byId = coursePubRepository.findById(courseId);
        CoursePub coursePub = byId.get();
        coursePub.setTeachplan(s);
        coursePubRepository.save(coursePub);*/
        return teachplanNode;
    }
    //添加课程计划
    @Transactional
    public ResponseResult addTecachplan(Teachplan teachplan) {
        if (teachplan==null|| StringUtils.isBlank(teachplan.getPname())||
                StringUtils.isBlank(teachplan.getStatus())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        if (teachplan.getParentid()==null){
            Optional<CourseBase> byId = courseBaseRepository.findById(teachplan.getCourseid());
            if (!byId.isPresent()){
                ExceptionCast.cast(CommonCode.INVALID_PARAM);
            }
            CourseBase courseBase = byId.get();
            Teachplan byPname = teachplanRepository.findByPname(courseBase.getName());

            teachplan.setParentid(byPname.getId());
            teachplan.setGrade("2");
            teachplanRepository.save(teachplan);
        }else {
            teachplan.setGrade("3");
            teachplanRepository.save(teachplan);
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }
    ////课程分页查询
    public QueryResponseResult findCourseBase(int page, int size,String companyId, CourseListRequest courseListRequest){
        if(page<=0){
            page=1;
        }
        if (size<=0){
            size=10;
        }
        if (courseListRequest==null){
            courseListRequest = new CourseListRequest();
        }
        courseListRequest.setCompanyId(companyId);
        PageHelper.startPage(page,size);
        Page<CourseInfo> all = courseMapper.findAll(courseListRequest);
        List<CourseInfo> result = all.getResult();
        long total = all.getTotal();

        QueryResult<CourseInfo>  queryResult = new QueryResult<>();
        queryResult.setTotal(total);
        queryResult.setList(result);

        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }
    //课程分类
    public CategoryNode findCategoryNode(){

        return categoryNodeMapper.findCategoryNode();
    }
    //新增课程
    public ResponseResult saveCourseBase(CourseBase courseBase){
        if (courseBase==null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        CourseBase save = courseBaseRepository.save(courseBase);
        return new ResponseResult(CommonCode.SUCCESS);
    }
    //新增课程图片
    public  ResponseResult savePicture(String courseId, String pic){
        CoursePic coursePic = new CoursePic();
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);

        Optional<CoursePic> byId = coursepicRepository.findById(courseId);
        if (byId.isPresent()){
            coursepicMapper.updateCoursePic(pic,courseId);
        }else {
            CoursePic coursePic1 = coursepicRepository.save(coursePic);
        }

        return  new ResponseResult(CommonCode.SUCCESS);
    }
    //回显课程图片
    public CoursePic findPicture(String courseid){
        Optional<CoursePic> byId = coursepicRepository.findById(courseid);
        if (byId.isPresent()){
            CoursePic coursePic = byId.get();
            return  coursePic ;
        }
        return  null;
    }
    //删除课程图片
    public ResponseResult deletePicture(String courseId){
        coursepicRepository.deleteById(courseId);
        Optional<CoursePic> byId = coursepicRepository.findById(courseId);
        if(byId.isPresent()){
           ExceptionCast.cast(CommonCode.FAIL);
        }

        return  new ResponseResult(CommonCode.SUCCESS);
    }
    //页面模板静态化数据
    public CourseView getCourseViewById(String courseid) {
        CourseView courseView = new CourseView();

        Optional<CourseBase> byId = courseBaseRepository.findById(courseid);
        if (byId.isPresent()){
            courseView.setCourseBase(byId.get());
        }
        Optional<CourseMarket> byId1 = courseMarketRepository.findById(courseid);
        if(byId1.isPresent()){
            courseView.setCourseMarket(byId1.get());
        }
        TeachplanNode nodeById = teachPlanNodeMapper.findNodeById(courseid);
        if (nodeById!=null){
            courseView.setTeachplanNode(nodeById);
        }
        Optional<CoursePic> byId2 = coursepicRepository.findById(courseid);
        if(byId2.isPresent()){
            courseView.setCoursePic(byId2.get());
        }

        return courseView;
    }
    //拿取URL
    public CoursePublishResult findUrlByCourseid(String courseid) {
        CourseBase bycourseid = this.findBycourseid(courseid);
        if (bycourseid==null){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);//课程预览站点
        //模板
        cmsPage.setTemplateId(publish_templateId);
        //页面名称
        cmsPage.setPageName(courseid+".html");
        //页面别名
        cmsPage.setPageAliase(bycourseid.getName());
        //页面访问路径
        cmsPage.setPageWebPath(publish_page_webpath);
        //页面存储路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        //数据url
        cmsPage.setDataUrl(publish_dataUrlPre+courseid);

        CmsPageResult cmsPageResult = courseClient.save(cmsPage);
        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        if ( cmsPage1==null){
            ExceptionCast.cast(CommonCode.FAIL);
        }
       return new CoursePublishResult(CommonCode.SUCCESS,previewUrl+cmsPage1.getPageId());

    }
    public CourseBase findBycourseid(String courseid){
        if (StringUtils.isBlank(courseid)){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        Optional<CourseBase> byId = courseBaseRepository.findById(courseid);
        if (byId.isPresent()){
            CourseBase courseBase = byId.get();
            return courseBase;
        }
        return null;
    }
    //课程等级，学习模式
    public SysDictionary findDictionaryByType(String type){

        return sysDictionaryRepository.findByDType(type);
    }

    //一键课程发布
    public CoursePublishResult publish(String courseid) {
        CourseBase bycourseid = this.findBycourseid(courseid);
        if (bycourseid==null){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);//课程预览站点
        //模板
        cmsPage.setTemplateId(publish_templateId);
        //页面名称
        cmsPage.setPageName(courseid+".html");
        //页面别名
        cmsPage.setPageAliase(bycourseid.getName());
        //页面访问路径
        cmsPage.setPageWebPath(publish_page_webpath);
        //页面存储路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        //数据url
        cmsPage.setDataUrl(publish_dataUrlPre+courseid);

        //保存Coursepub到索引库
        CoursePub coursePubById = this.getCoursePubById(courseid);
        CoursePub coursePub = this.saveCoursePub(courseid, coursePubById);
        coursePubRepository.save(coursePub);

        String publish = courseClient.publish(cmsPage);
        if (StringUtils.isBlank(publish)){
            return null;
        }
        this.updateTeachplanMediaPub(courseid);
        return new CoursePublishResult(CommonCode.SUCCESS,publish);
    }
    //封装CoursePub
    public CoursePub saveCoursePub(String courseid,CoursePub coursePub){

        coursePub.setTimestamp(new Date());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(new Date());
        coursePub.setPubTime(format);
        coursePub.setId(courseid);

        Optional<CoursePub> byId = coursePubRepository.findById(courseid);
        if (byId.isPresent()){
            CoursePub coursePub2 = byId.get();
            BeanUtils.copyProperties(coursePub,coursePub2);
            coursePub2.setId(courseid);
            return coursePub2;
        }
        return  coursePub;
    }
    //查询CoursePub
    public CoursePub getCoursePubById(String courseid){
        CoursePub coursePub = new CoursePub();
        //查询CourseBase
        Optional<CourseBase> byId = courseBaseRepository.findById(courseid);
        if (byId.isPresent()){
            BeanUtils.copyProperties(byId.get(),coursePub);
        }
        //查询CourseMarket
        Optional<CourseMarket> byId1 = courseMarketRepository.findById(courseid);
        if (byId1.isPresent()){
            BeanUtils.copyProperties(byId1.get(),coursePub);
        }
        //查询CourseMarket
        Optional<CoursePic> byId2 = coursepicRepository.findById(courseid);
        if (byId2.isPresent()){
            BeanUtils.copyProperties(byId2.get(),coursePub);
        }
        //查询TeachplanNode
        TeachplanNode nodeById = teachPlanNodeMapper.findNodeById(courseid);
        if (nodeById!=null){
            String s = JSON.toJSONString(nodeById);
            coursePub.setTeachplan(s);
        }
        return coursePub;
    }

    //给课程绑定视频
    public ResponseResult saveMedia(TeachplanMedia teachplanMedia) {
        if (StringUtils.isBlank(teachplanMedia.getTeachplanId())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        Optional<Teachplan> byId = teachplanRepository.findById(teachplanMedia.getTeachplanId());
        if (!byId.isPresent()){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        String grade = byId.get().getGrade();
        if (!grade.equals("3")){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //有着更新，无着增加
        Optional<TeachplanMedia> byId1 = teachplanMediaRepository.findById(teachplanMedia.getMediaId());
        TeachplanMedia save = null;
        //更新
        if (byId1.isPresent()){
            TeachplanMedia teachplanMedia1 = byId1.get();
            teachplanMedia1.setCourseId(teachplanMedia.getCourseId());
            teachplanMedia1.setTeachplanId(teachplanMedia.getTeachplanId());
            teachplanMedia1.setMediaId(teachplanMedia.getMediaId());
            teachplanMedia1.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
            teachplanMedia1.setMediaUrl(teachplanMedia.getMediaUrl());
            save = teachplanMediaRepository.save(teachplanMedia1);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        //增加
        save = teachplanMediaRepository.save(teachplanMedia);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //更数据库新课程媒资索引表
    public void updateTeachplanMediaPub(String courseid) {
        List<TeachplanMediaPub> byCourseId = teachplanMediaPubRepository.findByCourseId(courseid);
        if (byCourseId!=null&&byCourseId.size()>0){
            teachplanMediaPubRepository.deleteByCourseId(courseid);
        }
        List<TeachplanMedia> byCourseId1 = teachplanMediaRepository.findByCourseId(courseid);
        if (byCourseId1==null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        for (TeachplanMedia teachplanMedia :byCourseId1) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            teachplanMediaPub.setCourseId(teachplanMedia.getCourseId());
            teachplanMediaPub.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
            teachplanMediaPub.setMediaId(teachplanMedia.getMediaId());
            teachplanMediaPub.setMediaUrl(teachplanMedia.getMediaUrl());
            teachplanMediaPub.setTeachplanId(teachplanMedia.getTeachplanId());
           /* SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd hh:mm:ss");
            String format1 = format.format(new Date());*/
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubRepository.save(teachplanMediaPub);
        }

    }
}
