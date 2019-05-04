package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.MD5Util;
//import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class MediaUploadService {

    @Autowired
    MediaFileRepository mediaFileRepository;
    @Value("${xc-service-manage-media.upload-location}")
    private String upload_location;
    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    private String routingkey_media_video;
    @Value("${xc-service-manage-media.mq.queue-media-video-processor}")
    private String queue_media_video_processor;
    @Autowired
    private RabbitTemplate rabbitTemplate;


    /**
     * 文件上传前的注册，检查文件是否存在
     * 根据文件md5得到文件路径
     * 规则：
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符
     * 三级目录：md5
     * 文件名：md5+文件扩展名
     */
    //上传视频所在的文件夹位置
    public String getMkdirPath(String fileMd5){
       return upload_location + fileMd5.substring(0,1) +"/"+ fileMd5.substring(1,2)+"/"+fileMd5+"/";
    }
    //视频的精确位置
    public String getFilePath(String fileMd5,String fileExt){
        return upload_location + fileMd5.substring(0,1) +"/"+ fileMd5.substring(1,2)+"/"+fileMd5+"/"+fileMd5+"."+fileExt;
    }
    //分段视频所存在的文件夹位置
    public String getChunkPath(String fileMd5){
        return upload_location + fileMd5.substring(0,1) +"/"+ fileMd5.substring(1,2)+"/"+fileMd5+"/"+"chunk/";
    }

    //文件上传注册
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        String mkdirPath  = this.getMkdirPath(fileMd5);
        String filePath  = this.getFilePath(fileMd5,fileExt);
        String chunkPath = this.getChunkPath(fileMd5);
        //查看文件是否存在，如果存在则返回存在 抛出异常
        File file1 = new File(filePath);
        Optional<MediaFile> byId = mediaFileRepository.findById(fileMd5);
        if (file1.exists()&&byId.isPresent()){
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        //查看文件夹是否存在，不存在则创建
        File file = new File(mkdirPath);
        if (!file.exists()){
            file.mkdirs();
        }
        File file2 = new File(chunkPath);
        if (!file2.exists()){
            file2.mkdirs();
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //校验分块文件是否存在
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        String chunkPath  = this.getChunkPath(fileMd5)+chunk;
        File file1 = new File(chunkPath);
        if (file1.exists()){
            return  new CheckChunkResult(CommonCode.SUCCESS,true);
        }
        /*File[] files = file1.listFiles();
        int flag = 0;
        if (files.equals(null)){
            return new CheckChunkResult(MediaCode.MERGE_FILE_FAIL,false);
        }
        for(File file:files){
            if (file.getName().equals(chunk.toString())){
                flag=1;
            }
        }
        if (flag==0){
            return new CheckChunkResult(MediaCode.MERGE_FILE_FAIL,false);
        }*/
        return  new CheckChunkResult(CommonCode.SUCCESS,false);
    }
    //上传分块
    public ResponseResult uploadchunk(MultipartFile file, String fileMd5, Integer chunk) {
        String chunkPath = this.getChunkPath(fileMd5)+chunk;

        try {
            InputStream inputStream = file.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(new File(chunkPath));
            IOUtils.copy(inputStream,fileOutputStream);
            inputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }
    //合并分块
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        String filePath = this.getFilePath(fileMd5, fileExt);
        String chunkPath = this.getChunkPath(fileMd5);
        File file = new File(chunkPath);
        File[] files = file.listFiles();
        List<File> files1 = Arrays.asList(files);
        //对分段视频进行排序
        Collections.sort(files1, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (Integer.parseInt(o1.getName())>Integer.parseInt(o2.getName())){
                    return 1;
                }
                return -1;
            }
        });
        //进行合并
        try {
            FileOutputStream out = new FileOutputStream(new File(filePath));
            FileInputStream in = null;
            for (File file1:files1){
                in = new FileInputStream(file1);
                int len =-1;
                byte[] bytes = new byte[1024];
                while ((len = in.read(bytes))!=-1){
                    out.write(bytes,0,len);
                }
                in.close();
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //比较上传的视频和原本的视频是否一致
        File file1 = new File(filePath);
        MediaFile mediaFile = new MediaFile();
        try {
            String fileMD5String = MD5Util.getFileMD5String(file1);
            if (fileMd5.equals(fileMD5String)){
                mediaFile.setFileId(fileMd5);
                mediaFile.setFileOriginalName(fileName);
                mediaFile.setFileName(fileMd5 + "." +fileExt);
                //文件路径保存相对路径
                String filePath1 = fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/";
                mediaFile.setFilePath(filePath1);
                mediaFile.setFileSize(fileSize);
                mediaFile.setUploadTime(new Date());
                mediaFile.setMimeType(mimetype);
                mediaFile.setFileType(fileExt);
                //状态为上传成功
                mediaFile.setFileStatus("301002");
               mediaFileRepository.save(mediaFile);
            }else {
                return  new ResponseResult(MediaCode.MERGE_FILE_FAIL);
            }
            //将ID发送到发送MQ，使生成的视频转为mp4  和 mu38 格式
            Map<String,String>  map = new HashMap<>();
            map.put("fileId",fileMd5);
            String msg = JSON.toJSONString(map);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK,routingkey_media_video,msg);

            return new ResponseResult(CommonCode.SUCCESS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
}
