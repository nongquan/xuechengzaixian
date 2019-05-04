package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MediaMq {
    @Autowired
    private MediaFileRepository mediaFileRepository;
    @Value("${xc-service-manage-media.video-location}")
    private  String video_location;
    @Value("${xc-service-manage-media.ffmpeg-path}")
    private  String ffmpeg_path;


    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}",containerFactory = "customContainerFactory")
    public void getMsg(String msg){
        Map map = JSON.parseObject(msg, Map.class);
        String fileId = (String) map.get("fileId");
        if (StringUtils.isBlank(fileId)){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        Optional<MediaFile> byId = mediaFileRepository.findById(fileId);
        if (!byId.isPresent()){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        MediaFile mediaFile = byId.get();
        //判断是否为avi 不是的话直接返回 工具类只能处理avi 视频
        if (!mediaFile.getFileType().equals("avi")){
            mediaFile.setProcessStatus("303004");
            mediaFileRepository.save(mediaFile);
            return;
        }
        //将avi视频转化成mp4格式

        String video_path = video_location + mediaFile.getFileOriginalName();
        String mp4_name = fileId+".mp4";
        String mp4folder_path = video_location+mediaFile.getFilePath();
       //判断是否已经存在mp4
        String mp4FilePath = mp4folder_path+mp4_name;
            Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4folder_path);
            String mp4 = mp4VideoUtil.generateMp4();
            if (mp4==null||!mp4.equals("success")){
                mediaFile.setProcessStatus("303003");
                mediaFileRepository.save(mediaFile);
                ExceptionCast.cast(CommonCode.FAIL);
            }
            mediaFile.setProcessStatus("303001");
            mediaFileRepository.save(mediaFile);

        //将mp4格式 转化成m3u8与ts
        String hls_path = video_location+mediaFile.getFilePath()+ mp4_name;
        String m3u8_name = fileId+".m3u8";
        File file = new File(video_location+mediaFile.getFilePath()+"hls/");
        if (file.exists()){
            file.delete();
            file.mkdirs();
        }else {
            file.mkdirs();
        }
        String m3u8folder_path = mp4folder_path+"hls/";

        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg_path,hls_path,m3u8_name,m3u8folder_path);
        String m3u8 = hlsVideoUtil.generateM3u8();
        if (m3u8==null||!m3u8.equals("success")){
            mediaFile.setProcessStatus("303003");
            mediaFileRepository.save(mediaFile);
            ExceptionCast.cast(CommonCode.FAIL);
        }
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setProcessStatus("303002");
        mediaFile.setFileUrl(m3u8folder_path+m3u8_name);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
        mediaFileRepository.save(mediaFile);
    }
}
