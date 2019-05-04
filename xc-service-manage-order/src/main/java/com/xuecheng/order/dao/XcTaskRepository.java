package com.xuecheng.order.dao;

import com.xuecheng.framework.domain.task.XcTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface XcTaskRepository extends JpaRepository<XcTask,String> {
    //取到需要发送的课程信息
    List<XcTask> findByUpdateTimeBefore(Date updateTime);
    //发送成功后更新时间
    @Modifying
    @Query("update XcTask t set t.updateTime = :updateTime where t.id = :id ")
    public int updateTaskTime(@Param(value = "id") String id,@Param(value = "updateTime")Date updateTime);
    // 高可用状态下使用乐观锁使得在规定时间中，只执行一次Task
    @Modifying
    @Query("UPDATE XcTask set version =:version+1 where id=:id and version=:version")
    int updateTaskVersion(@Param("id") String id,@Param("version") int version);
}
