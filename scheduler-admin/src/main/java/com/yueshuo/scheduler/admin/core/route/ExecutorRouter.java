package com.yueshuo.scheduler.admin.core.route;

import com.yueshuo.scheduler.core.biz.model.ReturnT;
import com.yueshuo.scheduler.core.biz.model.TriggerParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by xuxueli on 17/3/10.
 */
public abstract class ExecutorRouter {
    protected static Logger logger = LoggerFactory.getLogger(ExecutorRouter.class);

    /**
     * route address
     *
     * @param addressList
     * @return  ReturnT.content=address
     */
    public abstract ReturnT<String> route(TriggerParam triggerParam, List<String> addressList);

}
