package com.yueshuo.scheduler.admin.core.alarm;

import com.yueshuo.scheduler.admin.core.model.XxlJobInfo;
import com.yueshuo.scheduler.admin.core.model.XxlJobLog;

/**
 * @author xuxueli 2020-01-19
 */
public interface JobAlarm {

    /**
     * job alarm
     *
     * @param info
     * @param jobLog
     * @return
     */
    public boolean doAlarm(XxlJobInfo info, XxlJobLog jobLog);

}
