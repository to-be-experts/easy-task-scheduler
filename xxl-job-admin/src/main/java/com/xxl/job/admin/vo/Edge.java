package com.xxl.job.admin.vo;

import java.io.Serializable;

/**
 * 边基本信息
 */
public class Edge implements Serializable {
    private String from ;
    private String to ;
    private String d ;
    private String id ;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
