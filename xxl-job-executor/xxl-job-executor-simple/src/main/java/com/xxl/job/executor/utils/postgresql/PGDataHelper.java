package com.xxl.job.executor.utils.postgresql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PGDataHelper {
    private static Logger logger = LoggerFactory.getLogger(PGDataHelper.class);

    public static Connection NewConnection(String user, String password,String url,String driver)
    {
        Connection conn=null;
        try {
            Class.forName( driver ).newInstance();
            conn= DriverManager.getConnection(url, user , password );
        } catch (InstantiationException e) {
            logger.info("获取 ==postgresql== 数据库连接失败",e);
            throw new RuntimeException("获取 ==postgresql== 数据库连接失败") ;
        } catch (IllegalAccessException e) {
            logger.info("获取 ==postgresql== 数据库连接失败",e);
            throw new RuntimeException("获取 ==postgresql== 数据库连接失败") ;
        } catch (ClassNotFoundException e) {
            logger.info("获取 ==postgresql== 数据库连接失败",e);
            throw new RuntimeException("获取 ==postgresql== 数据库连接失败") ;
        } catch (SQLException e) {
            logger.info("获取 ==postgresql== 数据库连接失败",e);
            throw new RuntimeException("获取 ==postgresql== 数据库连接失败") ;
        }
        return conn;
    }

}
