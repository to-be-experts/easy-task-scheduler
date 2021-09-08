package com.xxl.job.admin.service.impl;

import com.xxl.job.admin.core.model.PstDagJobInfo;
import com.xxl.job.admin.core.model.PstDagJobRunRecordInfo;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.PstDagJobRunRecordDao;
import com.xxl.job.admin.service.PstDagJobRunRecordService;
import com.xxl.job.admin.service.PstDagJobService;
import com.xxl.job.admin.vo.JobDagInfoVO;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.util.GsonTool;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PstDagJobRunRecordServiceImpl implements PstDagJobRunRecordService {
    @Resource
    private PstDagJobRunRecordDao runRecordDao ;


    @Override
    public Map<String, Object> pageList(int start, int length, String jobName, String jobDesc, int status) {
        // page list
        List<PstDagJobRunRecordInfo> list = runRecordDao.pageList(start, length, jobName, jobDesc, status );
        int list_count = runRecordDao.pageListCount(start, length, jobName, jobDesc, status);

        // package result
        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("recordsTotal", list_count);		// 总记录数
        maps.put("recordsFiltered", list_count);	// 过滤后的总记录数
        maps.put("data", list);  					// 分页列表
        return maps;
    }

    @Override
    public List<PstDagJobRunRecordInfo> loadByRunRecord(String runRecord) {
        return runRecordDao.loadByRunRecord(runRecord);
    }

    @Override
    public PstDagJobRunRecordInfo loadById(long id) {
        return runRecordDao.loadById(id);
    }

    @Override
    public ReturnT<String> add(PstDagJobRunRecordInfo jobInfo) {
        jobInfo.setCreateTime(new Date());
        runRecordDao.save(jobInfo);
        if (jobInfo.getId() < 1) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_add")+I18nUtil.getString("system_fail")) );
        }

        return new ReturnT<String>(String.valueOf(jobInfo.getId()));
    }

    @Override
    public ReturnT<String> update(PstDagJobRunRecordInfo jobInfo) {
        return null;
    }


    @Override
    public ReturnT<String> remove(int id) {
        return null;
    }

}
