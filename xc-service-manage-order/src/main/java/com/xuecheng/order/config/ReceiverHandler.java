package com.xuecheng.order.config;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.order.service.ChoseCourseService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReceiverHandler {
    @Autowired
    ChoseCourseService choseCourseService;

    @RabbitListener(queues = {RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE})
    public void  Received(String msg){
        XcTask xcTask = JSON.parseObject(msg, XcTask.class);
        //删除订单任务表 添加历史记录表
        ResponseResult responseResult = choseCourseService.deleteTask(xcTask);

    }
}
