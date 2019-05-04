package com.xuecheng.test.fastdfs;

import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFastDFS {

    //上传文件
    @Test
    public void  test1(){

        try {
            //加载配置文件
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //定义TrackerClient
            TrackerClient trackerClient = new TrackerClient();
            //连接tracker
            TrackerServer trackerServer = trackerClient.getConnection();
            //获得Storage
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //创建storageClient
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storeStorage);
            //本地文件路径
            String path = "C:/Users/acer/Desktop/b.jpg";
            //上传成功 返回ID
            String file1Id = storageClient1.upload_appender_file1(path, "jpg", null);
            System.out.println(file1Id);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    //下载文件
    @Test
    public void  test2(){
        try {
            //加载配置文件
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //定义TrackerClient
            TrackerClient trackerClient = new TrackerClient();
            //连接tracker
            TrackerServer trackerServer = trackerClient.getConnection();
            //获得Storage
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //创建storageClient
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storeStorage);
            //文件Id
            String file1Id = "group1/M00/00/01/wKgZhVyyoLqEZ3b8AAAAAACbVVc751.jpg";
            byte[] bytes = storageClient1.download_file1(file1Id);
            ;
            FileOutputStream fileOutputStream = new FileOutputStream(new File("d:/a.jpg"));
            fileOutputStream.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
