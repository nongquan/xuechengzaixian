package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ChoseCourseService {

    @Autowired
    XcTaskRepository xcTaskRepository;
    @Autowired
    XcTaskHisRepository xcTaskHisRepository;

    //取到需要发送的课程信息
    public List<XcTask> findNeedSendXcTask(Date time) {
        List<XcTask> byUpdateTimeBefore = xcTaskRepository.findByUpdateTimeBefore(time);
        return byUpdateTimeBefore;
    }
    //发送成功后更新时间
    @Transactional
    public int updateTime(String id, Date time) {
        return  xcTaskRepository.updateTaskTime(id,time);

    }
    // 高可用状态下使用乐观锁使得在规定时间中，只执行一次Task
    @Transactional
    public int updateTaskVersion(String id, int version) {
        return  xcTaskRepository.updateTaskVersion(id,version);

    }
    //删除订单任务表 添加历史记录表
    @Transactional
    public ResponseResult deleteTask(XcTask xcTask) {
        xcTaskRepository.deleteById(xcTask.getId());
        Optional<XcTask> byId = xcTaskRepository.findById(xcTask.getId());
        if (byId.isPresent()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        XcTaskHis xcTaskHis = new XcTaskHis();
        BeanUtils.copyProperties(xcTask,xcTaskHis);
        xcTaskHis.setDeleteTime(new Date());
        XcTaskHis save = xcTaskHisRepository.save(xcTaskHis);
        if (save==null){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
