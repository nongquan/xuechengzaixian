package com.xuecheng.learning.config;

import com.xuecheng.framework.domain.course.response.TeachplanMediaPub;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "xc-search-service")
public interface FeignConfig {

    @GetMapping("/search/course/getmedia/{teachplanId}")
    public TeachplanMediaPub seachTeachplanMediaPub(@PathVariable("teachplanId") String teachplanId);
}
