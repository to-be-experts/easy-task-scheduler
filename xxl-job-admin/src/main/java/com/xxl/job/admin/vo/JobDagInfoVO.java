package com.xxl.job.admin.vo;

import java.io.Serializable;
import java.util.List;

public class JobDagInfoVO implements Serializable {

    private long dagJobId ;
    private List<Node> nodes ;
    private List<Edge> links ;
    private int runStatus = -1;
    private int nodeUnRuning = 0 ;
    private int nodeRuning = 0 ;
    private int nodeRunEnd = 0 ;
    private int nodeRunErr = 0 ;
    private int nodeRunLose = 0 ;
    private int queueSize ;

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Edge> getLinks() {
        return links;
    }

    public void setLinks(List<Edge> links) {
        this.links = links;
    }

    public long getDagJobId() {
        return dagJobId;
    }

    public void setDagJobId(long dagJobId) {
        this.dagJobId = dagJobId;
    }

    public int getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(int runStatus) {
        this.runStatus = runStatus;
    }

    public int getNodeUnRuning() {
        return nodeUnRuning;
    }

    public void setNodeUnRuning(int nodeUnRuning) {
        this.nodeUnRuning = nodeUnRuning;
    }

    public int getNodeRuning() {
        return nodeRuning;
    }

    public void setNodeRuning(int nodeRuning) {
        this.nodeRuning = nodeRuning;
    }

    public int getNodeRunEnd() {
        return nodeRunEnd;
    }

    public void setNodeRunEnd(int nodeRunEnd) {
        this.nodeRunEnd = nodeRunEnd;
    }

    public int getNodeRunErr() {
        return nodeRunErr;
    }

    public void setNodeRunErr(int nodeRunErr) {
        this.nodeRunErr = nodeRunErr;
    }

    public int getNodeRunLose() {
        return nodeRunLose;
    }

    public void setNodeRunLose(int nodeRunLose) {
        this.nodeRunLose = nodeRunLose;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }
}
