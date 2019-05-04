package com.xuecheng.manage_cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.manage_cms_client.service.RabbitmqService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RabbitmqClient {

    @Value("${xuecheng.mq.queue}")
    private String queue;
    @Autowired
    private RabbitmqService rabbitmqService;

    @RabbitListener(queues = {"${xuecheng.mq.queue}"})
    public void query(String msg){
        Map map = JSON.parseObject(msg, Map.class);
        String pageId = (String) map.get("pageId");
        rabbitmqService.uploadHtml(pageId);
    }
}
