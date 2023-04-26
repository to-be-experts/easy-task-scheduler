package com.yueshuo.scheduler.admin.service.impl;

import com.yueshuo.scheduler.admin.core.conf.XxlJobAdminConfig;
import com.yueshuo.scheduler.admin.core.dag.DAGQueueMgr;
import com.yueshuo.scheduler.admin.core.dag.DAGRunRecordCacheUtils;
import com.yueshuo.scheduler.admin.core.dag.DAGTaskHandlerHelper;
import com.yueshuo.scheduler.admin.core.model.PstDagJobInfo;
import com.yueshuo.scheduler.admin.core.model.PstDagJobRunRecordInfo;
import com.yueshuo.scheduler.admin.core.model.XxlJobInfo;
import com.yueshuo.scheduler.admin.core.model.XxlJobLog;
import com.yueshuo.scheduler.admin.core.thread.DAGJobScheduleHelper;
import com.yueshuo.scheduler.admin.core.thread.JobScheduleHelper;
import com.yueshuo.scheduler.admin.core.thread.JobTriggerPoolHelper;
import com.yueshuo.scheduler.admin.core.trigger.TriggerTypeEnum;
import com.yueshuo.scheduler.admin.core.util.BeanCopyUtils;
import com.yueshuo.scheduler.admin.core.util.I18nUtil;
import com.yueshuo.scheduler.admin.dao.PstDagJobInfoDao;
import com.yueshuo.scheduler.admin.dao.PstDagJobRunRecordDao;
import com.yueshuo.scheduler.admin.dao.XxlJobInfoDao;
import com.yueshuo.scheduler.admin.service.PstDagJobService;
import com.yueshuo.scheduler.admin.vo.DAGJobInfoVO;
import com.yueshuo.scheduler.admin.vo.Node;
import com.yueshuo.scheduler.core.biz.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class PstDagJobServiceImpl implements PstDagJobService {
    private static Logger logger = LoggerFactory.getLogger(PstDagJobServiceImpl.class);
    @Resource
    private PstDagJobInfoDao dagJobInfoDao ;
    @Resource
    private XxlJobInfoDao xxlJobInfoDao;
    @Resource
    private PstDagJobRunRecordDao runRecordDao ;

    @Override
    public Map<String, Object> pageList(int start, int length, String jobName, String jobDesc, int status) {
        // page list
        List<PstDagJobInfo> list = dagJobInfoDao.pageList(start, length, jobName, jobDesc, status );
        int list_count = dagJobInfoDao.pageListCount(start, length, jobName, jobDesc, status);
        List<DAGJobInfoVO> vos = BeanCopyUtils.copyListProperties(list,DAGJobInfoVO::new,(dbInfo, vo) ->{
            long times = 0 ;

            List<PstDagJobRunRecordInfo> records = runRecordDao.loadByDataTimeAndDagJobId(dbInfo.getLastRunTime(),dbInfo.getId()) ;
            int nodeUnRuning = 0 ;
            int nodeRuning = 0 ;
            int nodeRunEnd = 0 ;
            int nodeRunErr = 0 ;
            int nodeRunLose = 0 ;
            for(PstDagJobRunRecordInfo record : records){
                if(record.getRunStatus() == 0){
                    nodeUnRuning ++ ;
                }else if(record.getRunStatus() == 1){
                    nodeRuning ++ ;
                }else if(record.getRunStatus() == 2){
                    nodeRunEnd ++ ;
                }else if(record.getRunStatus() == 3){
                    nodeRunErr ++ ;
                }
            }
            vo.setUnRuning(nodeUnRuning);
            vo.setRuning(nodeRuning);
            vo.setRunErr(nodeRunErr);
            vo.setRunOk(nodeRunEnd);
            vo.setNodeTotal(records.size());
            vo.setQueueSize(DAGQueueMgr.getQueueSize(dbInfo));
        });
        // package result
        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("recordsTotal", list_count);		// 总记录数
        maps.put("recordsFiltered", list_count);	// 过滤后的总记录数
        maps.put("data", vos);  					// 分页列表
        return maps;
    }

    @Override
    public ReturnT<String> add(PstDagJobInfo jobInfo) {
        jobInfo.setStatus(0);
        jobInfo.setCreateTime(new Date());
        dagJobInfoDao.save(jobInfo);
        if (jobInfo.getId() < 1) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_add")+I18nUtil.getString("system_fail")) );
        }

        return new ReturnT<String>(String.valueOf(jobInfo.getId()));
    }

    @Override
    public ReturnT<String> update(PstDagJobInfo jobInfo) {
        PstDagJobInfo existsInfo = dagJobInfoDao.loadById(jobInfo.getId()) ;
        if (existsInfo == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_id")+I18nUtil.getString("system_not_found")) );
        }
        // next trigger time (5s后生效，避开预读周期)
        long nextTriggerTime = existsInfo.getTriggerNextTime();
        if (existsInfo.getStatus() == 1 ) {
            try {
                Date nextValidTime = JobScheduleHelper.generateNextValidTime(jobInfo, new Date(System.currentTimeMillis() + JobScheduleHelper.PRE_READ_MS));
                if (nextValidTime == null) {
                    return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid")) );
                }
                nextTriggerTime = nextValidTime.getTime();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid")) );
            }
        }
        existsInfo.setJobRunExp(jobInfo.getJobRunExp());
        existsInfo.setJobName(jobInfo.getJobName());
        existsInfo.setJobDesc(jobInfo.getJobDesc());
        existsInfo.setExecutorBlockStrategy(jobInfo.getExecutorBlockStrategy());
        existsInfo.setTriggerNextTime(nextTriggerTime);

        dagJobInfoDao.update(existsInfo);
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> updateDagInfo(PstDagJobInfo jobInfo) {

        dagJobInfoDao.updateDagInfo(jobInfo);

        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> remove(int id) {
        dagJobInfoDao.delete(id) ;
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> start(int id) {
        PstDagJobInfo dagJobInfo = dagJobInfoDao.loadById(id) ;

        // next trigger time (5s后生效，避开预读周期)
        long nextTriggerTime = 0;
        try {
            Date nextValidTime = DAGJobScheduleHelper.generateNextValidTime(dagJobInfo, new Date(System.currentTimeMillis() + JobScheduleHelper.PRE_READ_MS));
            if (nextValidTime == null) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid")) );
            }
            nextTriggerTime = nextValidTime.getTime();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid")) );
        }

        dagJobInfo.setTriggerLastTime(0);
        dagJobInfo.setTriggerNextTime(nextTriggerTime);
        dagJobInfo.setStatus(1);

        dagJobInfoDao.update(dagJobInfo);

        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> trigger(int id) {
        //启动一次时，直接启动开始节点，然后启动开始节点下一级节点
        //启动开始节点时，给该dag依赖的所有任务设置唯一的运行版本参数
        //如果正在有依赖的dag占用任务未执行完或未执行，则其他任务阻塞，即不可分配dag执行版本
        //当运行到结束节点时，将所有的依赖任务dag运行参数置为空
        PstDagJobInfo dagJobInfo = dagJobInfoDao.loadById(id) ;
        dagJobInfo.setTriggerLastTime(new Date().getTime());
        dagJobInfo.setLastRunStatus(0);

        dagJobInfoDao.update(dagJobInfo) ;

        DAGTaskHandlerHelper.start(id) ;

        return ReturnT.SUCCESS;
    }

    /**
     * 执行一次，写入记录，模式值为1
     * @param dagJobId
     * @param jobId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ReturnT<String> triggerOneTime(long dagJobId ,int jobId,String record) {
        XxlJobInfo jobInfo = XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().loadById(jobId) ;
        if(jobInfo == null){
            return new ReturnT(ReturnT.FAIL_CODE,"【JobID = "+ jobId +" 】 执行失败，无法查询到相关任务");
        }
        if(!DAGTaskHandlerHelper.checkJobExecGroupIdle(jobInfo)){
            return new ReturnT(ReturnT.FAIL_CODE,"【Job "+jobInfo.getJobDesc()+"】 执行失败，执行器未注册");
        }
        PstDagJobRunRecordInfo recordInfo = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecordAndJobId(record,jobId) ;

        JobTriggerPoolHelper.trigger(jobId, TriggerTypeEnum.MANUAL, -1, null, "", "",recordInfo.getRunRecord());

        recordInfo.setRunStatus(1);
        recordInfo.setRunMod(1);
        XxlJobAdminConfig.getAdminConfig().getRunRecordDao().update(recordInfo) ;

        //缓存记录
        DAGRunRecordCacheUtils.put(recordInfo.getRunRecord()+"-"+jobId,recordInfo);

        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> SkipCurrStep(int jobId, String record) {
        XxlJobLog log = new XxlJobLog();
        log.setDagRunRecord(record);
        log.setJobId(jobId);

        log = XxlJobAdminConfig.getAdminConfig().getXxlJobLogDao().loadByJobIdAndDagRunRecord(log);

        log.setHandleMod(2);
        XxlJobAdminConfig.getAdminConfig().getXxlJobLogDao().updateHandleInfo(log) ;
        //1、先将该job kill掉，
        ReturnT<String> runResult = DAGTaskHandlerHelper.killJob(log) ;
        if (ReturnT.SUCCESS_CODE == runResult.getCode()) {
            /*log.setHandleCode(ReturnT.SUCCESS_CODE);
            log.setHandleMsg( "跳过任务-"+I18nUtil.getString("joblog_kill_log_byman")+":" + (runResult.getMsg()!=null?runResult.getMsg():""));
            log.setHandleTime(new Date());
            //2、然后将该节点设置为已完成状态
            //3、启动下级节点，需要判断是否满足启动条件
            //防止正在跳过时任务已被更新完成，在设置一次运行记录值
            XxlJobInfo jobInfo = XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().loadById(jobId) ;
            jobInfo.setDagRunRecord(record);
            XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().update(jobInfo) ;

            XxlJobCompleter.updateHandleInfoAndFinish(log);*/

            return new ReturnT<String>("跳过任务成功！");
        } else {
            return new ReturnT<String>(500, "跳过任务失败，任务终止失败");
        }
    }

    @Override
    public ReturnT  triggerAgain(int jobId, String record, int type) {
        PstDagJobRunRecordInfo recordInfo = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecordAndJobId(record,jobId) ;

        if(!DAGTaskHandlerHelper.setRunRecordLogState(recordInfo)){
            return new ReturnT(ReturnT.FAIL_CODE,"【JobID = "+ jobId +" 】 重新调度失败，重置运行记录日志状态失败");
        }
        //异常补偿调度,将记录状态置为运行中
        if(type == 1){
            JobTriggerPoolHelper.trigger(jobId, TriggerTypeEnum.MANUAL, -1, null, "", "",recordInfo.getRunRecord());
            recordInfo.setRunStatus(1);
            XxlJobAdminConfig.getAdminConfig().getRunRecordDao().update(recordInfo) ;
            //缓存记录
            DAGRunRecordCacheUtils.put(record+"-"+jobId,record+"-"+jobId+"-"+type);
        }
        if(type == 2){
            //忽略异常，默认执行完成然后往下判断是否能执行
            DAGTaskHandlerHelper.handlerIgnoreRunNextNodeTask(record,jobId);
        }
        //重新调度,节点job已完成
        if(type == 3){
            //重新调度需要判断后续所有节点中是否有正在跑的节点，没有正在跑的节点才能跑，有则提示先终止
            Node node = DAGTaskHandlerHelper.canRunTriggerAgain(recordInfo) ;
            if (node != null){
                return new ReturnT(ReturnT.FAIL_CODE,"【Job "+node.getText()+"】 正在执行中，不能重跑后续整个依赖调度，请先终止后再操作");
            }else{
                if(DAGTaskHandlerHelper.restartRunRecordInfo(recordInfo)){
                    JobTriggerPoolHelper.trigger(jobId, TriggerTypeEnum.MANUAL, -1, null, "", "",recordInfo.getRunRecord());

                    recordInfo.setRunStatus(1);
                    recordInfo.setRunMod(0);

                    XxlJobAdminConfig.getAdminConfig().getRunRecordDao().update(recordInfo) ;
                    //缓存记录
                    DAGRunRecordCacheUtils.put(record+"-"+jobId,record+"-"+jobId+"-"+type);
                }else{
                    return new ReturnT(ReturnT.FAIL_CODE,"【Job "+node.getText()+"】 重新调度失败，重置依赖任务状态失败");
                }
            }

        }
        //重新执行，节点本身已执行完成，只重新执行当前节点
        if(type == 4){
            JobTriggerPoolHelper.trigger(jobId, TriggerTypeEnum.MANUAL, -1, null, "", "",recordInfo.getRunRecord());

            recordInfo.setRunStatus(1);
            recordInfo.setRunMod(1);
            XxlJobAdminConfig.getAdminConfig().getRunRecordDao().update(recordInfo) ;

            //缓存记录
            DAGRunRecordCacheUtils.put(record+"-"+jobId,record+"-"+jobId+"-"+type);
        }
        //更新状态为执行中
        if(recordInfo != null){
            PstDagJobInfo dagJobInfo = dagJobInfoDao.loadById(recordInfo.getDagJobId()) ;
            if(dagJobInfo.getCurrRunRecord() != null){
                dagJobInfo.setLastRunRecord("");
                dagJobInfo.setLastRunStatus(0);
            }
            dagJobInfoDao.updateRunStatusInfo(dagJobInfo) ;
        }

        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> stop(int id) {
        PstDagJobInfo dagJobInfo = dagJobInfoDao.loadById(id) ;
        dagJobInfo.setStatus(0);
        dagJobInfoDao.update(dagJobInfo);

        return ReturnT.SUCCESS;
    }

    @Override
    public PstDagJobInfo loadById(long id) {
        return dagJobInfoDao.loadById(id);
    }

    @Override
    public List<PstDagJobRunRecordInfo> loadRunDataTimeByDagJobId(long dagJobId,int size) {
        return runRecordDao.loadRunDataTimeByDagJobId(dagJobId,size);
    }
}
