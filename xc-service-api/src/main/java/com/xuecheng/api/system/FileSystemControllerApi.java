package com.xuecheng.api.system;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import org.springframework.web.multipart.MultipartFile;

public interface FileSystemControllerApi {
    //上传文件
    public UploadFileResult uploadFile(MultipartFile multipartFile,
                                       String businesskey,
                                       String filetag,
                                       String metadata);
}
