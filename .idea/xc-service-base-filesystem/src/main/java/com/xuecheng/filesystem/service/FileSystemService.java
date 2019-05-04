package com.xuecheng.filesystem.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.domain.ucenter.response.UcenterCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResultCode;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FileSystemService {

    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
    private int connect_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
    private int network_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.charset}")
    private String charset;
    @Value("${xuecheng.fastdfs.tracker_servers}")
    private String tracker_servers;

    @Autowired
    FileSystemRepository fileSystemRepository;

    public UploadFileResult uploadFile(MultipartFile multipartFile, String businesskey, String filetag, String metadata){
       if (multipartFile==null){
           ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
       }
        //加载配置文件
        this.addConnection();
       try {
            //定义TrackerClient
            TrackerClient trackerClient = new TrackerClient();
            //连接tracker
            TrackerServer trackerServer = trackerClient.getConnection();
            //获得Storage
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //创建storageClient
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storeStorage);
            //上传文件
           byte[] bytes = multipartFile.getBytes();
           //获得文件名
           String originalFilename = multipartFile.getOriginalFilename();
           //获得文件格式
           String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
           String fileId = storageClient1.upload_appender_file1(bytes, ext, null);
           if (StringUtils.isBlank(fileId)){
               ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
           }

           //往数据库保存数据
           FileSystem fileSystem = new FileSystem();
           fileSystem.setFilePath(fileId);
           fileSystem.setFileId(fileId);
           fileSystem.setFileName(originalFilename);
           fileSystem.setFileType(multipartFile.getContentType());
           fileSystem.setBusinesskey(businesskey);
           fileSystem.setFiletag(filetag);
           if (StringUtils.isNotBlank(metadata)){
               Map map = JSON.parseObject(metadata, Map.class);
               fileSystem.setMetadata(map);
           }
           FileSystem save = fileSystemRepository.save(fileSystem);

           return new UploadFileResult(CommonCode.SUCCESS,save);
       } catch (Exception e) {
            e.printStackTrace();
        }

       return null;
    }
    //加载配置文件
    public void  addConnection(){
        try {
            ClientGlobal.initByTrackers(tracker_servers);
            ClientGlobal.setG_charset(charset);
            ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
            ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
        }
    }
}
