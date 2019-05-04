package com.xuecheng.learning.config;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.learning.Request_body;
import com.xuecheng.framework.domain.learning.XcLearningCourse;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.service.LearningService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ReceiverHandler {
    @Autowired
    LearningService learningService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    //添加用户课程
    @RabbitListener(queues = {RabbitMQConfig.XC_LEARNING_ADDCHOOSECOURSE})
    public void  Received(String msg){
        XcTask xcTask = JSON.parseObject(msg, XcTask.class);
        String requestBody = xcTask.getRequestBody();
        Request_body request_body = JSON.parseObject(requestBody, Request_body.class);
        String userId = request_body.getUserId();
        String courseId = request_body.getCourseId();
        ResponseResult responseResult = learningService.saveCourse(xcTask,userId,courseId);
        if (responseResult.isSuccess()){
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_LEARNING_ADDCHOOSECOURSE,RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE_KEY,msg);
        }

    }
}
