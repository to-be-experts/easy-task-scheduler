package com.xxl.job.admin.dao;

import com.xxl.job.admin.core.model.PstDagJobInfo;
import com.xxl.job.admin.core.model.PstDagJobRunRecordInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PstDagJobRunRecordDao {
    public List<PstDagJobRunRecordInfo> pageList(@Param("offset") int offset,
                                     @Param("pagesize") int pagesize,
                                     @Param("jobName") String jobName,
                                     @Param("jobDesc") String jobDesc,
                                     @Param("status") int status );
    public int pageListCount(@Param("offset") int offset,
                             @Param("pagesize") int pagesize,
                             @Param("jobName") String jobName,
                             @Param("jobDesc") String jobDesc,
                             @Param("status") int status );

    public int save(PstDagJobRunRecordInfo info);

    public List<PstDagJobRunRecordInfo> loadByRunRecord(@Param("runRecord") String runRecord);

    public List<PstDagJobRunRecordInfo> loadByDataTimeAndDagJobId(@Param("runDataTime") long dataTime,@Param("dagJobId") long dagJobId);

    public PstDagJobRunRecordInfo loadById(@Param("id") long id);

    public PstDagJobRunRecordInfo loadByRunRecordAndJobId(@Param("runRecord") String runRecord,@Param("jobId") long jobId);

    public PstDagJobRunRecordInfo loadByRunRecordAndDagTaskType (@Param("runRecord") String runRecord,@Param("dagTaskType") int dagTaskType);

    public List<PstDagJobRunRecordInfo> loadRunDataTimeByDagJobId(@Param("dagJobId") long dagJobId,@Param("size") int size);

    public int update(PstDagJobRunRecordInfo xxlJobInfo);


    public int delete(@Param("id") long id);
}
