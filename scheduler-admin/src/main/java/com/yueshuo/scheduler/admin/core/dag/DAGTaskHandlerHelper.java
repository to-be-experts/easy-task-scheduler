package com.yueshuo.scheduler.admin.core.dag;

import com.yueshuo.scheduler.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.*;
import com.yueshuo.scheduler.admin.core.model.*;
import com.yueshuo.scheduler.admin.core.scheduler.XxlJobScheduler;
import com.yueshuo.scheduler.admin.core.thread.JobTriggerPoolHelper;
import com.yueshuo.scheduler.admin.core.trigger.TriggerTypeEnum;
import com.yueshuo.scheduler.admin.core.util.I18nUtil;
import com.yueshuo.scheduler.admin.vo.JobDagInfoVO;
import com.yueshuo.scheduler.admin.vo.Node;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.client.ExecutorBizClient;
import com.xxl.job.core.biz.model.IdleBeatParam;
import com.xxl.job.core.biz.model.KillParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.util.DateUtil;
import com.xxl.job.core.util.GsonTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/***
 * 处理 DGA 调度流程
 */
public class DAGTaskHandlerHelper {
    private static Logger logger = LoggerFactory.getLogger(DAGTaskHandlerHelper.class);

    /**
     * DAG 运行任务完成，处理是否能执行下级依赖任务
     * 1、 找到当前任务的下级依赖节点，可能多个
     * 2、 根据 DAG 运行记录，查找当前某一个下级依赖节点的所有依赖的父节点的任务是否都执行完成，完成则启动下级节点的任务，未完成的丢弃并更新当前节点DAG运行记录为已完成
     * @param xxlJobInfo
     */
    public static void handlerRunNextNodeTask(XxlJobInfo xxlJobInfo){
        PstDagJobRunRecordInfo recordInfo = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecordAndJobId(xxlJobInfo.getDagRunRecord(),xxlJobInfo.getId()) ;
        PstDagJobInfo dagJobInfo = XxlJobAdminConfig.getAdminConfig().getDagJobInfoDao().loadById(recordInfo.getDagJobId()) ;

        //先更新当前记录的job为已执行完成，并更新job的运行记录信息为空
        if(recordInfo != null){
            recordInfo.setRunStatus(2);
            recordInfo.setRunEndTime(new Date());

            XxlJobAdminConfig.getAdminConfig().getRunRecordDao().update(recordInfo) ;

            xxlJobInfo.setDagRunRecord("");
            XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().update(xxlJobInfo) ;
            if(recordInfo.getRunMod() == 1){//执行一次，退出
                return;
            }

            List<Node> nodes = DAGJobUtils.getNodeChildJobs(dagJobInfo,recordInfo.getJobId());
            for (Node node : nodes){
                //判断该子任务是否可执行
                if(canRunNextNode(node,dagJobInfo,recordInfo.getRunRecord())){
                    if( node.getDataId() == 102 ){
                        PstDagJobRunRecordInfo record  = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecordAndDagTaskType(recordInfo.getRunRecord(),102) ;
                        record.setRunStatus(2);
                        record.setRunStartTime(new Date());
                        record.setStartUpJobId(recordInfo.getJobId());

                        //如果已经到了结束节点，则更新结束节点信息并更新dag任务最近运行完成时间及记录
                        PstDagJobInfo dagJob = XxlJobAdminConfig.getAdminConfig().getDagJobInfoDao().loadById(recordInfo.getDagJobId()) ;
                        dagJob.setLastRunMsg(" 运行结束：成功 " );
                        dagJob.setLastRunRecord(recordInfo.getRunRecord());
                        dagJobInfo.setLastRunStatus(1);
                        //dagJob.setLastRunTime(new Date());

                        dagJob.setCurrRunRecord("");
                        XxlJobAdminConfig.getAdminConfig().getDagJobInfoDao().updateRunStatusInfo(dagJob) ;


                        record.setRunEndTime(new Date());
                        XxlJobAdminConfig.getAdminConfig().getRunRecordDao().update(record) ;

                        //缓存记录
                        DAGRunRecordCacheUtils.put(record.getRunRecord()+"-"+record.getJobId(),record);

                    }else {
                        JobTriggerPoolHelper.trigger(Integer.parseInt(node.getTaskId()+""), TriggerTypeEnum.PARENT, -1, null, null, null,recordInfo.getRunRecord());
                        PstDagJobRunRecordInfo record  = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecordAndJobId(recordInfo.getRunRecord(),node.getTaskId()) ;
                        record.setRunStatus(1);
                        record.setRunStartTime(new Date());
                        record.setStartUpJobId(recordInfo.getJobId());

                        XxlJobAdminConfig.getAdminConfig().getRunRecordDao().update(record) ;
                        //缓存记录
                        DAGRunRecordCacheUtils.put(record.getRunRecord()+"-"+record.getJobId(),record);
                    }

                }
            }
        }else{
            xxlJobInfo.setDagRunRecord("");
            XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().update(xxlJobInfo) ;
        }
    }

    /**
     * DAG 运行任务完成，处理是否能执行下级依赖任务
     * 1、 找到当前任务的下级依赖节点，可能多个
     * 2、 根据 DAG 运行记录，查找当前某一个下级依赖节点的所有依赖的父节点的任务是否都执行完成，完成则启动下级节点的任务，未完成的丢弃并更新当前节点DAG运行记录为已完成
     * @param xxlJobLog
     */
    public static void handlerRunNextNodeTask(XxlJobLog xxlJobLog){
        PstDagJobRunRecordInfo recordInfo = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecordAndJobId(xxlJobLog.getDagRunRecord(),xxlJobLog.getJobId()) ;
        PstDagJobInfo dagJobInfo = XxlJobAdminConfig.getAdminConfig().getDagJobInfoDao().loadById(recordInfo.getDagJobId()) ;

        //先更新当前记录的job为已执行完成，并更新job的运行记录信息为空
        if(recordInfo != null){
            recordInfo.setRunStatus(2);
            recordInfo.setRunEndTime(new Date());

            XxlJobAdminConfig.getAdminConfig().getRunRecordDao().update(recordInfo) ;
            if(recordInfo.getRunMod() == 1){//执行一次，退出
                return;
            }

            List<Node> nodes = DAGJobUtils.getNodeChildJobs(dagJobInfo,recordInfo.getJobId());
            for (Node node : nodes){
                //判断该子任务是否可执行
                if(canRunNextNode(node,dagJobInfo,recordInfo.getRunRecord())){
                    if( node.getDataId() == 102 ){
                        PstDagJobRunRecordInfo record  = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecordAndDagTaskType(recordInfo.getRunRecord(),102) ;
                        record.setRunStatus(2);
                        record.setRunStartTime(new Date());
                        record.setStartUpJobId(recordInfo.getJobId());

                        PstDagJobInfo dagJob = XxlJobAdminConfig.getAdminConfig().getDagJobInfoDao().loadById(recordInfo.getDagJobId()) ;
                        //判断是否是覆盖执行策略，是则判断是否有正在运行的记录，且不等于当前完成的记录，有则不更新
                        DAGExecutorBlockStrategyEnum blockStrategy = DAGExecutorBlockStrategyEnum.match(dagJobInfo.getExecutorBlockStrategy(), null);
                        if (DAGExecutorBlockStrategyEnum.COVER_EARLY == blockStrategy && !dagJob.getCurrRunRecord().equals(record.getRunRecord())) {

                        }else{
                            //如果已经到了结束节点，则更新结束节点信息并更新dag任务最近运行完成时间及记录
                            dagJob.setLastRunMsg(" 运行结束：成功 " );
                            dagJob.setLastRunRecord(recordInfo.getRunRecord());
                            dagJob.setLastRunStatus(1);
                            ///dagJob.setLastRunTime(new Date());

                            dagJob.setCurrRunRecord("");
                            XxlJobAdminConfig.getAdminConfig().getDagJobInfoDao().updateRunStatusInfo(dagJob) ;
                        }
                        record.setRunEndTime(new Date());
                        XxlJobAdminConfig.getAdminConfig().getRunRecordDao().update(record) ;

                        //缓存记录
                        DAGRunRecordCacheUtils.put(record.getRunRecord()+"-"+record.getJobId(),record);

                        //运行成功，去看看队列里面是否有排队，有排队则运行
                        runQueuesJob(dagJob) ;
                    }else {
                        JobTriggerPoolHelper.trigger(Integer.parseInt(node.getTaskId()+""), TriggerTypeEnum.PARENT, -1, null, null, null,recordInfo.getRunRecord());
                        PstDagJobRunRecordInfo record  = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecordAndJobId(recordInfo.getRunRecord(),node.getTaskId()) ;
                        record.setRunStatus(1);
                        record.setRunStartTime(new Date());
                        record.setStartUpJobId(recordInfo.getJobId());

                        XxlJobAdminConfig.getAdminConfig().getRunRecordDao().update(record) ;
                        //缓存记录
                        DAGRunRecordCacheUtils.put(record.getRunRecord()+"-"+record.getJobId(),record);
                    }

                }
            }
        }else{
            //xxlJobInfo.setDagRunRecord("");
           // XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().update(xxlJobInfo) ;
        }
    }

    /**
     * DAG 运行任务节点运行异常时，忽略执行
     * 1、 默认执行成功，然后继续往下判断是否能执行后续节点
     * @param currRecord 运行记录
     * @param jobId 节点任务ID
     */
    public static void handlerIgnoreRunNextNodeTask(String currRecord, long jobId){
        XxlJobInfo xxlJobInfo = XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().loadById(jobId) ;
        PstDagJobRunRecordInfo recordInfo = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecordAndJobId(currRecord,jobId) ;
        PstDagJobInfo dagJobInfo = XxlJobAdminConfig.getAdminConfig().getDagJobInfoDao().loadById(recordInfo.getDagJobId()) ;
        //先更新当前记录的job为已执行完成，并更新job的运行记录信息为空
        if(recordInfo != null){
            recordInfo.setRunStatus(2);
            recordInfo.setRunEndTime(new Date());

            XxlJobAdminConfig.getAdminConfig().getRunRecordDao().update(recordInfo) ;

            //移除缓存记录
            DAGRunRecordCacheUtils.remove(recordInfo.getRunRecord()+"-"+recordInfo.getJobId());

            xxlJobInfo.setDagRunRecord("");
            XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().update(xxlJobInfo) ;


            List<Node> nodes = DAGJobUtils.getNodeChildJobs(dagJobInfo,recordInfo.getJobId());
            for (Node node : nodes){
                if(canRunNextNode(node,dagJobInfo,recordInfo.getRunRecord()) ){
                    if( node.getDataId() == 102 ){
                        PstDagJobRunRecordInfo record  = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecordAndDagTaskType(recordInfo.getRunRecord(),102) ;
                        record.setRunStatus(2);
                        record.setRunStartTime(new Date());
                        record.setStartUpJobId(recordInfo.getJobId());

                        //如果已经到了结束节点，则更新结束节点信息并更新dag任务最近运行完成时间及记录
                        PstDagJobInfo dagJob = XxlJobAdminConfig.getAdminConfig().getDagJobInfoDao().loadById(recordInfo.getDagJobId()) ;
                        dagJob.setLastRunMsg(" 运行结束：成功 " );
                        dagJob.setLastRunRecord(recordInfo.getRunRecord());
                        dagJob.setLastRunStatus(1);
                        //dagJob.setLastRunTime(new Date());

                        dagJob.setCurrRunRecord("");
                        XxlJobAdminConfig.getAdminConfig().getDagJobInfoDao().updateRunStatusInfo(dagJob) ;


                        record.setRunEndTime(new Date());
                        XxlJobAdminConfig.getAdminConfig().getRunRecordDao().update(record) ;
                        //缓存记录
                        DAGRunRecordCacheUtils.put(record.getRunRecord()+"-"+record.getJobId(),record);

                    }else {//判断该子任务是否可执行
                        JobTriggerPoolHelper.trigger(Integer.parseInt(node.getTaskId()+""), TriggerTypeEnum.PARENT, -1, null, null, null,recordInfo.getRunRecord());
                        PstDagJobRunRecordInfo record  = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecordAndJobId(recordInfo.getRunRecord(),node.getTaskId()) ;
                        record.setRunStatus(1);
                        record.setRunStartTime(new Date());
                        record.setStartUpJobId(recordInfo.getJobId());
                        //缓存记录
                        DAGRunRecordCacheUtils.put(record.getRunRecord()+"-"+record.getJobId(),record);

                        XxlJobAdminConfig.getAdminConfig().getRunRecordDao().update(record) ;
                    }
                }
            }
        }
    }

    public static void handlerRunCurrNodeError(XxlJobInfo xxlJobInfo){
        PstDagJobRunRecordInfo recordInfo = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecordAndJobId(xxlJobInfo.getDagRunRecord(),xxlJobInfo.getId()) ;
        // 将运行记录置为失败，不更新job对应的运行记录信息
        // 这里需要将dag job 更新为异常状态，因为有一个执行不完成，就不会继续往下执行,更新状态有利于查看
        PstDagJobInfo dagJob = XxlJobAdminConfig.getAdminConfig().getDagJobInfoDao().loadById(recordInfo.getDagJobId()) ;
        if(recordInfo != null){
            recordInfo.setRunStatus(3);
            recordInfo.setRunEndTime(new Date());

            XxlJobAdminConfig.getAdminConfig().getRunRecordDao().update(recordInfo) ;
            //移除缓存记录
            DAGRunRecordCacheUtils.remove(recordInfo.getRunRecord()+"-"+recordInfo.getJobId());

            dagJob.setLastRunMsg("运行失败：失败节点JOB ID 为 ："+ recordInfo.getJobId() );
            dagJob.setLastRunRecord(recordInfo.getRunRecord());
            dagJob.setLastRunStatus(2);
            //dagJob.setLastRunTime(new Date());

            XxlJobAdminConfig.getAdminConfig().getDagJobInfoDao().updateRunStatusInfo(dagJob) ;
        }
    }

    public static void handlerRunCurrNodeError(XxlJobLog xxlJobLog){
        PstDagJobRunRecordInfo recordInfo = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecordAndJobId(xxlJobLog.getDagRunRecord(),xxlJobLog.getJobId()) ;
        // 将运行记录置为失败，不更新job对应的运行记录信息
        // 这里需要将dag job 更新为异常状态，因为有一个执行不完成，就不会继续往下执行,更新状态有利于查看
        if(recordInfo != null){
            PstDagJobInfo dagJob = XxlJobAdminConfig.getAdminConfig().getDagJobInfoDao().loadById(recordInfo.getDagJobId()) ;

            recordInfo.setRunStatus(3);
            recordInfo.setRunEndTime(new Date());

            XxlJobAdminConfig.getAdminConfig().getRunRecordDao().update(recordInfo) ;
            //移除缓存记录
            DAGRunRecordCacheUtils.remove(recordInfo.getRunRecord()+"-"+recordInfo.getJobId());

            dagJob.setLastRunMsg("运行失败：失败节点JOB ID 为 ："+ recordInfo.getJobId() );
            dagJob.setLastRunRecord(recordInfo.getRunRecord());
            dagJob.setLastRunStatus(2);
            //dagJob.setLastRunTime(new Date());

            XxlJobAdminConfig.getAdminConfig().getDagJobInfoDao().updateRunStatusInfo(dagJob) ;
        }
    }

    /***
     * 判断当前节点是否可以启动
     * 校验依赖的父节点是否都执行完毕
     * @param node
     * @return
     */
    private static boolean canRunNextNode(Node node,PstDagJobInfo dagJobInfo,String runRecord){
        boolean canRun = true ;
        List<Node> pNodes = DAGJobUtils.getNodeParentJobs(dagJobInfo,node) ;
        for (Node pNode : pNodes){
            PstDagJobRunRecordInfo recordInfo = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecordAndJobId(runRecord,pNode.getTaskId()) ;
            if(recordInfo.getRunStatus() != 2){
                canRun = false ;
            }
        }
        return canRun ;
    }

    /***
     * 判断当前 DAG 任务是否满足启动条件
     * 不可启动的情况 ：是丢弃还是覆盖执行 ？
     * 1、上一次执行未结束，即有运行记录正在正常运行
     * 2、要启动的 DAG 调度包含某一个任务正在被其他任务暂用且未执行完成
     * dagJobInfo 当前要启动的 DAG 任务
     * @return
     */
    private static int canRun(PstDagJobInfo dagJobInfo,JobDagInfoVO jobDag){
        int canRun = 0 ;
        if(dagJobInfo.getCurrRunRecord() != null && !"".equals(dagJobInfo.getCurrRunRecord())){
            //当前记录有值，则表示记录未完全执行完成，不可执行
            return 1 ;
        }
        List<Node> pNodes = jobDag.getNodes() ;
        for (Node pNode : pNodes){
            if(pNode.getDataId() == 101 || pNode.getDataId() == 102){
                continue;
            }
            XxlJobInfo info = XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().loadById(Integer.parseInt(pNode.getTaskId()+"")) ;
            if(info.getDagRunRecord() != null && !"".equals(info.getDagRunRecord())){
                //如果某一个job还有运行记录，则查询该记录的运行状态
                PstDagJobRunRecordInfo recordInfo = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecordAndJobId(info.getDagRunRecord(), pNode.getTaskId()) ;
                if(recordInfo != null && recordInfo.getRunStatus() != 2 && recordInfo.getRunStatus() != 3 && recordInfo.getDagJobId() != dagJobInfo.getId()){
                    canRun = 2 ;
                    logger.info("====== DAG JOB RUN FAIL： DAG 调度包含的任务ID[{}]正在被调度ID [{}] 占用且未执行完成，本次不能运行 ======",recordInfo.getJobId(),recordInfo.getDagJobId());
                    break;
                }
            }
        }
        return canRun ;
    }

    /***
     * 启动一个 dag 调度
     *
     * 1、启动时，直接找到开始节点，然后启动开始节点下一级节点
     * 2、处理开始开始节点时，给该dag依赖的所有任务设置唯一的运行版本参数（DagRunRecord）
     * 3、如果正在有依赖的dag占用任务未执行完或未执行，则其他任务阻塞，即不可分配dag执行版本
     * 4、当运行到结束节点时，将所有的依赖任务dag运行参数置为空
     * @param id
     */
    public static boolean start(long id){
        PstDagJobInfo dagJobInfo = XxlJobAdminConfig.getAdminConfig().getDagJobInfoDao().loadById(id) ;
        JobDagInfoVO jobDag = GsonTool.fromJson(dagJobInfo.getDagInfo(),JobDagInfoVO.class) ;
        int canRun = canRun(dagJobInfo,jobDag) ;
        if(canRun == 1){
            logger.info("====== DAG JOB RUN ： 上一次执行未结束，DAG 调度 正在运行 ======");
            checkExecutorBlock(dagJobInfo,jobDag) ;
            return false ;
        }else if(canRun == 2){
            logger.info("====== DAG JOB RUN ： DAG 调度包含某一个任务正在被其他任务占用且未执行完成，DAG 调度 正在运行 ======");
            checkExecutorBlock(dagJobInfo,jobDag) ;
            return false ;
        }else{
             runDAGJob(dagJobInfo,jobDag);
             return true ;
        }
    }

    private static void checkExecutorBlock(PstDagJobInfo dagJobInfo,JobDagInfoVO jobDag){
        DAGExecutorBlockStrategyEnum blockStrategy = DAGExecutorBlockStrategyEnum.match(dagJobInfo.getExecutorBlockStrategy(), null);
        if (DAGExecutorBlockStrategyEnum.DISCARD_LATER == blockStrategy) {
            // 丢弃后面的调度
            logger.info("====== DAG JOB RUN FAIL： DAG调度正在运行， 调度阻塞策略为 丢弃后面的任务，本次调度周期不执行 ======");
        } else if (DAGExecutorBlockStrategyEnum.COVER_EARLY == blockStrategy) {
            //覆盖之前的调度，找到正在运行的任务记录，需要kill掉之前的任务,没kill掉有点像并行
            // kill running jobThread
            logger.info("====== DAG JOB RUN ： DAG调度正在运行， 调度阻塞策略为 覆盖之前的调度，本次调度周期正在执行 ======");
            runDAGJob(dagJobInfo,jobDag);
        }else {
            //串行，需要使用队列,在这里去入队列,然后单独的线程来启动
            DAGQueueMgr.put2Queue(dagJobInfo) ;
            int size = DAGQueueMgr.getQueueSize(dagJobInfo) ;
            XxlJobAdminConfig.getAdminConfig().getDagJobInfoDao().update(dagJobInfo);
            logger.info("====== DAG JOB RUN FAIL： DAG调度正在运行， 调度阻塞策略为 单机串行，本次调度周期不执行，放到排队队列进行排队,当前队列长度为：{} ======",size);
            // just queue trigger
            //runDAGJob(dagJobInfo,jobDag);
        }
    }

    private static void runQueuesJob(PstDagJobInfo dagJob){
        int size = DAGQueueMgr.getQueueSize(dagJob) ;
        if(size > 0){
            logger.info("====任务 ID[{}] 排队队列长度为 ：{} ====",dagJob.getId(),size);
            dagJob = DAGQueueMgr.getInQueue(dagJob) ;
            if(dagJob == null){
                return;
            }
            JobDagInfoVO jobDag = GsonTool.fromJson(dagJob.getDagInfo(),JobDagInfoVO.class) ;

            logger.info("====从队列中取出任务 ID[{}]，调度时间为 ：{} 开始运行,队列剩余长度为：{} ====",dagJob.getId(),DateUtil.formatDate(dagJob.getTriggerLastTime()),DAGQueueMgr.getQueueSize(dagJob));

            runDAGJob(dagJob,jobDag);
        }else{
            logger.info("====任务 ID[{}] 排队队列为空====",dagJob.getId());
        }
    }

    private static void runDAGJob(PstDagJobInfo dagJobInfo,JobDagInfoVO jobDag){
        Date startTime = new Date() ;
        //1.给该dag调度所有依赖的job添加唯一执行标识 并将运行记录 写到dag运行记录表
        String currRecord = dagJobInfo.getId()+"-"+ DateUtil.format(new Date(),"yyyyMMddHHmmssSSS");
        for (Node node : jobDag.getNodes()){
            if(node.getDataId() != 101 && node.getDataId() != 102){
                //添加唯一标识到运行参数
                //XxlJobInfo xxlJobInfo = XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().loadById(node.getTaskId()) ;
                //xxlJobInfo.setDagRunRecord(currRecord);
                //XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().update(xxlJobInfo) ;
            }

            //写入运行记录表
            PstDagJobRunRecordInfo recordInfo = new PstDagJobRunRecordInfo() ;
            recordInfo.setDagJobId(dagJobInfo.getId());
            recordInfo.setJobId(node.getTaskId());
            recordInfo.setRunRecord(currRecord);
            recordInfo.setDagTaskType(node.getDataId());
            recordInfo.setCreateTime(new Date());
            recordInfo.setRunStatus(0);
            recordInfo.setRunMod(0);
            recordInfo.setRunDataTime(dagJobInfo.getTriggerLastTime());

            XxlJobAdminConfig.getAdminConfig().getRunRecordDao().save(recordInfo) ;

        }

        //开始启动dag调度，异步/同步 启动
        List<Node> nodes = DAGJobUtils.getStartNodeChildJobs(jobDag) ;
        for (Node node : nodes){
            JobTriggerPoolHelper.trigger(Integer.parseInt(node.getTaskId()+""), TriggerTypeEnum.PARENT, -1, null, null, null,currRecord);
            PstDagJobRunRecordInfo recordInfo = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecordAndJobId(currRecord,node.getTaskId()) ;
            recordInfo.setRunStatus(1);
            recordInfo.setRunStartTime(startTime);
            recordInfo.setStartUpJobId(DAGJobUtils.getStartNode(jobDag).getTaskId());

            XxlJobAdminConfig.getAdminConfig().getRunRecordDao().update(recordInfo) ;

            //缓存记录
            DAGRunRecordCacheUtils.put(recordInfo.getRunRecord()+"-"+recordInfo.getJobId(),recordInfo);
        }
        PstDagJobRunRecordInfo recordInfo = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecordAndDagTaskType(currRecord,101) ;
        recordInfo.setRunStatus(2);
        recordInfo.setRunStartTime(startTime);
        recordInfo.setRunEndTime(new Date());

        XxlJobAdminConfig.getAdminConfig().getRunRecordDao().update(recordInfo) ;

        dagJobInfo.setLastRunStatus(0);
        dagJobInfo.setLastRunRecord(currRecord);
        dagJobInfo.setCurrRunRecord(currRecord);
        dagJobInfo.setLastRunTime(dagJobInfo.getTriggerLastTime());

        XxlJobAdminConfig.getAdminConfig().getDagJobInfoDao().update(dagJobInfo);
    }

    /**
     * 根据记录判断是否能重跑依赖
     * @param recordInfo
     * @return
     */
    public static Node canRunTriggerAgain(PstDagJobRunRecordInfo recordInfo){
        PstDagJobInfo dagJobInfo = XxlJobAdminConfig.getAdminConfig().getDagJobInfoDao().loadById(recordInfo.getDagJobId()) ;
        Node node = DAGJobUtils.getNodeByJobId(dagJobInfo,recordInfo.getJobId()) ;
        List<Node> childNodes = DAGJobUtils.getCurrNodeAllChildPathNodes(dagJobInfo,node) ;
        for (Node n : childNodes){
            PstDagJobRunRecordInfo record = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecordAndJobId(recordInfo.getRunRecord(),n.getTaskId()) ;
            if(record.getRunStatus() == 1){
                return  n ;
            }
        }
        return null ;
    }

    public static boolean restartRunRecordInfo(PstDagJobRunRecordInfo recordInfo){
        //重新设置运行记录值
        //XxlJobInfo xxlJobInfo = XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().loadById(recordInfo.getJobId());
        //xxlJobInfo.setDagRunRecord(recordInfo.getRunRecord());
        //XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().update(xxlJobInfo);

        PstDagJobInfo dagJobInfo = XxlJobAdminConfig.getAdminConfig().getDagJobInfoDao().loadById(recordInfo.getDagJobId()) ;
        Node node = DAGJobUtils.getNodeByJobId(dagJobInfo,recordInfo.getJobId()) ;
        List<Node> childNodes = DAGJobUtils.getCurrNodeAllChildPathNodes(dagJobInfo,node) ;
        recordInfo.setRunStatus(1);
        XxlJobAdminConfig.getAdminConfig().getRunRecordDao().update(recordInfo) ;

        for (Node n : childNodes) {
            if (n.getDataId() != 101 && n.getDataId() != 102) {
                //添加唯一标识到运行参数
                //XxlJobInfo jobInfo = XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().loadById(n.getTaskId());
                //jobInfo.setDagRunRecord(recordInfo.getRunRecord());
                //XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().update(jobInfo);

                PstDagJobRunRecordInfo record = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecordAndJobId(recordInfo.getRunRecord(),n.getTaskId()) ;
                record.setRunStatus(0);
                XxlJobAdminConfig.getAdminConfig().getRunRecordDao().update(record);

                DAGTaskHandlerHelper.setRunRecordLogState(record) ;
            }
        }

        return true ;
    }

    public static boolean setRunRecordLogState(PstDagJobRunRecordInfo recordInfo){
        XxlJobLog log = new XxlJobLog();
        log.setDagRunRecord(recordInfo.getRunRecord());
        log.setJobId(Integer.parseInt(recordInfo.getJobId()+""));

        log = XxlJobAdminConfig.getAdminConfig().getXxlJobLogDao().loadByJobIdAndDagRunRecord(log);
        //将回调日志更新为非正常模式
        if(log != null && log.getId() > 0){
            log.setHandleMod(1);
            XxlJobAdminConfig.getAdminConfig().getXxlJobLogDao().updateTriggerInfo(log) ;
        }
        return true ;
    }

    /**
     * 校验执行器是否在线
     * @return
     */
    public static boolean checkJobExecGroupIdle(XxlJobInfo jobInfo){
        ExecutorBiz executorBiz = getJobExecutorBizByJobGroup(jobInfo) ;
        if(executorBiz == null){
            return false ;
        }else{
            return true ;
        }
    }

    private static ExecutorBiz getJobExecutorBizByJobGroup(XxlJobInfo jobInfo){

        XxlJobGroup jobGroup = XxlJobAdminConfig.getAdminConfig().getXxlJobGroupDao().load(jobInfo.getJobGroup()) ;

        if(jobGroup == null || jobGroup.getAddressList() == null || "".equals(jobGroup.getAddressList())){
            return null;
        }
        String[] addrList = jobGroup.getAddressList().split(",") ;
        ExecutorBiz executorBiz = null ;
        for (String addr : addrList){
            executorBiz = new ExecutorBizClient(addr, "");
            if(executorBiz != null){
                break;
            }
        }
        return executorBiz ;
    }


    /**
     * 校验job是否能执行，不能执行则说明执行器正在跑
     * @param jobId
     * @return
     */
    public static boolean checkDagJobExecIdle(int jobId){
        if(!checkExecIdle(jobId)){
            return false ;
        }
        ExecutorBiz executorBiz = getJobExecutorBizByDagJobLog(jobId) ;
        if(executorBiz == null){
            return false;
        }
        // Act
        final ReturnT<String> retval = executorBiz.idleBeat(new IdleBeatParam(jobId));
        if(500 == retval.getCode()){
            return  false ;
        }
        return true ;
    }


    /**
     * 校验任务执行器是否在线
     * @param jobId
     * @return
     */
    public static boolean checkExecIdle(int jobId){
        ExecutorBiz executorBiz = getJobExecutorBizByDagJobLog(jobId) ;
        if(executorBiz == null){
            return false ;
        }
        final ReturnT<String> retval = executorBiz.beat();
        if( 200 == retval.getCode() ){
            return true ;
        }
        return false ;
    }

    private static ExecutorBiz getJobExecutorBizByDagJobLog(int jobId){
        XxlJobInfo jobInfo = XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().loadById(jobId) ;

        XxlJobLog jobLog = new XxlJobLog();
        jobLog.setJobId(jobInfo.getId());
        jobLog.setDagRunRecord(jobInfo.getDagRunRecord());
        if(jobInfo.getDagRunRecord() == null || "".equals(jobInfo.getDagRunRecord())){
            return null;
        }
        jobLog = XxlJobAdminConfig.getAdminConfig().getXxlJobLogDao().loadByJobIdAndDagRunRecord(jobLog) ;
        if(jobLog == null){
            return null;
        }
        if(jobLog.getExecutorAddress() == null || "".equals(jobLog.getExecutorAddress())){
            return null;
        }
        ExecutorBiz executorBiz = new ExecutorBizClient(jobLog.getExecutorAddress(), "");

        return executorBiz ;
    }

    public static ReturnT killJob(XxlJobLog log){
        XxlJobInfo jobInfo = XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().loadById(log.getJobId());
        if (jobInfo==null) {
            return new ReturnT<String>(500, I18nUtil.getString("jobinfo_glue_jobid_unvalid"));
        }
        if (ReturnT.SUCCESS_CODE != log.getTriggerCode()) {
            return new ReturnT<String>(500, I18nUtil.getString("joblog_kill_log_limit"));
        }

        // request of kill
        ReturnT<String> runResult = null;
        try {
            ExecutorBiz executorBiz = XxlJobScheduler.getExecutorBiz(log.getExecutorAddress());
            runResult = executorBiz.kill(new KillParam(jobInfo.getId()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            runResult = new ReturnT<String>(500, e.getMessage());
        }

        return  runResult ;
    }

}
