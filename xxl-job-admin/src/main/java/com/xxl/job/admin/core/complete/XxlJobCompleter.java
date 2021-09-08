package com.xxl.job.admin.core.complete;

import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.dag.DAGTaskHandlerHelper;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobLog;
import com.xxl.job.admin.core.thread.JobTriggerPoolHelper;
import com.xxl.job.admin.core.trigger.TriggerTypeEnum;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.util.XxlJobRemotingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * @author xuxueli 2020-10-30 20:43:10
 */
public class XxlJobCompleter {
    private static Logger logger = LoggerFactory.getLogger(XxlJobCompleter.class);

    /**
     * common fresh handle entrance (limit only once)
     *
     * @param xxlJobLog
     * @return
     */
    public static int updateHandleInfoAndFinish(XxlJobLog xxlJobLog) {

        // finish
        //finishJob(xxlJobLog);
        //finishJobWithDag(xxlJobLog);
        finishJobWithDagByLog(xxlJobLog) ;
        // text最大64kb 避免长度过长
        if (xxlJobLog.getHandleMsg().length() > 15000) {
            xxlJobLog.setHandleMsg( xxlJobLog.getHandleMsg().substring(0, 15000) );
        }
        if(xxlJobLog.getHandleMod() > 0){
            xxlJobLog.setHandleMod(0);
        }

        // fresh handle
        return XxlJobAdminConfig.getAdminConfig().getXxlJobLogDao().updateHandleInfo(xxlJobLog);
    }

    private static void finishJobWithDag(XxlJobLog xxlJobLog){
        XxlJobInfo xxlJobInfo = XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().loadById(xxlJobLog.getJobId());

        logger.info("======job finish job id is {},DAG run record is {},run handle code is {}  =====",xxlJobLog.getJobId(),(xxlJobInfo != null && xxlJobInfo.getDagRunRecord() != null && !"".equals(xxlJobInfo.getDagRunRecord())) ? xxlJobInfo.getDagRunRecord():"NULL",xxlJobLog.getHandleCode());
        // 1、handle success, to trigger child job
        String triggerChildMsg = null;
        if(xxlJobInfo != null && xxlJobInfo.getDagRunRecord() != null && !"".equals(xxlJobInfo.getDagRunRecord())){
            logger.info("========================= JOB RUN IN DAG TASK =========================");
            //有DAG运行记录，校验是否可以运行后向依赖节点任务5
            if (XxlJobContext.HANDLE_COCE_SUCCESS == xxlJobLog.getHandleCode() || (XxlJobContext.HANDLE_COCE_FAIL == xxlJobLog.getHandleCode() && xxlJobLog.getHandleMod() == 2)) {
                DAGTaskHandlerHelper.handlerRunNextNodeTask(xxlJobInfo);
                triggerChildMsg = "DAG 调度运行 ，DAG 记录 "+xxlJobInfo.getDagRunRecord() ;
            }else{
                DAGTaskHandlerHelper.handlerRunCurrNodeError(xxlJobInfo);
                triggerChildMsg = "DAG 调度运行 JOB 记录失败 ，DAG 记录 "+xxlJobInfo.getDagRunRecord() ;
            }
        }else{
            if (XxlJobContext.HANDLE_COCE_SUCCESS == xxlJobLog.getHandleCode()) {
                logger.info("========================= JOB RUN OUT DAG TASK =========================");
                if (xxlJobInfo!=null && xxlJobInfo.getChildJobId()!=null && xxlJobInfo.getChildJobId().trim().length()>0) {
                    triggerChildMsg = "<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>"+ I18nUtil.getString("jobconf_trigger_child_run") +"<<<<<<<<<<< </span><br>";
                    String[] childJobIds = xxlJobInfo.getChildJobId().split(",");
                    for (int i = 0; i < childJobIds.length; i++) {
                        int childJobId = (childJobIds[i]!=null && childJobIds[i].trim().length()>0 && isNumeric(childJobIds[i]))?Integer.valueOf(childJobIds[i]):-1;
                        if (childJobId > 0) {
                            JobTriggerPoolHelper.trigger(childJobId, TriggerTypeEnum.PARENT, -1, null, null, null,"");
                            ReturnT<String> triggerChildResult = ReturnT.SUCCESS;
                            // add msg
                            triggerChildMsg += MessageFormat.format(I18nUtil.getString("jobconf_callback_child_msg1"),
                                    (i+1),
                                    childJobIds.length,
                                    childJobIds[i],
                                    (triggerChildResult.getCode()==ReturnT.SUCCESS_CODE?I18nUtil.getString("system_success"):I18nUtil.getString("system_fail")),
                                    triggerChildResult.getMsg());
                        } else {
                            triggerChildMsg += MessageFormat.format(I18nUtil.getString("jobconf_callback_child_msg2"),
                                    (i+1),
                                    childJobIds.length,
                                    childJobIds[i]);
                        }
                    }
                }
            }
        }

        if (triggerChildMsg != null) {
            /*if(xxlJobInfo != null && xxlJobInfo.getDagRunRecord() != null && !"".equals(xxlJobInfo.getDagRunRecord())){
                xxlJobLog.setExecutorShardingParam(xxlJobInfo.getDagRunRecord());
            }*/
            xxlJobLog.setHandleMsg( xxlJobLog.getHandleMsg() + triggerChildMsg );
        }

        // 2、fix_delay trigger next
        // on the way

    }

    private static void finishJobWithDagByLog(XxlJobLog xxlJobLog){
        logger.info("======job finish job id is {},DAG run record is {},run handle code is {}  =====",xxlJobLog.getJobId(),(xxlJobLog != null && xxlJobLog.getDagRunRecord() != null && !"".equals(xxlJobLog.getDagRunRecord())) ? xxlJobLog.getDagRunRecord():"NULL",xxlJobLog.getHandleCode());
        // 1、handle success, to trigger child job
        String triggerChildMsg = null;
        if(xxlJobLog != null && xxlJobLog.getDagRunRecord() != null && !"".equals(xxlJobLog.getDagRunRecord())){
            logger.info("========================= JOB RUN IN DAG TASK =========================");
            //有DAG运行记录，校验是否可以运行后向依赖节点任务5
            if (XxlJobContext.HANDLE_COCE_SUCCESS == xxlJobLog.getHandleCode() || (XxlJobContext.HANDLE_COCE_FAIL == xxlJobLog.getHandleCode() && xxlJobLog.getHandleMod() == 2)) {
                DAGTaskHandlerHelper.handlerRunNextNodeTask(xxlJobLog);
                triggerChildMsg = "DAG 调度运行 ，DAG 记录 "+xxlJobLog.getDagRunRecord() ;
            }else{
                DAGTaskHandlerHelper.handlerRunCurrNodeError(xxlJobLog);
                triggerChildMsg = "DAG 调度运行 JOB 记录失败 ，DAG 记录 "+xxlJobLog.getDagRunRecord() ;
            }
        }else{
            if (XxlJobContext.HANDLE_COCE_SUCCESS == xxlJobLog.getHandleCode()) {
                XxlJobInfo xxlJobInfo = XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().loadById(xxlJobLog.getJobId());
                logger.info("========================= JOB RUN OUT DAG TASK =========================");
                if (xxlJobInfo!=null && xxlJobInfo.getChildJobId()!=null && xxlJobInfo.getChildJobId().trim().length()>0) {
                    triggerChildMsg = "<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>"+ I18nUtil.getString("jobconf_trigger_child_run") +"<<<<<<<<<<< </span><br>";
                    String[] childJobIds = xxlJobInfo.getChildJobId().split(",");
                    for (int i = 0; i < childJobIds.length; i++) {
                        int childJobId = (childJobIds[i]!=null && childJobIds[i].trim().length()>0 && isNumeric(childJobIds[i]))?Integer.valueOf(childJobIds[i]):-1;
                        if (childJobId > 0) {
                            JobTriggerPoolHelper.trigger(childJobId, TriggerTypeEnum.PARENT, -1, null, null, null,"");
                            ReturnT<String> triggerChildResult = ReturnT.SUCCESS;
                            // add msg
                            triggerChildMsg += MessageFormat.format(I18nUtil.getString("jobconf_callback_child_msg1"),
                                    (i+1),
                                    childJobIds.length,
                                    childJobIds[i],
                                    (triggerChildResult.getCode()==ReturnT.SUCCESS_CODE?I18nUtil.getString("system_success"):I18nUtil.getString("system_fail")),
                                    triggerChildResult.getMsg());
                        } else {
                            triggerChildMsg += MessageFormat.format(I18nUtil.getString("jobconf_callback_child_msg2"),
                                    (i+1),
                                    childJobIds.length,
                                    childJobIds[i]);
                        }
                    }
                }
            }
        }

        if (triggerChildMsg != null) {
            /*if(xxlJobInfo != null && xxlJobInfo.getDagRunRecord() != null && !"".equals(xxlJobInfo.getDagRunRecord())){
                xxlJobLog.setExecutorShardingParam(xxlJobInfo.getDagRunRecord());
            }*/
            xxlJobLog.setHandleMsg( xxlJobLog.getHandleMsg() + triggerChildMsg );
        }

        // 2、fix_delay trigger next
        // on the way

    }

    /**
     * do somethind to finish job
     */
    private static void finishJob(XxlJobLog xxlJobLog){
        // 1、handle success, to trigger child job
        String triggerChildMsg = null;
        if (XxlJobContext.HANDLE_COCE_SUCCESS == xxlJobLog.getHandleCode()) {
            XxlJobInfo xxlJobInfo = XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().loadById(xxlJobLog.getJobId());
            if (xxlJobInfo!=null && xxlJobInfo.getChildJobId()!=null && xxlJobInfo.getChildJobId().trim().length()>0) {
                triggerChildMsg = "<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>"+ I18nUtil.getString("jobconf_trigger_child_run") +"<<<<<<<<<<< </span><br>";
                String[] childJobIds = xxlJobInfo.getChildJobId().split(",");
                for (int i = 0; i < childJobIds.length; i++) {
                    int childJobId = (childJobIds[i]!=null && childJobIds[i].trim().length()>0 && isNumeric(childJobIds[i]))?Integer.valueOf(childJobIds[i]):-1;
                    if (childJobId > 0) {
                        JobTriggerPoolHelper.trigger(childJobId, TriggerTypeEnum.PARENT, -1, null, null, null,"");
                        ReturnT<String> triggerChildResult = ReturnT.SUCCESS;

                        // add msg
                        triggerChildMsg += MessageFormat.format(I18nUtil.getString("jobconf_callback_child_msg1"),
                                (i+1),
                                childJobIds.length,
                                childJobIds[i],
                                (triggerChildResult.getCode()==ReturnT.SUCCESS_CODE?I18nUtil.getString("system_success"):I18nUtil.getString("system_fail")),
                                triggerChildResult.getMsg());
                    } else {
                        triggerChildMsg += MessageFormat.format(I18nUtil.getString("jobconf_callback_child_msg2"),
                                (i+1),
                                childJobIds.length,
                                childJobIds[i]);
                    }
                }

            }
        }

        if (triggerChildMsg != null) {
            xxlJobLog.setHandleMsg( xxlJobLog.getHandleMsg() + triggerChildMsg );
        }

        // 2、fix_delay trigger next
        // on the way

    }

    private static boolean isNumeric(String str){
        try {
            int result = Integer.valueOf(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
