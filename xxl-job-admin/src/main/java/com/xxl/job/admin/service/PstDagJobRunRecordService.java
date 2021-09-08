package com.xxl.job.admin.service;

import com.xxl.job.admin.core.model.PstDagJobInfo;
import com.xxl.job.admin.core.model.PstDagJobRunRecordInfo;
import com.xxl.job.core.biz.model.ReturnT;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface PstDagJobRunRecordService {

    /**
     * page list
     *
     * @param start
     * @param length
     * @return
     */
    public Map<String, Object> pageList(int start, int length, String jobName, String jobDesc, int status);


    public List<PstDagJobRunRecordInfo> loadByRunRecord(String runRecord);

    public PstDagJobRunRecordInfo loadById( long id);
    /**
     * add job
     *
     * @param jobInfo
     * @return
     */
    public ReturnT<String> add(PstDagJobRunRecordInfo jobInfo);

    /**
     * update job
     *
     * @param jobInfo
     * @return
     */
    public ReturnT<String> update(PstDagJobRunRecordInfo jobInfo);



    /**
     * remove job
     * 	 *
     * @param id
     * @return
     */
    public ReturnT<String> remove(int id);


}
