package com.xxl.job.admin.core.dag;

import com.xxl.job.admin.core.model.PstDagJobInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DAG 调度排队执行 维护
 * 每个调度一个队列，放到map中记录
 */
public class DAGQueueMgr {
    private volatile static Map<String, Queue<PstDagJobInfo>> queueData = new ConcurrentHashMap<>();

    public static boolean put2Queue(PstDagJobInfo dagJobInfo) {
        Queue<PstDagJobInfo> queues = queueData.get(dagJobInfo.getId()+"") ;
        if(queues == null){
            queues = new LinkedList<PstDagJobInfo>();
        }
        boolean flag = queues.offer(dagJobInfo) ;
        queueData.put(dagJobInfo.getId()+"",queues) ;

        return  flag ;
    }

    public static PstDagJobInfo getInQueue(PstDagJobInfo dagJobInfo) {
        Queue<PstDagJobInfo> queues = queueData.get(dagJobInfo.getId()+"") ;
        if(queues == null || queues.size() <= 0){
            return null ;
        }
        return queues.poll() ;
    }

    public static int getQueueSize(PstDagJobInfo dagJobInfo) {
        Queue<PstDagJobInfo> queues = queueData.get(dagJobInfo.getId()+"") ;
        if(queues == null || queues.size() <= 0){
            return 0 ;
        }
        return queues.size() ;
    }
}
