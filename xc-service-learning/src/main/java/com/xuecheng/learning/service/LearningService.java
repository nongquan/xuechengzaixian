package com.xuecheng.learning.service;

import com.xuecheng.framework.domain.course.response.TeachplanMediaPub;
import com.xuecheng.framework.domain.learning.XcLearningCourse;
import com.xuecheng.framework.domain.learning.ext.MediaPubResult;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.config.FeignConfig;

import com.xuecheng.learning.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class LearningService {
    @Autowired
    FeignConfig feignConfig;
   @Autowired
   XcCompanyUserRepository xcCompanyUserRepository;
   @Autowired
    XcUserRepository xcUserRepository;
    @Autowired
    XcMenuMapper xcMenuMapper;
    @Autowired
    XcLearningCourseRepository xcLearningCourseRepository;
    @Autowired
    XcTaskHisRepository xcTaskHisRepository;

    //调用搜索服务，拿取media__url
    public  MediaPubResult getUrl(String teachplanId) {
        TeachplanMediaPub teachplanMediaPub = feignConfig.seachTeachplanMediaPub(teachplanId);
        if (teachplanMediaPub==null||teachplanMediaPub.getMediaUrl()==null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        String mediaUrl = teachplanMediaPub.getMediaUrl();
        return  new MediaPubResult(CommonCode.SUCCESS,mediaUrl);
    }
    //拿取用户信息
    public XcUserExt getUser(String username) {
            if (StringUtils.isBlank(username)){
                ExceptionCast.cast(CommonCode.INVALID_PARAM);
            }
        XcUser byUsername = xcUserRepository.findByUsername(username);
       /* String id1 = byUsername.getId();
        //查询权限
        List<XcMenu> byUserId1 = menuMapper.findByUserId(id1);*/
            List<XcMenu> xcMenuList = xcMenuMapper.selectPermissionByUserId(byUsername.getId());
        XcUserExt xcUserExt = new   XcUserExt();
        if (byUsername!=null&&byUsername.getId()!=null){
            String id = byUsername.getId();
            XcCompanyUser byUserId = xcCompanyUserRepository.findByUserId(id);
            String companyId = byUserId.getCompanyId();
            BeanUtils.copyProperties(byUsername,xcUserExt);
            if (StringUtils.isBlank(companyId)){
                xcUserExt.setCompanyId(companyId);
            }
           /* xcUserExt.setPermissions(byUserId1);*/
            xcUserExt.setPermissions(xcMenuList);
            return  xcUserExt;
        }
        return  null;
    }
    //查找用户课程是否存在

    public XcLearningCourse findLearningCourse(String userId, String courseId) {
        return xcLearningCourseRepository.findByUserIdAndCourseId(userId, courseId);

    }
    //查找历史记录是否存在
    public XcTaskHis findTaskHis(String id) {
        Optional<XcTaskHis> byId = xcTaskHisRepository.findById(id);
        if (byId.isPresent()){
            return byId.get();
        }
        return null;
    }

    @Transactional
    public ResponseResult saveCourse(XcTask xcTask, String userId, String courseId) {
        //保存或更新课程
        XcLearningCourse learningCourse = this.findLearningCourse(userId, courseId);
        if (learningCourse!=null){
            learningCourse.setStatus("501001");

        }else {
            learningCourse = new XcLearningCourse();
            learningCourse.setStatus("501001");
            learningCourse.setUserId(userId);
            learningCourse.setCourseId(courseId);
        }
        xcLearningCourseRepository.save(learningCourse);
        //保存或更新历史记录
        XcTaskHis taskHis = this.findTaskHis(xcTask.getId());
        if (taskHis!=null){
            taskHis.setUpdateTime(new Date());
        }else {
            taskHis =  new XcTaskHis();
           BeanUtils.copyProperties(xcTask,taskHis);
            taskHis.setUpdateTime(new Date());
        }
        XcTaskHis save = xcTaskHisRepository.save(taskHis);
        if (save==null){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }


}
