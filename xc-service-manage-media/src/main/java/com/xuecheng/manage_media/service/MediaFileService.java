package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MediaFileService {
    @Autowired
    private MediaFileRepository mediaFileRepository;

    //我的媒资列表
    public QueryResponseResult findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {
       //处理参数
        if (queryMediaFileRequest==null){
            queryMediaFileRequest = new QueryMediaFileRequest();
        }
        if (page<=0){
            page=1;
        }
        page = page -1;
        if (size<=0){
            size=10;
        }
        //ProcessStatus 状态是精确查找
        MediaFile mediaFile = new MediaFile();
        if (StringUtils.isNotBlank(queryMediaFileRequest.getProcessStatus())){
            mediaFile.setProcessStatus(queryMediaFileRequest.getProcessStatus());
        }
        if (StringUtils.isNotBlank(queryMediaFileRequest.getFileOriginalName())){
            mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
        }
        if (StringUtils.isNotBlank(queryMediaFileRequest.getTag())){
            mediaFile.setTag(queryMediaFileRequest.getTag());
        }
        //fileOriginalName，tag 模糊查询 不设置就是精确查询
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withMatcher("fileOriginalName", ExampleMatcher.GenericPropertyMatchers.contains())
                                        .withMatcher("tag",ExampleMatcher.GenericPropertyMatchers.contains());
        Example<MediaFile> ex = Example.of(mediaFile, exampleMatcher);
        //设置分页参数 Pageable 和PageRequest 都是springdata 的对象
        Pageable of = PageRequest.of(page, size);
        Page<MediaFile> all = mediaFileRepository.findAll(ex, of);
        long totalPages = all.getTotalElements();
        List<MediaFile> content = all.getContent();

        QueryResult queryResult  = new QueryResult();
        queryResult.setTotal(totalPages);
        queryResult.setList(content);

        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }
}
