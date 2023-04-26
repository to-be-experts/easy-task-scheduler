<!DOCTYPE html>
<html>
<head>
    <#import "../common/common.macro.ftl" as netCommon>
    <@netCommon.commonStyle />
    <!-- DataTables -->
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/datatables.net-bs/css/dataTables.bootstrap.min.css">
    <link  rel="stylesheet" href="${request.contextPath}/static/css/dag-index.css">
    <title>${I18n.admin_name}</title>
</head>
<body class="hold-transition skin-blue sidebar-mini <#if cookieMap?exists && cookieMap["xxljob_adminlte_settings"]?exists && "off" == cookieMap["xxljob_adminlte_settings"].value >sidebar-collapse</#if>">
<div class="wrapper">
    <!-- header -->
    <@netCommon.commonHeader />
    <!-- left -->
    <@netCommon.commonLeft "dagjob" />

    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <!-- Content Header (Page header) -->
        <section class="content-header">
            <h1>${I18n.dag_job_name}</h1>
        </section>

        <!-- Main content -->
        <section class="content">

            <div class="row">
                <div class="col-xs-1">
                    <div class="input-group">
                        <select class="form-control" id="status" >
                            <option value="-1" >${I18n.system_all}</option>
                            <option value="0" >${I18n.jobinfo_opt_stop}</option>
                            <option value="1" >${I18n.jobinfo_opt_start}</option>
                        </select>
                    </div>
                </div>
                <div class="col-xs-2">
                    <div class="input-group">
                        <input type="text" class="form-control" id="jobName" placeholder="${I18n.system_please_input} 调度名称" >
                    </div>
                </div>
                <#--<div class="col-xs-2">
                    <div class="input-group">
                        <input type="text" class="form-control" id="jobDesc" placeholder="${I18n.system_please_input} 任务描述" >
                    </div>
                </div>-->

                <div class="col-xs-2"> </div>

                <div class="col-xs-1">
                    <button class="btn btn-block btn-info" id="searchBtn">${I18n.system_search}</button>
                </div>
                <div class="col-xs-1">
                    <button class="btn btn-block btn-success add" type="button">${I18n.jobinfo_field_add}</button>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12">
                    <div class="box">
                        <#--<div class="box-header hide">
                            <h3 class="box-title">调度列表</h3>
                        </div>-->
                        <div class="box-body" >
                            <table id="job_dag_list" class="table table-bordered table-striped" width="100%" >
                                <thead>
                                <tr>
                                    <th name="id" >ID 编号</th>
                                    <th name="jobName" >任务名称</th>
                                    <#--<th name="jobDesc" >任务描述</th>-->
                                    <th name="jobRunExp" >执行表达式</th>
                                    <th name="status" >调度状态</th>
                                    <th name="lastRunTime" >最近调度</th>
                                    <th name="lastRunStatus" >运行状态</th>
                                    <th name="unRuning" >未运行</th>
                                    <th name="runing" >运行中</th>
                                    <th name="runErr" >异常</th>
                                    <th name="runOk" >成功</th>
                                    <th name="nodeTotal" >总数</th>
                                    <th name="triggerNextTime" >下次调度</th>
                                    <th name="queueSize" >待调度</th>
                                    <th>${I18n.system_opt}</th>
                                </tr>
                                </thead>
                                <tbody></tbody>
                                <tfoot></tfoot>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </div>

    <!-- footer -->
    <@netCommon.commonFooter />
</div>

<!-- job新增.模态框 -->
<div class="modal fade" id="addModal" tabindex="-1" role="dialog"  aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title" >${I18n.jobinfo_field_add}</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal form" role="form" >

                    <p style="margin: 0 0 10px;text-align: left;border-bottom: 1px solid #e5e5e5;color: gray;">${I18n.jobinfo_conf_base}</p>    <#-- 基础信息 -->
                    <div class="form-group">
                        <label for="jobName" class="col-sm-2 control-label">任务名称<font color="red">*</font></label>
                        <div class="col-sm-4"><input type="text" class="form-control" name="jobName" placeholder="${I18n.system_please_input} 任务名称" maxlength="50" ></div>
                    </div>
                    <div class="form-group">
                        <label for="jobDesc" class="col-sm-2 control-label">任务描述<font color="red">*</font></label>
                        <div class="col-sm-4"><input type="text" class="form-control" name="jobDesc" placeholder="${I18n.system_please_input} 任务描述" maxlength="50" ></div>
                    </div>

                    <div class="form-group schedule_conf_CRON">
                        <label for="jobRunExp" class="col-sm-2 control-label">执行表达式(Cron)<font color="red">*</font></label>
                        <div class="col-sm-4"><input type="text" class="form-control" name="jobRunExp" placeholder="${I18n.system_please_input} 执行表达式" maxlength="50" ></div>
                    </div>

                    <div class="form-group">
                        <label for="executorBlockStrategy" class="col-sm-2 control-label">${I18n.jobinfo_field_executorBlockStrategy}<font color="red">*</font></label>
                        <div class="col-sm-4">
                            <select class="form-control" name="executorBlockStrategy" >
                                <#list ExecutorBlockStrategyEnum as item>
                                    <option value="${item}" >${item.title}</option>
                                </#list>
                            </select>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="createBy" class="col-sm-2 control-label">负责人<font color="red">*</font></label>
                        <div class="col-sm-4"><input type="text" class="form-control" name="createBy" placeholder="${I18n.system_please_input} 负责人" maxlength="50" ></div>
                    </div>

                    <hr>
                    <div class="form-group">
                        <div class="col-sm-offset-3 col-sm-6 " style="text-align: right;">
                            <button type="submit" class="btn btn-primary"  >${I18n.system_save}</button>
                            <button type="button" class="btn btn-default" data-dismiss="modal">${I18n.system_cancel}</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<!-- 更新.模态框 -->
<div class="modal fade" id="updateModal" tabindex="-1" role="dialog"  aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title" >${I18n.jobinfo_field_update}</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal form" role="form" >

                    <p style="margin: 0 0 10px;text-align: left;border-bottom: 1px solid #e5e5e5;color: gray;">${I18n.jobinfo_conf_base}</p>    <#-- 基础信息 -->


                    <div class="form-group">
                        <label for="jobName" class="col-sm-2 control-label">任务名称<font color="red">*</font></label>
                        <div class="col-sm-4"><input type="text" class="form-control" name="jobName" placeholder="${I18n.system_please_input} 任务名称" maxlength="50" ></div>
                    </div>
                    <div class="form-group">
                        <label for="jobDesc" class="col-sm-2 control-label">任务描述<font color="red">*</font></label>
                        <div class="col-sm-4"><input type="text" class="form-control" name="jobDesc" placeholder="${I18n.system_please_input} 任务描述" maxlength="50" ></div>
                    </div>
                    <div class="form-group">
                        <label for="jobRunExp" class="col-sm-2 control-label">执行表达式<font color="red">*</font></label>
                        <div class="col-sm-4"><input type="text" class="form-control" name="jobRunExp" placeholder="${I18n.system_please_input} 执行表达式" maxlength="50" ></div>
                    </div>
                    <div class="form-group">
                        <label for="executorBlockStrategy" class="col-sm-2 control-label">${I18n.jobinfo_field_executorBlockStrategy}<font color="red">*</font></label>
                        <div class="col-sm-4">
                            <select class="form-control" name="executorBlockStrategy" >
                                <#list ExecutorBlockStrategyEnum as item>
                                    <option value="${item}" >${item.title}</option>
                                </#list>
                            </select>
                        </div>
                    </div>
                    <hr>
                    <div class="form-group">
                        <div class="col-sm-offset-3 col-sm-6">
                            <button type="submit" class="btn btn-primary"  >${I18n.system_save}</button>
                            <button type="button" class="btn btn-default" data-dismiss="modal">${I18n.system_cancel}</button>
                            <input type="hidden" name="id" >
                        </div>
                    </div>

                </form>
            </div>
        </div>
    </div>
</div>

<#-- trigger -->
<div class="modal fade" id="jobTriggerModal" tabindex="-1" role="dialog"  aria-hidden="true">
    <div class="modal-dialog ">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title" >${I18n.jobinfo_opt_run}</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal form" role="form" >
                    <div class="form-group">
                        <label for="firstname" class="col-sm-2 control-label">${I18n.jobinfo_field_executorparam}<font color="black">*</font></label>
                        <div class="col-sm-10">
                            <textarea class="textarea form-control" name="executorParam" placeholder="${I18n.system_please_input}${I18n.jobinfo_field_executorparam}" maxlength="512" style="height: 63px; line-height: 1.2;"></textarea>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="firstname" class="col-sm-2 control-label">${I18n.jobgroup_field_registryList}<font color="black">*</font></label>
                        <div class="col-sm-10">
                            <textarea class="textarea form-control" name="addressList" placeholder="${I18n.jobinfo_opt_run_tips}" maxlength="512" style="height: 63px; line-height: 1.2;"></textarea>
                        </div>
                    </div>
                    <hr>
                    <div class="form-group">
                        <div class="col-sm-offset-3 col-sm-6">
                            <button type="button" class="btn btn-primary ok" >${I18n.system_save}</button>
                            <button type="button" class="btn btn-default" data-dismiss="modal">${I18n.system_cancel}</button>
                            <input type="hidden" name="id" >
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<@netCommon.commonScript />
<!-- DataTables -->
<script src="${request.contextPath}/static/adminlte/bower_components/datatables.net/js/jquery.dataTables.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/datatables.net-bs/js/dataTables.bootstrap.min.js"></script>
<!-- moment -->
<script src="${request.contextPath}/static/adminlte/bower_components/moment/moment.min.js"></script>
<#-- cronGen -->
<script src="${request.contextPath}/static/plugins/cronGen/cronGen<#if I18n.admin_i18n?default('')?length gt 0 >_${I18n.admin_i18n}</#if>.js"></script>
<script src="${request.contextPath}/static/js/jobdag.index.1.js"></script>
</body>
</html>
