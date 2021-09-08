package com.xxl.job.admin.controller.websocket;

import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.log.JobStatusThread;
import com.xxl.job.admin.core.log.TailLogThread;
import com.xxl.job.admin.core.model.PstDagJobInfo;
import com.xxl.job.core.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.xxl.job.admin.core.conf.XxlJobAdminConfig.getAdminConfig;

/**
 * @Author: TheBigBlue
 * @Description: 向web端实时推送信息
 * @Date: 2019/7/16
 **/
@Component
@ServerEndpoint(value = "/ws/status/{dagJobId}/{recordTime}")
public class WSStatusController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WSStatusController.class);

    private ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private Process process;

    /**
     * 新的WebSocket请求开启
     */
    @OnOpen
    public void onOpen(@PathParam("dagJobId") String jobId,@PathParam("recordTime") long recordTime, Session session) {
        LOGGER.info("[{}-{}]加入连接!", jobId,recordTime);
        try {
            PstDagJobInfo dagJobInfo = XxlJobAdminConfig.getAdminConfig().getDagJobInfoDao().loadById(Long.parseLong(jobId)) ;
            pool.submit(new JobStatusThread(dagJobInfo,recordTime, session));
        } catch (Exception e) {
            LOGGER.error("[{}-{}]获取状态内容失败。", jobId,recordTime, e);
        }
    }

    /**
     * WebSocket请求关闭
     */
    @OnClose
    public void onClose(@PathParam("dagJobId") String jobId,@PathParam("recordTime") long recordTime, Session session) {
        LOGGER.info("[" + jobId + "]-{} 断开连接!",recordTime);
        try {
            session.close();
        } catch (Exception e) {
            LOGGER.info("关闭 websocket 连接异常 ",e);
        }
        if (process != null){
            process.destroy();
        }

    }

    @OnMessage
    public void onMessage(@PathParam("dagJobId") String jobId,String message) {
        LOGGER.info(" jobId {} process  msg is {} ",jobId,message);
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        thr.printStackTrace();
    }
}
