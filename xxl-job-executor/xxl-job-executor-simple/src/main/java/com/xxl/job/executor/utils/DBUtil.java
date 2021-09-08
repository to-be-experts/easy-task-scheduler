package com.xxl.job.executor.utils;

import org.neo4j.driver.*;
import org.neo4j.driver.Driver;

import java.sql.*;
import java.util.Map;

public class DBUtil {



    public static void close (Driver driver, Session session) {
        if (session != null) {
            session.close();
        }
        if (driver != null) {
            driver.close();
        }
    }

    public static void close (Driver...driver) {
        for (int i = 0; i < driver.length; i++) {
            if (driver[i] != null) {
                driver[i].close();
            }
        }
    }

    public static void close (Session...session) {
        for (int i = 0; i < session.length; i++) {
            if (session[i] != null) {
                session[i].close();
            }
        }
    }

    public static void close (Connection...conn) throws SQLException {
        for (int i = 0; i < conn.length; i++) {
            if (conn[i] != null) {
                conn[i].close();
            }
        }
    }

    public static void close (Statement...stat) throws SQLException {
        for (int i = 0; i < stat.length; i++) {
            if (stat[i] != null) {
                stat[i].close();
            }
        }
    }

    public static void close (PreparedStatement...pstat) throws SQLException {
        for (int i = 0; i < pstat.length; i++) {
            if (pstat[i] != null) {
                pstat[i].close();
            }
        }
    }

    public static void close (ResultSet...rs) throws SQLException {
        for (int i = 0; i < rs.length; i++) {
            if (rs[i] != null) {
                rs[i].close();
            }
        }
    }
}
