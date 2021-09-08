package com.xxl.job.admin.core.model;

import java.util.Date;

public class PstDagJobRunRecordInfo {
    private long id ; //ID 编号
    private long dagJobId;
    private String runRecord;
    private long jobId;
    private int runMod ;
    private int runStatus;
    private Date runStartTime;
    private Date runEndTime;
    private int dagTaskType;
    private long startUpJobId;
    private Date createTime ;
    private long runDataTime ;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDagJobId() {
        return dagJobId;
    }

    public void setDagJobId(long dagJobId) {
        this.dagJobId = dagJobId;
    }

    public String getRunRecord() {
        return runRecord;
    }

    public void setRunRecord(String runRecord) {
        this.runRecord = runRecord;
    }

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public int getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(int runStatus) {
        this.runStatus = runStatus;
    }

    public Date getRunStartTime() {
        return runStartTime;
    }

    public void setRunStartTime(Date runStartTime) {
        this.runStartTime = runStartTime;
    }

    public Date getRunEndTime() {
        return runEndTime;
    }

    public void setRunEndTime(Date runEndTime) {
        this.runEndTime = runEndTime;
    }

    public int getDagTaskType() {
        return dagTaskType;
    }

    public void setDagTaskType(int dagTaskType) {
        this.dagTaskType = dagTaskType;
    }

    public long getStartUpJobId() {
        return startUpJobId;
    }

    public void setStartUpJobId(long startUpJobId) {
        this.startUpJobId = startUpJobId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getRunMod() {
        return runMod;
    }

    public void setRunMod(int runMod) {
        this.runMod = runMod;
    }

    public long getRunDataTime() {
        return runDataTime;
    }

    public void setRunDataTime(long runDataTime) {
        this.runDataTime = runDataTime;
    }
}
