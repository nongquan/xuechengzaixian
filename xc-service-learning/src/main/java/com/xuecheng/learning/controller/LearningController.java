package com.xuecheng.learning.controller;

import com.xuecheng.api.learning.LearningControllerApi;
import com.xuecheng.framework.domain.learning.ext.MediaPubResult;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.learning.service.LearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/learning/course")
public class LearningController implements LearningControllerApi {
    @Autowired
    LearningService learningService;

    //调用搜索服务，拿取media__url

    @GetMapping("/getmedia/{courseId}/{teachplanId}")
    public MediaPubResult getUrl(@PathVariable("courseId") String courseId,@PathVariable("teachplanId") String teachplanId) {
        return learningService.getUrl(teachplanId);
    }

    //拿取用户信息
    @Override
    @GetMapping("/ucenter/getuserext")
    public XcUserExt getUser(@RequestParam("username") String username) {

        return learningService.getUser( username);
    }
}
