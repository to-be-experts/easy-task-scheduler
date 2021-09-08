package com.xxl.job.admin.vo;

import java.util.Date;

public class DAGJobInfoVO {
    private long id ; //ID 编号
    private String jobName ; //任务名称
    private String jobDesc ; //任务描述
    private String jobRunExp ; //运行表达式
    private int status ; //任务状态
    private String createBy ; //创建人
    private Date createTime ; //创建时间
    private String dagInfo ; //调度依赖信息

    private int lastRunStatus ; //最近一次运行状态
    private String lastRunRecord ; //最近一次运行记录
    private String lastRunMsg ; // 最近一次运行描述
    private long lastRunTime ; // 最近一次运行结束时间

    private long triggerLastTime;	// 上次调度时间
    private long triggerNextTime;	// 下次调度时间
    private String currRunRecord ; //当前运行记录
    private String executorBlockStrategy ; //阻塞执行策略

    private int unRuning ;
    private int runing ;
    private int runErr ;
    private int runOk ;
    private int nodeTotal ;
    private int queueSize ;


    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public String getExecutorBlockStrategy() {
        return executorBlockStrategy;
    }

    public void setExecutorBlockStrategy(String executorBlockStrategy) {
        this.executorBlockStrategy = executorBlockStrategy;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobDesc() {
        return jobDesc;
    }

    public void setJobDesc(String jobDesc) {
        this.jobDesc = jobDesc;
    }

    public String getJobRunExp() {
        return jobRunExp;
    }

    public void setJobRunExp(String jobRunExp) {
        this.jobRunExp = jobRunExp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getDagInfo() {
        return dagInfo;
    }

    public void setDagInfo(String dagInfo) {
        this.dagInfo = dagInfo;
    }

    public int getLastRunStatus() {
        return lastRunStatus;
    }

    public void setLastRunStatus(int lastRunStatus) {
        this.lastRunStatus = lastRunStatus;
    }

    public String getLastRunRecord() {
        return lastRunRecord;
    }

    public void setLastRunRecord(String lastRunRecord) {
        this.lastRunRecord = lastRunRecord;
    }

    public String getLastRunMsg() {
        return lastRunMsg;
    }

    public void setLastRunMsg(String lastRunMsg) {
        this.lastRunMsg = lastRunMsg;
    }

    public long getLastRunTime() {
        return lastRunTime;
    }

    public void setLastRunTime(long lastRunTime) {
        this.lastRunTime = lastRunTime;
    }

    public long getTriggerLastTime() {
        return triggerLastTime;
    }

    public void setTriggerLastTime(long triggerLastTime) {
        this.triggerLastTime = triggerLastTime;
    }

    public long getTriggerNextTime() {
        return triggerNextTime;
    }

    public void setTriggerNextTime(long triggerNextTime) {
        this.triggerNextTime = triggerNextTime;
    }

    public String getCurrRunRecord() {
        return currRunRecord;
    }

    public void setCurrRunRecord(String currRunRecord) {
        this.currRunRecord = currRunRecord;
    }

    public int getUnRuning() {
        return unRuning;
    }

    public void setUnRuning(int unRuning) {
        this.unRuning = unRuning;
    }

    public int getRuning() {
        return runing;
    }

    public void setRuning(int runing) {
        this.runing = runing;
    }

    public int getRunErr() {
        return runErr;
    }

    public void setRunErr(int runErr) {
        this.runErr = runErr;
    }

    public int getRunOk() {
        return runOk;
    }

    public void setRunOk(int runOk) {
        this.runOk = runOk;
    }

    public int getNodeTotal() {
        return nodeTotal;
    }

    public void setNodeTotal(int nodeTotal) {
        this.nodeTotal = nodeTotal;
    }
}
