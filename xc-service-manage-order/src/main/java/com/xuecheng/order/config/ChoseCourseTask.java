package com.xuecheng.order.config;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.service.ChoseCourseService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Component
public class ChoseCourseTask {

    @Autowired
    ChoseCourseService choseCourseService;
    @Autowired
    RabbitTemplate rabbitTemplate;


    @Scheduled(cron = "0/3 * * * * * ")
    public void task(){
        //取出当前时间1分钟之前的时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(GregorianCalendar.MINUTE,-1);
        Date time = calendar.getTime();
        //取到需要发送的课程信息
        List<XcTask> list = choseCourseService.findNeedSendXcTask(time);
        if (list==null&&list.size()<=0){
            return;
        }
        this.sendXcTask(list);
    }
    //发送消息
    public void sendXcTask(List<XcTask> list){
        for (XcTask xcTask:list) {
            if (choseCourseService.updateTaskVersion(xcTask.getId(),xcTask.getVersion())>0){
                String xcTaskJson = JSON.toJSONString(xcTask);
                rabbitTemplate.convertAndSend(xcTask.getMqExchange(),xcTask.getMqRoutingkey(),xcTaskJson);
                //发送成功后更新时间
                choseCourseService.updateTime(xcTask.getId(), new Date());
            }
        }
    }

}
