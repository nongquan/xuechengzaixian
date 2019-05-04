package com.xuecheng.test.freemarker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RequestMapping("/freemaker")
@Controller
public class FreemarkerController {
    @Autowired
    RestTemplate restTemplate;

    @RequestMapping("/test1")
    public String test(Map<String,Object> map){
        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31200/course//courseview/297e7c7c62b888f00162b8a7dec20000", Map.class);
        Map body = forEntity.getBody();
        body.get("courseBase");
        map.put("courseBase",body.get("courseBase"));
        map.put("courseMarket",body.get("courseMarket"));
        map.put("coursePic",body.get("coursePic"));
        map.put("teachplanNode",body.get("teachplanNode"));
        return "course";
    }
}
