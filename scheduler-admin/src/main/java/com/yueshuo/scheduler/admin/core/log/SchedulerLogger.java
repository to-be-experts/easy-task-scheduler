package com.yueshuo.scheduler.admin.core.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.OutputStreamAppender;
import com.google.common.collect.Lists;
import com.xxl.job.core.util.DateUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;


/**
 * @Author: TheBigBlue
 * @Description: 每次调度一个日志
 * @Date: 2020/6/20
 */
@Component
public class SchedulerLogger {

    private static final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    @Value("${logger.level}")
    private String loggerLevel;

    /**
     * @Author: TheBigBlue
     * @Description: 获取logger
     * @Date: 2020/6/21
     * @Param: [logName]
     * @Return:
     **/
    public synchronized Logger getLogger(String logName) {
        Logger rootLogger = context.getLogger(logName);
        //如果没有appender，新增，如果存在，则直接返回
        if (!rootLogger.iteratorForAppenders().hasNext()) {
            rootLogger.setLevel(Level.toLevel(loggerLevel));
            rootLogger.setAdditive(false);
            addAppenders(logName).forEach(appdender -> {
                rootLogger.addAppender(appdender);
            });
        }
        return rootLogger;
    }

    /**
     * @Author: TheBigBlue
     * @Description: 添加fileAppender、consoleAppender
     * @Date: 2020/6/21
     * @Param logName:
     * @Return:
     **/
    public List<OutputStreamAppender<ILoggingEvent>> addAppenders(String logName) {
        //设置fileAppender
        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setAppend(true);
        fileAppender.setName("FILE");
        String dateStr = DateUtil.format(new Date(),"yyyyMMdd");
        String logPath = new StringBuilder(System.getProperty("user.dir")).append("/logs/")
                .append(dateStr).append("/").append(logName).append(".log").toString();
        fileAppender.setFile(logPath);
        //设置consoleAppender
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setName("CONSOLE");
        //内容配置
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n");
        encoder.setCharset(Charset.forName("UTF-8"));
        encoder.setContext(context);
        encoder.start();
        //启动appender线程
        fileAppender.setEncoder(encoder);
        fileAppender.start();
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();
        return Lists.newArrayList(fileAppender, consoleAppender);
    }

    /**
     * @Author: TheBigBlue
     * @Description: 关闭appender线程，移除logger
     * @Date: 2020/6/21
     * @Param: [logName]
     * @Return:
     **/
    public void removeLogger(String logName) {
        context.getLogger(logName).detachAndStopAllAppenders();
        context.removeObject(logName);
    }
}
