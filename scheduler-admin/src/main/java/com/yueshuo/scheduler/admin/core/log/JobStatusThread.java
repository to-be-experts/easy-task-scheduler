package com.yueshuo.scheduler.admin.core.log;

import com.yueshuo.scheduler.admin.core.conf.XxlJobAdminConfig;
import com.yueshuo.scheduler.admin.core.dag.DAGQueueMgr;
import com.yueshuo.scheduler.admin.core.dag.DAGRunRecordCacheUtils;
import com.yueshuo.scheduler.admin.core.dag.DAGTaskHandlerHelper;
import com.yueshuo.scheduler.admin.core.model.PstDagJobInfo;
import com.yueshuo.scheduler.admin.core.model.PstDagJobRunRecordInfo;
import com.yueshuo.scheduler.admin.core.model.XxlJobInfo;
import com.yueshuo.scheduler.admin.vo.JobDagInfoVO;
import com.yueshuo.scheduler.admin.vo.Node;
import com.yueshuo.scheduler.core.util.GsonTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: jaytan
 * @Description: 读取状态内容，发送websocket
 * @Date: 2021/8/15
 */
public class JobStatusThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(JobStatusThread.class);
    private Session session;
    private PstDagJobInfo dagJobInfo ;
    private long recordTime ;

    public JobStatusThread(PstDagJobInfo dagJobInfo,long recordTime, Session session) {
        this.session = session;
        this.dagJobInfo = dagJobInfo ;
        this.recordTime = recordTime ;
    }

    @Override
    public void run() {
        while (true && session.isOpen()){

            try {
                JobDagInfoVO jobDag = GsonTool.fromJson(dagJobInfo.getDagInfo(),JobDagInfoVO.class) ;
                jobDag.setQueueSize(DAGQueueMgr.getQueueSize(dagJobInfo));
                // 将实时状态通过WebSocket发送给客户端，给每一行添加一个HTML换行
                //1、如果调度没有当前运行记录信息，则未调度，有则查询该调度记录的所有信息返回前台
                if(this.dagJobInfo.getCurrRunRecord() == null || "".equals(dagJobInfo.getCurrRunRecord())){
                    jobDag.setRunStatus(-1);//未运行
                }else{
                    if(dagJobInfo.getCurrRunRecord().equals(dagJobInfo.getLastRunRecord())){
                        jobDag.setRunStatus(dagJobInfo.getLastRunStatus()); //1、运行成功，2、运行失败
                    }else{
                        jobDag.setRunStatus(0);//运行中
                    }
                }
                List<PstDagJobRunRecordInfo> records = new ArrayList<>();
                if(recordTime <= 0 || "".equals(recordTime)){
                    records = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecord(dagJobInfo.getCurrRunRecord()) ;
                }else{
                    records = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByDataTimeAndDagJobId(this.recordTime,this.dagJobInfo.getId()) ;
                }

                int nodeUnRuning = 0 ;
                int nodeRuning = 0 ;
                int nodeRunEnd = 0 ;
                int nodeRunErr = 0 ;
                int nodeRunLose = 0 ;
                if(records != null && records.size() > 0){
                    for (Node node : jobDag.getNodes()){
                        for (PstDagJobRunRecordInfo record : records){
                            if(node.getTaskId() == record.getJobId()){
                                node.setStatus(record.getRunStatus());
                                node.setRecord(record.getRunRecord());

                                if(record.getRunStatus() == 1 && record.getDagTaskType() == 212){
                                    //看看缓存中是否有该记录，没有则可能由于服务异常中断导致任务丢失，前台可选择重新执行
                                    Object obj = DAGRunRecordCacheUtils.get(record.getRunRecord()+"-"+record.getJobId()) ;
                                    //缓存没有-执行器有
                                    if(obj == null && !DAGTaskHandlerHelper.checkDagJobExecIdle(Integer.parseInt(record.getJobId()+""))){
                                        node.setStatus(4); //任务丢失状态
                                    }
                                    //缓存有-执行器没有
                                    if(obj != null && DAGTaskHandlerHelper.checkDagJobExecIdle(Integer.parseInt(record.getJobId()+""))){
                                        node.setStatus(4); //任务丢失状态
                                    }
                                }
                                if(node.getStatus() == 0){
                                    nodeUnRuning ++ ;
                                }else if(node.getStatus() == 1){
                                    nodeRuning ++ ;
                                }else if(node.getStatus() == 2){
                                    nodeRunEnd ++ ;
                                }else if(node.getStatus() == 3){
                                    nodeRunErr ++ ;
                                }else if(node.getStatus() == 4){
                                    nodeRunLose ++ ;
                                }
                                break;
                            }
                        }
                    }
                }else {
                    for (Node node : jobDag.getNodes()) {
                        /*if(node.getDataId() == 101 || node.getDataId() == 102){
                            continue;
                        }*/
                        XxlJobInfo jobInfo = XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().loadById(node.getTaskId());
                        if (jobInfo != null && jobInfo.getDagRunRecord() != null && !"".equals(jobInfo.getDagRunRecord())) {
                            PstDagJobRunRecordInfo record = XxlJobAdminConfig.getAdminConfig().getRunRecordDao().loadByRunRecordAndJobId(jobInfo.getDagRunRecord(), jobInfo.getId()) ;
                            if (record != null) {
                                node.setStatus(record.getRunStatus());
                                node.setRecord(record.getRunRecord());

                                if (record.getRunStatus() == 1) {
                                    //看看缓存中是否有该记录，没有则可能由于服务异常中断导致任务丢失，前台可选择重新执行
                                    Object obj = DAGRunRecordCacheUtils.get(record.getRunRecord() + "-" + record.getJobId());
                                    //缓存没有-执行器有
                                    if (obj == null && !DAGTaskHandlerHelper.checkDagJobExecIdle(Integer.parseInt(record.getJobId() + ""))) {
                                        node.setStatus(4); //任务丢失状态
                                    }
                                    //缓存有-执行器没有
                                    if (obj != null && DAGTaskHandlerHelper.checkDagJobExecIdle(Integer.parseInt(record.getJobId() + ""))) {
                                        node.setStatus(4); //任务丢失状态
                                    }
                                }
                            }

                        }
                        if(node.getStatus() == 0){
                            nodeUnRuning ++ ;
                        }else if(node.getStatus() == 1){
                            nodeRuning ++ ;
                        }else if(node.getStatus() == 2){
                            nodeRunEnd ++ ;
                        }else if(node.getStatus() == 3){
                            nodeRunErr ++ ;
                        }else if(node.getStatus() == 4){
                            nodeRunLose ++ ;
                        }
                    }
                }
                jobDag.setNodeUnRuning(nodeUnRuning);
                jobDag.setNodeRuning(nodeRuning);
                jobDag.setNodeRunEnd(nodeRunEnd);
                jobDag.setNodeRunErr(nodeRunErr);
                jobDag.setNodeRunLose(nodeRunLose);

                session.getBasicRemote().sendText(GsonTool.toJson(jobDag));
                Thread.sleep(5000);
                this.dagJobInfo = XxlJobAdminConfig.getAdminConfig().getDagJobInfoDao().loadById(dagJobInfo.getId()) ;
            } catch (Exception e) {
                logger.info("send status text error ",e);
            }
        }

    }
}
