<!DOCTYPE html>
<html>
<head>
    <#import "../common/common.macro.ftl" as netCommon>
    <@netCommon.commonStyle />
    <link  rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/font-awesome/css/font-awesome.min.css">
    <link  rel="stylesheet" href="${request.contextPath}/static/css/dag-index.css">
    <link rel="stylesheet" href="${request.contextPath}/static/plugins/doublebox/doublebox-bootstrap.css">
    <title>${I18n.admin_name}</title>
</head>
<body class="hold-transition skin-blue layout-top-nav">
    <div class="container-fuild">
        <input type="hidden" value="${jobId}" id="dagJobId">
        <div id="left-wrapper" class="left-wrapper">
            <ul class="sidebar-nav">
                <li>
                    <a class="open">
                        <i class="fa fa-folder-o"></i>
                        <span>源</span>
                    </a>
                    <ul>
                        <li class="node" data-id="101">
                            <a href="">
                                <i class="fa node-start"></i>
                                <span>开始</span>
                            </a>
                        </li>
                        <li class="node" data-id="212">
                            <a href="">
                                <i class="fa fa-crosshairs" aria-hidden="true"></i>
                                <span>任务</span>
                            </a>
                        </li>
                        <li class="node" data-id="102">
                            <a href="">
                                <i class="fa fa-database"></i>
                                <span>结束</span>
                            </a>
                        </li>
                    </ul>
                </li>

            </ul>
        </div>

        <div class="middle-wrapper">
            <h4>调度名称：${jobName}</h4>
            <div id="idsw-bpmn" class="bpmn" style="position: relative; width: 100%; height: 100%;">
                <svg width="100%" height="100%">
                    <defs>
                        <marker id="arrowhead" markerWidth="8" markerHeight="8" refx="2" refy="5" orient="auto">
                            <path d="M2,2 L2,8 L8,5 L2,2" style="fill: #61a8e0;" />
                        </marker>
                    </defs>
                </svg>
            </div>
            <div style="height: 40px; border-top: solid 1px #e7e7e7; text-align: center; line-height: 40px; position: absolute;bottom: 2px; width: 100%">
                <a class="btn btn-link" href="javascript:void(0);" onclick="saveNodes()"><i class="fa fa-save" aria-hidden="true"></i>&nbsp;保存</a>
                <a class="btn btn-link" href="javascript:void(0);" onclick="closeWindow()"><i class="fa fa-close" aria-hidden="true"></i>&nbsp;关闭</a>
            </div>
        </div>

        <#--<div class="right-wrapper">
            <h4>任务节点属性</h4>
            <div class="row">
                <div class="col-xs-2">
                    <div class="input-group">
                        <span class="input-group-addon">${I18n.jobinfo_job}</span>
                        <select class="form-control" id="jobId" paramVal="<#if jobInfo?exists>${jobInfo.id}</#if>" >
                            <option value="0" >${I18n.system_all}</option>
                        </select>
                    </div>
                </div>
            </div>
        </div>-->


    </div>
<#--    <!-- footer &ndash;&gt;-->
<#--    <@netCommon.commonFooter />-->
    <div class="modal fade" id="doubleBox" tabindex="-1" role="dialog"  aria-hidden="false">
        <div class="ue-container">
            <select multiple="multiple" size="10" id="doubleChoose" name="doubleChoose">
            </select>
            <hr>
            <div style="text-align: right;">
                <button type="submit" id="flushUpStreaJobListBox" class="btn btn-primary flushUpStreaJobListBox" data-dismiss="modal">${I18n.system_ok}</button>
                <button type="button" class="btn btn-primary flushUpStreaJobListBoxClose" data-dismiss="modal">${I18n.system_cancel}</button>
            </div>
        </div>
    </div>

<@netCommon.commonScript />
<script>

</script>
<script src="${request.contextPath}/static/js/jobdag.detail.1.js"></script>
<script src="${request.contextPath}/static/js/plugins/jquery-ui.min.js"></script>
<script src="${request.contextPath}/static/js/plugins/d3.min.js"></script>
<script src="${request.contextPath}/static/js/plugins/d3-transform.min.js"></script>
<#-- doubleBox -->
<script src="${request.contextPath}/static/plugins/doublebox/doublebox-bootstrap.js"></script>
</body>
</html>