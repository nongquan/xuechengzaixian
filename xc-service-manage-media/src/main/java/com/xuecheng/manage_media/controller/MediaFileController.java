package com.xuecheng.manage_media.controller;

import com.xuecheng.api.media.MediaFileControllerApi;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.manage_media.service.MediaFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/media")
public class MediaFileController implements MediaFileControllerApi {
    @Autowired
    private MediaFileService mediaFileService;

    //我的媒资列表
    @Override
    @GetMapping("/file/list/{page}/{size}")
    public QueryResponseResult findList(@PathVariable("page") int page,@PathVariable("size") int size,QueryMediaFileRequest queryMediaFileRequest) {
        return mediaFileService.findList( page,size, queryMediaFileRequest);
    }
}
