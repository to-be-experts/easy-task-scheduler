package com.xxl.job.admin.dao;

import com.xxl.job.admin.core.model.PstDagJobInfo;
import com.xxl.job.admin.core.model.XxlJobInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PstDagJobInfoDao {
    public List<PstDagJobInfo> pageList(@Param("offset") int offset,
                                     @Param("pagesize") int pagesize,
                                     @Param("jobName") String jobName,
                                     @Param("jobDesc") String jobDesc,
                                     @Param("status") int status );
    public int pageListCount(@Param("offset") int offset,
                             @Param("pagesize") int pagesize,
                             @Param("jobName") String jobName,
                             @Param("jobDesc") String jobDesc,
                             @Param("status") int status );

    public int save(PstDagJobInfo info);

    public PstDagJobInfo loadById(@Param("id") long id);

    public int update(PstDagJobInfo xxlJobInfo);

    public int updateDagInfo(PstDagJobInfo xxlJobInfo);

    public int updateRunStatusInfo(PstDagJobInfo xxlJobInfo);

    public int delete(@Param("id") long id);

    public List<PstDagJobInfo> scheduleJobQuery(@Param("maxNextTime") long maxNextTime, @Param("pagesize") int pagesize );

    public int scheduleUpdate(PstDagJobInfo xxlJobInfo);
}
