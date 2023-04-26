package com.yueshuo.scheduler.admin.core.dag;

import com.yueshuo.scheduler.admin.core.route.ExecutorRouter;
import com.yueshuo.scheduler.admin.core.util.I18nUtil;

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
