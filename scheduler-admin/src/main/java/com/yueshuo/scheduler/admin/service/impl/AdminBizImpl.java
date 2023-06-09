package com.yueshuo.scheduler.admin.service.impl;

import com.yueshuo.scheduler.admin.core.thread.JobCompleteHelper;
import com.yueshuo.scheduler.admin.core.thread.JobRegistryHelper;
import com.yueshuo.scheduler.core.biz.AdminBiz;
import com.yueshuo.scheduler.core.biz.model.HandleCallbackParam;
import com.yueshuo.scheduler.core.biz.model.RegistryParam;
import com.yueshuo.scheduler.core.biz.model.ReturnT;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xuxueli 2017-07-27 21:54:20
 */
@Service
public class AdminBizImpl implements AdminBiz {


    @Override
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList) {
        return JobCompleteHelper.getInstance().callback(callbackParamList);
    }

    @Override
    public ReturnT<String> registry(RegistryParam registryParam) {
        return JobRegistryHelper.getInstance().registry(registryParam);
    }

    @Override
    public ReturnT<String> registryRemove(RegistryParam registryParam) {
        return JobRegistryHelper.getInstance().registryRemove(registryParam);
    }

}
