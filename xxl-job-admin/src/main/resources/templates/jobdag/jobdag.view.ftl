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
<body class="skin-blue fixed layout-top-nav">

<div class="wrapper">

    <header class="main-header">
        <nav class="navbar navbar-static-top">
            <div class="container" style="width: 100%;">
                <#-- icon -->
                <div class="navbar-header">
                    <a class="navbar-brand"><b>DAG</b>调度设计器</a>
                    <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar-collapse">
                        <i class="fa fa-bars"></i>
                    </button>
                </div>

                <#-- left nav -->
                <div class="collapse navbar-collapse pull-left" id="navbar-collapse">
                    <ul class="nav navbar-nav">
                        <li class="active" >
                            <a href="javascript:void(0);">
                                <span class="sr-only"></span>
                                (调度名称)${jobName}
                            </a>
                        </li>
                    </ul>
                </div>
                <div class="collapse navbar-collapse pull-left" id="navbar-collapse">
                    <ul class="nav navbar-nav">
                        <li class="" >
                            <a href="javascript:void(0);">
                                (调度状态)${status}
                            </a>
                        </li>
                    </ul>
                </div>
                <div class="collapse navbar-collapse pull-left" id="navbar-collapse">
                    <ul class="nav navbar-nav">
                        <li class="" id="runStatusBox"> <a href="javascript:void(0);" id="runStatusText"> </a></li>
                    </ul>
                </div>

                <div class="collapse navbar-collapse pull-left" id="navbar-collapse">
                    <ul class="nav navbar-nav">
                        <li class="" id="runStatusBox"> <a href="javascript:void(0);" id="runDataTimeText"> </a></li>
                    </ul>
                </div>

                <#-- right nav -->
                <div class="navbar-custom-menu">
                    <ul class="nav navbar-nav">
                        <li id="" onclick="saveNodes()" >
                            <a href="javascript:void(0);" >
                                <i class="fa fa-fw fa-save" ></i>
                                ${I18n.system_save}
                            </a>
                        </li>
                       <#-- <li>
                            <a href="javascript:void(0);" onclick="closeWindow()">
                                <i class="fa fa-fw fa-close" ></i>
                                ${I18n.system_close}
                            </a>
                        </li>-->
                    </ul>
                </div>

            </div>
        </nav>
    </header>

    <div class="content-wrapper" id="ideWindow" >
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
                            <a href="javascript:void(0);">
                                <i class="fa node-start"></i>
                                <span>开始</span>
                            </a>
                        </li>
                        <li class="node" data-id="212">
                            <a href="javascript:void(0);">
                                <i class="fa node-task" aria-hidden="true"></i>
                                <span>任务</span>
                            </a>
                        </li>
                        <li class="node" data-id="102">
                            <a href="javascript:void(0);">
                                <i class="fa node-end"></i>
                                <span>结束</span>
                            </a>
                        </li>
                    </ul>
                </li>

            </ul>
            <span style="font-size: 12px;">调度记录:</span>
            <ul class="nav navbar-nav" style="" id="runDataTimeRecords">

            </ul>
            <ul class="nav navbar-nav" style="position: absolute;bottom: 12px;">
                <li class="left-bt-li">
                    <p class="left-bt-li-left1"></p><p class="left-bt-li-right" id="node-unruning">未运行</p>
                </li>
                <li class="left-bt-li" >
                    <p class="left-bt-li-left2"></p><p class="left-bt-li-right" id="node-runing">运行中</p>
                </li>
                <li class="left-bt-li" >
                    <p class="left-bt-li-left3"></p><p class="left-bt-li-right" id="node-runend">运行结束</p>
                </li>
                <li class="left-bt-li" >
                    <p class="left-bt-li-left5"></p><p class="left-bt-li-right" id="node-runlose">任务丢失</p>
                </li>
                <li class="left-bt-li" >
                    <p class="left-bt-li-left4"></p><p class="left-bt-li-right" id="node-runerr">运行异常</p>
                </li>

            </ul>
        </div>

        <div class="middle-wrapper">
            <div id="idsw-bpmn" class="bpmn" style="position: relative; width: 100%; height: 100%;">
                <svg width="100%" height="100%">
                    <defs>
                        <marker id="arrowhead" markerWidth="8" markerHeight="8" refx="2" refy="5" orient="auto">
                            <path d="M2,2 L2,8 L8,5 L2,2" style="fill: #61a8e0;" />
                        </marker>
                    </defs>
                </svg>
            </div>

        </div>

    </div>

    <!-- footer -->
    <#--<@netCommon.commonFooter />-->
</div>

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

<div class="modal fade" id="showLogs" tabindex="-1" role="dialog"  aria-hidden="false">
    <div class="ue-container">
        <hr>
        <div style="text-align: right;">
            <button type="button" class="btn btn-primary flushUpStreaJobListBoxClose" data-dismiss="modal">${I18n.system_cancel}</button>
        </div>
    </div>
</div>

<ul id="myMenu" class="dropdown-menu" role="menu" _id="82">

</ul>

<@netCommon.commonScript />

<script src="${request.contextPath}/static/plugins/codemirror/lib/codemirror.js"></script>

<script src="${request.contextPath}/static/plugins/codemirror/addon/hint/show-hint.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/addon/hint/anyword-hint.js"></script>
<script src="${request.contextPath}/static/js/plugins/jquery-ui.min.js"></script>
<script src="${request.contextPath}/static/js/plugins/d3.v5.min.js"></script>
<script src="${request.contextPath}/static/js/plugins/d3-transform.min.js"></script>
<#-- doubleBox -->
<script src="${request.contextPath}/static/plugins/doublebox/doublebox-bootstrap.js"></script>
<script>
    var hostPath = '${hostPath}';
</script>
<script src="${request.contextPath}/static/js/jobdag.detail.1.js">

</script>
</body>
</html>