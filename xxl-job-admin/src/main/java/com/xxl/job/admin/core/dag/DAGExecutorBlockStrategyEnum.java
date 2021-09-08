package com.xxl.job.admin.core.dag;

import com.xxl.job.admin.core.route.ExecutorRouteStrategyEnum;
import com.xxl.job.admin.core.route.ExecutorRouter;
import com.xxl.job.admin.core.route.strategy.*;
import com.xxl.job.admin.core.util.I18nUtil;

public enum DAGExecutorBlockStrategyEnum {

    DISCARD_LATER(I18nUtil.getString("jobconf_block_DISCARD_LATER")),
    SERIAL_EXECUTION(I18nUtil.getString("jobconf_block_SERIAL_EXECUTION")),
    COVER_EARLY(I18nUtil.getString("jobconf_block_CONCURRENT_EXECUTION")), ;

    DAGExecutorBlockStrategyEnum(String title) {
        this.title = title;
    }

    private String title;
    private ExecutorRouter router;

    public String getTitle() {
        return title;
    }
    public ExecutorRouter getRouter() {
        return router;
    }

    public static DAGExecutorBlockStrategyEnum match(String name, DAGExecutorBlockStrategyEnum defaultItem){
        if (name != null) {
            for (DAGExecutorBlockStrategyEnum item: DAGExecutorBlockStrategyEnum.values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
        }
        return defaultItem;
    }
}
