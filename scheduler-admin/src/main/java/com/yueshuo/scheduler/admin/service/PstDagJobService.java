package com.yueshuo.scheduler.admin.service;

import com.yueshuo.scheduler.admin.core.model.PstDagJobInfo;
import com.yueshuo.scheduler.admin.core.model.PstDagJobRunRecordInfo;
import com.xxl.job.core.biz.model.ReturnT;

import java.util.List;
import java.util.Map;

public interface PstDagJobService {

    /**
     * page list
     *
     * @param start
     * @param length
     * @return
     */
    public Map<String, Object> pageList(int start, int length, String jobName, String jobDesc, int status);

    /**
     * add job
     *
     * @param jobInfo
     * @return
     */
    public ReturnT<String> add(PstDagJobInfo jobInfo);

    /**
     * update job
     *
     * @param jobInfo
     * @return
     */
    public ReturnT<String> update(PstDagJobInfo jobInfo);

    /**
     * 更新dag调度依赖信息
     * @param jobInfo
     * @return
     */
    public ReturnT<String> updateDagInfo(PstDagJobInfo jobInfo);

    /**
     * remove job
     * 	 *
     * @param id
     * @return
     */
    public ReturnT<String> remove(int id);

    /**
     * start job
     *
     * @param id
     * @return
     */
    public ReturnT<String> start(int id);

    /**
     * 执行
     *
     * @param id
     * @return
     */
    public ReturnT<String> trigger(int id);

    /**
     * 异常补偿执行
     *
     * @param jobId
     * @param record
     * @param type
     * @return
     */
    public ReturnT<String> triggerAgain(int jobId,String record,int type);

    /**
     * 执行一次
     * @param jobId
     * @return
     */
    public ReturnT<String> triggerOneTime(long dagJobId,int jobId,String record) ;

    /**
     * 跳过当前正在执行的job，直接执行下一级节点
     * @param jobId
     * @param record
     * @return
     */
    public ReturnT<String> SkipCurrStep(int jobId,String record) ;

    /**
     * stop job
     *
     * @param id
     * @return
     */
    public ReturnT<String> stop(int id);

    public PstDagJobInfo loadById(long id);

    public List<PstDagJobRunRecordInfo> loadRunDataTimeByDagJobId(long dagJobId,int size);
}
