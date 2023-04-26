package com.yueshuo.scheduler.admin.controller.websocket;

import com.yueshuo.scheduler.admin.core.log.TailLogThread;
import com.xxl.job.core.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: TheBigBlue
 * @Description: 向web端实时推送信息
 * @Date: 2019/7/16
 **/
@Component
@ServerEndpoint(value = "/ws/logs/{dagJobId}")
public class WSLogController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WSLogController.class);

    private ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private Process process;
    private InputStream inputStream;

    /**
     * 新的WebSocket请求开启
     */
    @OnOpen
    public void onOpen(@PathParam("dagJobId") String jobId, Session session) {

        LOGGER.info("[{}]加入连接!", jobId);
        try {
            String dateStr = DateUtil.format(new Date(),"yyyyMMdd");
            String filePath = new StringBuilder(System.getProperty("user.dir")).append("/logs/").append(dateStr).append("/").append(jobId).append(".log").toString();
            // 执行tail -f命令，由于是tail命令，需要在linux系统执行
//            process = Runtime.getRuntime().exec("tail -f " + filePath);
//            inputStream = process.getInputStream();
            inputStream = new FileInputStream(filePath);
            // 一定要启动新的线程，防止InputStream阻塞处理WebSocket的线程
            pool.submit(new TailLogThread(inputStream, session));
//            new TailLogThread(inputStream, session).start();
        } catch (IOException e) {
            LOGGER.error("[{}]获取日志内容失败。", jobId, e);
            onClose(jobId);
        }
    }

    /**
     * WebSocket请求关闭
     */
    @OnClose
    public void onClose(@PathParam("jobId") String jobId) {
        LOGGER.info("[" + jobId + "]断开连接!");
        try {
            if (inputStream != null)
                inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (process != null)
            process.destroy();
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
