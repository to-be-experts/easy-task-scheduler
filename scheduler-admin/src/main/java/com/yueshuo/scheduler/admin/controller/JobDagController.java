package com.yueshuo.scheduler.admin.controller;


import com.yueshuo.scheduler.admin.core.complete.XxlJobCompleter;
import com.yueshuo.scheduler.admin.core.conf.XxlJobAdminConfig;
import com.yueshuo.scheduler.admin.core.dag.DAGExecutorBlockStrategyEnum;
import com.yueshuo.scheduler.admin.core.dag.DAGTaskHandlerHelper;
import com.yueshuo.scheduler.admin.core.route.ExecutorRouteStrategyEnum;
import com.yueshuo.scheduler.admin.core.scheduler.MisfireStrategyEnum;
import com.yueshuo.scheduler.admin.core.scheduler.ScheduleTypeEnum;
import com.yueshuo.scheduler.admin.core.util.I18nUtil;
import com.yueshuo.scheduler.admin.service.PstDagJobService;
import com.yueshuo.scheduler.admin.vo.JobDagInfoVO;
import com.yueshuo.scheduler.admin.vo.Node;
import com.yueshuo.scheduler.admin.core.model.PstDagJobInfo;
import com.yueshuo.scheduler.admin.core.model.PstDagJobRunRecordInfo;
import com.yueshuo.scheduler.admin.core.model.XxlJobLog;
import com.yueshuo.scheduler.core.biz.model.ReturnT;
import com.yueshuo.scheduler.core.glue.GlueTypeEnum;
import com.yueshuo.scheduler.core.util.GsonTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dagjob")
public class JobDagController {
    private static Logger logger = LoggerFactory.getLogger(JobDagController.class);
    @Resource
    private PstDagJobService dagJobService;

    @RequestMapping
    public String index(HttpServletRequest request, Model model, @RequestParam(required = false, defaultValue = "-1") int jobGroup) {

        // 枚举-字典
        model.addAttribute("ExecutorRouteStrategyEnum", ExecutorRouteStrategyEnum.values());	    // 路由策略-列表
        model.addAttribute("GlueTypeEnum", GlueTypeEnum.values());								// Glue类型-字典
        model.addAttribute("ExecutorBlockStrategyEnum", DAGExecutorBlockStrategyEnum.values());	    // 阻塞处理策略-字典
        model.addAttribute("ScheduleTypeEnum", ScheduleTypeEnum.values());	    				// 调度类型
        model.addAttribute("MisfireStrategyEnum", MisfireStrategyEnum.values());	    			// 调度过期策略

        model.addAttribute("JobGroupList", new ArrayList<>());
        model.addAttribute("jobGroup", jobGroup);

        return "jobdag/jobdag.index";
    }

    @RequestMapping("/detail")
    public String detail(HttpServletRequest request, Model model, @RequestParam(required = false, defaultValue = "-1") long jobId, @RequestParam(required = false, defaultValue = "调度") String jobName) {
        // 枚举-字典
        //logger.info(request.getLocalAddr());
        logger.info(request.getServerName());
        logger.info(request.getServerPort()+"");
        logger.info(request.getContextPath());
        logger.info(request.getRequestURL().toString());
        model.addAttribute("jobId", jobId);
        model.addAttribute("jobName", jobName);
        model.addAttribute("hostPath", request.getServerName() +":"+request.getServerPort()+"/"+request.getContextPath());
        PstDagJobInfo dagJobInfo = dagJobService.loadById(jobId) ;

        if(dagJobInfo.getStatus() == 0){
            model.addAttribute("status", "未调度");
        }
        if(dagJobInfo.getStatus() == 1){
            model.addAttribute("status", "调度中");
        }

        return "jobdag/jobdag.view";
    }

    @RequestMapping("/loadRunDataTimeLimit")
    @ResponseBody
    public ReturnT<JobDagInfoVO> loadRunDataTimeLimit5(long id, int size) {
        List<PstDagJobRunRecordInfo> records = dagJobService.loadRunDataTimeByDagJobId(id,size) ;

        return new ReturnT(records);
    }

    @RequestMapping("/getById")
    @ResponseBody
    public ReturnT<JobDagInfoVO> getById(long id) {
        PstDagJobInfo dagJobInfo = dagJobService.loadById(id) ;
        JobDagInfoVO jobDag = GsonTool.fromJson(dagJobInfo.getDagInfo(),JobDagInfoVO.class) ;

        return new ReturnT(jobDag);
    }

    @RequestMapping("/pageList")
    @ResponseBody
    public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
                                        @RequestParam(required = false, defaultValue = "10") int length,
                                        String jobName, String jobDesc, int status) {

        return dagJobService.pageList(start, length, jobName, jobDesc, status );
    }
    @RequestMapping("/add")
    @ResponseBody
    public ReturnT<String> add(PstDagJobInfo jobInfo) {
        return dagJobService.add(jobInfo);
    }

    @RequestMapping("/update")
    @ResponseBody
    public ReturnT<String> update(PstDagJobInfo jobInfo) {
        return dagJobService.update(jobInfo);
    }

    @RequestMapping("/remove")
    @ResponseBody
    public ReturnT<String> remove(int id) {
        return dagJobService.remove(id);
    }

    @RequestMapping("/stop")
    @ResponseBody
    public ReturnT<String> pause(int id) {
        return dagJobService.stop(id);
    }

    @RequestMapping("/start")
    @ResponseBody
    public ReturnT<String> start(int id) {
        return dagJobService.start(id);
    }

    @RequestMapping("/trigger")
    @ResponseBody
    public ReturnT<String> trigger(int id) {
        return dagJobService.trigger(id);
    }


    @RequestMapping("/triggerAgain")
    @ResponseBody
    public ReturnT<String> triggerAgain(int jobId,String record,int type) {
        return dagJobService.triggerAgain(jobId,record,type);
    }

    @RequestMapping("/triggerOneTime")
    @ResponseBody
    public ReturnT<String> triggerOneTime(long dagJobId ,int jobId,String record) {
        return dagJobService.triggerOneTime(dagJobId,jobId,record);
    }

    @RequestMapping("/toNextStep")
    @ResponseBody
    public ReturnT<String> toSkipCurrStep(int jobId,String record){
        return dagJobService.SkipCurrStep(jobId,record);
    }
    @RequestMapping("/kill")
    @ResponseBody
    public ReturnT<String> killNode(int jobId,String record){
        // base check
        XxlJobLog log = new XxlJobLog();
        log.setDagRunRecord(record);
        log.setJobId(jobId);

        log = XxlJobAdminConfig.getAdminConfig().getXxlJobLogDao().loadByJobIdAndDagRunRecord(log);

        ReturnT<String> runResult = DAGTaskHandlerHelper.killJob(log) ;

        if (ReturnT.SUCCESS_CODE == runResult.getCode()) {
            log.setHandleCode(ReturnT.FAIL_CODE);
            log.setHandleMsg( I18nUtil.getString("joblog_kill_log_byman")+":" + (runResult.getMsg()!=null?runResult.getMsg():""));
            log.setHandleTime(new Date());
            XxlJobCompleter.updateHandleInfoAndFinish(log);
            return new ReturnT<String>(runResult.getMsg());
        } else {
            return new ReturnT<String>(500, runResult.getMsg());
        }
    }



    @RequestMapping(value = "/updateDagInfo",method = RequestMethod.POST)
    @ResponseBody
    public ReturnT<String> updateDagInfo(@RequestBody JobDagInfoVO dagInfoVO) {
        PstDagJobInfo jobInfo = new PstDagJobInfo();

        jobInfo.setId(dagInfoVO.getDagJobId());
        for(Node node : dagInfoVO.getNodes()){
            node.setStatus(0);
        }
        jobInfo.setDagInfo(GsonTool.toJson(dagInfoVO));

        return dagJobService.updateDagInfo(jobInfo);
    }
}
