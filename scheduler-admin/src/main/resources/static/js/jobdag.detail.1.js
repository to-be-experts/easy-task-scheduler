var workflow = {
	mangNodes: {},
	nodes:[],
	links:[],
};
var svg = null ;
var runStatus = -1 ;
var graphG = null ;
var zoom = null;
var scale = 1 ;
var recordTime = 0;
var ws = null ;
$(function() {
	var loading = layer.load('数据正在加载...', {
		shade: [0.3,'#fff'] //0.1透明度的白色背景
	});
	setTimeout(function (data) {
		svg = d3.select("svg");
		graphG = svg.append("g") ;
		initDAGJobNodes();
		createTooltip() ;
		onZoom();
		setPosition();
		initWS() ;
		//最后数据加载完 让 loading层消失
		layer.close(loading);
	}, 1000);

	// 绑定拖拽
	$('#left-wrapper .node').draggable({
		helper: "clone",
		addClass: false,
		connectToSortable: "#idsw-bpmn",
		start: function (e, ui) {
			ui.helper.addClass("ui-draggable-helper");
		},
		stop: function (e, ui) {
			var node = {
				id: "node-"+new Date().getTime(),
				dataId: ui.helper.attr('data-id'),
				x: ui.position.left - 250,
				y: ui.position.top - 40,
				text: ui.helper.text().replaceAll(" ",""),
				record: "",
				inputs: 1,
				outputs: 2
			};

			if(node.dataId == 101) {
				node.inputs = 0;
				node.outputs = 1;
				node['taskId']= -1
			} else if(node.dataId == 102) {
				node.inputs = 1;
				node.outputs = 0;
				node['taskId']= 0 ;
			} else {
				node.inputs = 1;
				node.outputs = 1;
			}
			// 计算节点编号
			if(workflow.mangNodes[node.dataId]) {
				if(node.dataId == 101){
					layer.open({
						title: I18n.system_tips ,
						btn: [ I18n.system_ok ],
						content: "开始节点只能添加一个",
						icon: '2'
					});
					return ;
				}else if(node.dataId == 102){
					layer.open({
						title: I18n.system_tips ,
						btn: [ I18n.system_ok ],
						content: "结束节点只能添加一个",
						icon: '2'
					});
					return ;
				}else{
					workflow.mangNodes[node.dataId] += 1;
				}
			} else {
				workflow.mangNodes[node.dataId] = 1;
			}
			workflow.nodes.push(node) ;
			if(node.dataId == 212){
				showSelectJob(node);
			}else{
				pushNode(node) ;
			}
		}
	});
});

function initWS(){
	let jobId = $("#dagJobId").val() ;
	ws = new WebSocket("ws://"+hostPath+"/ws/status/"+jobId+"/"+recordTime);
	//申请一个WebSocket对象，参数是服务端地址，同http协议使用http://开头一样，WebSocket协议的url使用ws://开头，另外安全的WebSocket协议使用wss://开头
	ws.onopen = function(){
		//当WebSocket创建成功时，触发onopen事件
		console.log("open");
		ws.send("init"); //将消息发送到服务端
	}
	ws.onmessage = function(e){
		//当客户端收到服务端发来的消息时，触发onmessage事件，参数e.data包含server传递过来的数据
		//console.log(e.data);
		let dagJob = JSON.parse(e.data);
		runStatus =  dagJob.runStatus ;
		if(dagJob.runStatus == 0){
			$("#runStatusBox").css("background","#00bb00");
			if(dagJob.queueSize > 0){
				$("#runStatusText").html("运行状态：运行中<>共【"+dagJob.queueSize+"】个调度周期等待运行");
			}else{
				$("#runStatusText").html("运行状态：运行中");
			}
		}else if(dagJob.runStatus == 1){
			$("#runStatusBox").css("background","#00c0ef");
			$("#runStatusText").html("运行状态：运行结束");
		}else if(dagJob.runStatus == 2){
			$("#runStatusBox").css("background","#f14249");
			$("#runStatusText").html("运行状态：运行异常");
		}else{
			$("#runStatusBox").css("background","#bcddff");
			$("#runStatusText").html("运行状态：未运行");
		}
		$("#node-unruning").html("未运行:"+dagJob.nodeUnRuning);
		$("#node-runing").html("运行中:"+dagJob.nodeRuning);
		$("#node-runend").html("运行结束:"+dagJob.nodeRunEnd);
		$("#node-runerr").html("运行异常:"+dagJob.nodeRunErr);
		$("#node-runlose").html("任务丢失:"+dagJob.nodeRunLose);

		let nodes = dagJob.nodes ;
		for (let i = 0;i < nodes.length;i ++){
			let fill = "#bcddff" ;
			let img = base_url+"/static/img/task.svg";

			if(nodes[i].status == 1){
				img = base_url+"/static/img/runing.svg";
				fill = "#FFAA25" ;
			}else if(nodes[i].status == 2){
				img = base_url+"/static/img/ok.svg";
				fill = "#F1E3BC" ;
			}else if(nodes[i].status == 3){
				img = base_url+"/static/img/error.svg";
				fill = "#8d0600" ;
			}else if(nodes[i].status == 4){
				img = base_url+"/static/img/error.svg";
				fill = "#FF5600" ;
			}
			if(nodes[i].dataId == 101){
				img = base_url+"/static/img/start.svg";
			}
			if(nodes[i].dataId == 102){
				img = base_url+"/static/img/end.svg";
			}
			d3.select("#"+nodes[i].id +" image").attr("xlink:href",img) ;
			d3.select("#"+nodes[i].id ).attr("fill",fill) ;
			d3.select("#"+nodes[i].id +" rect").attr("fill",fill) ;
			for (let k = 0 ; k < workflow.nodes.length ; k ++){
				if(nodes[i].id == workflow.nodes[k].id){
					workflow.nodes[k]['record'] = nodes[i].record ;
					workflow.nodes[k]['status'] = nodes[i].status ;
				}
			}
		}

	}
	ws.onclose = function(e){
		//当客户端收到服务端发送的关闭连接请求时，触发onclose事件
		console.log("close");
	}
	ws.onerror = function(e){
		//如果出现连接、处理、接收、发送数据失败的时候触发onerror事件
		console.log(error);
	}

	loadRunDataTimeLimit();
}

function loadRunDataTimeLimit(){
	$.ajax({
		type : 'POST',
		timeout : 3000,
		url: base_url + "/dagjob/loadRunDataTimeLimit",
		data : {
			"id":$("#dagJobId").val(),
			"size":10
		},
		async:false,
		dataType : "json",
		success : function(data){
			if(data.code == 200){
				$('#runDataTimeRecords').html("");
				for(let i = 0 ; i < data.content.length; i ++){
					let item = data.content[i] ;
					let line = '' ;
					if(runStatus == -1 && recordTime <= 0){
						//未运行，取第一个记录
						recordTime = data.content[0].runDataTime;
					}
					if(item.runDataTime  == recordTime){
						line = '<li class="left-run-data-time-li left-run-data-time-item-active" onclick="loadDagJobRunStatus('+item.runDataTime+',this)">\n' +
								'	<p class="left-run-data-time-item" >'+getDate(item.runDataTime)+'</p>\n' +
								'</li>' ;
					}else{
						line = '<li class="left-run-data-time-li" onclick="loadDagJobRunStatus('+item.runDataTime+',this)">\n' +
								'	<p class="left-run-data-time-item" >'+getDate(item.runDataTime)+'</p>\n' +
								'</li>' ;
					}
					$('#runDataTimeRecords').append(line) ;
				}

				if(recordTime > 0){
					$("#runDataTimeText").html("调度时间:"+getDate(recordTime)) ;
					if( ws ){
						ws.close();
					}
					initWS();
				}
			}
		}
	});
}

function loadDagJobRunStatus(optime,obj){
	recordTime = optime ;
	var loading = layer.load('数据正在加载...', {
		shade: [0.3,'#fff'] //0.1透明度的白色背景
	});
	setTimeout(function (data) {
		$("#runDataTimeText").html("调度时间:"+getDate(recordTime)) ;
		$(obj).addClass("left-run-data-time-item-active");
		ws.close();
		initWS() ;
		//最后数据加载完 让 loading层消失
		layer.close(loading);
	}, 1000);

}

function initDAGJobNodes(){

	$.ajax({
		type : 'POST',
		timeout : 3000,
		url: base_url + "/dagjob/getById",
		data : {
			"id":$("#dagJobId").val()
		},
		async:false,
		dataType : "json",
		success : function(data){
			if(data.code == 200){
				if(data.content && data.content.nodes && data.content.nodes.length > 1){
					workflow.mangNodes[101] = 1;
					workflow.mangNodes[102] = 1;
					workflow.mangNodes[212] = data.content.nodes.length - 2;

					workflow.nodes = data.content.nodes ;
					workflow.links = data.content.links ;
					for (let i = 0 ; i < workflow.nodes.length ; i ++){
						workflow.nodes[i].id = workflow.nodes[i].id
						pushNode(workflow.nodes[i]) ;
					}

					for (let i = 0 ; i < workflow.links.length ; i ++){
						let node = getNodeById(workflow.links[i].from)
						let line = d3.select("svg").select("g")
							.append("path")
							.attr("class", "cable")
							.attr("start", (40) + ", " + 15)
							.attr("output",node.outputs)
							.attr("from",  workflow.links[i].from)
							.attr("to",  workflow.links[i].to)
							.attr("id",  workflow.links[i].from+"-"+workflow.links[i].to)
							.attr("input", node.inputs)
							.attr("end", "-7," + 15 )
							.attr("d",workflow.links[i].d)
							.attr("marker-end", "url(#arrowhead)");

						let elem = d3.select("#"+workflow.links[i].from) ;
						elem.attr("transform", "translate(" + (node.x) + ", " + (node.y) + ")");
						updateCable(elem)

						let node2 = getNodeById(workflow.links[i].to)
						let elem2 = d3.select("#"+workflow.links[i].to) ;
						elem2.attr("transform", "translate(" + (node2.x) + ", " + (node2.y) + ")");
						updateCable(elem2)
						initLineEvent(line) ;
					}
				}

			}
		}
	});
}

function initLineEvent(line){
	line.on("contextmenu", (d) => { //鼠标右击事件
		d3.event.preventDefault();// 禁止系统默认右键
		d3.event.stopPropagation();// 阻止事件冒泡

		let menuHtml = "" ;
		menuHtml += ' <li onClick="removeLink()"><a href="javascript:void(0);"  >删除</a></li> ' ;
		myMenu.innerHTML = menuHtml ;
		myMenu.style.top = d3.event.clientY + "px";
		myMenu.style.left = d3.event.clientX + "px";
		myMenu.style.display = 'block';
		contextmenuSelectLink = line ;

	}).on('click',function(d){
		line.attr("class", "cable-select")
	}).on('mouseover',function(d){
		line.attr("class", "cable-select")
	}).on('mouseout',function (d){
		if(contextmenuSelectLink == null){
			line.attr("class", "cable")
		}
	});
}

function getNodeById(id){
	let node = null ;
	for (let i = 0 ; i < workflow.nodes.length ; i ++){
		if(workflow.nodes[i].id == id){
			node = workflow.nodes[i] ;
		}
	}
	return node ;
}

function pushNode(node){
	//console.log(node);
	var g = addNode(graphG, node);
	g.call(
		d3.drag()
			.on("start", dragstarted)
			.on("drag", dragged)
			.on("end", dragended)
	);
	g.selectAll("circle.output").call(
		d3.drag()
			.on("start", linestarted)
			.on("drag", linedragged)
			.on("end", lineended)
	);
	g.selectAll("circle.input").on("mouseover", function() {
		if(drawLine) {
			d3.selectAll("circle.end").classed("end", false);
			d3.select(this).classed("end", true);
		}
	});
}

var activeLine = null;
var points = [];
var translate = null;
var drawLine = false;
function linestarted() {
	drawLine = false;
	if(points.length > 0){
		points = [] ;
	}
	// 当前选中的circle
	var anchor = d3.select(this);
	// 当前选中的节点
	var node = d3.select(this.parentNode);
	var rect = node.node().getBoundingClientRect();
	var dx = rect.width
	var dy = rect.height / (+anchor.attr("output") + 1);

	var transform = node.attr("transform");
	translate = getTranslate(transform);
	console.log(scale);

	points.push([dx * 100/scale + translate[0], dy * 100/scale + translate[1]]);
	activeLine = d3.select("svg").select("g")
		.append("path")
		.attr("class", "cable")
		.attr("from", node.attr("id"))
		.attr("start", (40) + ", " + 15)
		.attr("output", d3.select(this).attr("output"))
		.attr("marker-end", "url(#arrowhead)");
}

function linedragged() {
	drawLine = true;
	points[1] = [d3.event.x + translate[0], d3.event.y + translate[1]];
	activeLine.attr("d", function() {
		return "M" + points[0][0] + "," + points[0][1]
			+ " C" + points[1][0] + "," + (points[1][1] + points[1][1]) / 2
			+ " " + points[0][0] + "," +  (points[1][1] + points[1][1]) / 2
			+ " " + points[1][0] + "," + points[1][1];
	});
}

function lineended(d) {
	drawLine = false;
	var anchor = d3.selectAll("circle.end");
	if(anchor.empty()) {
		activeLine.remove();
	} else {
		var pNode = d3.select(anchor.node().parentNode);
		var input = pNode.node().getBoundingClientRect().height / (+anchor.attr("input") + 1);
		anchor.classed("end", false);
		activeLine.attr("to", pNode.attr("id"));
		activeLine.attr("input", anchor.attr("input"));
		activeLine.attr("end", "-7," + 15 );
	}
	if( activeLine.attr("from") &&  activeLine.attr("to")){
		activeLine.attr("id",activeLine.attr("from")+"-"+activeLine.attr("to")) ;
		workflow.links.push({from:activeLine.attr("from"),to:activeLine.attr("to"),d:activeLine.attr("d")});
	}
	updateCable(d3.select("#"+activeLine.attr("from")));
	updateCable(d3.select("#"+activeLine.attr("to")));
	initLineEvent(activeLine)
	activeLine = null;
	points.length = 0;
	translate = null;
}

function getTranslate(transform) {
	var arr = transform.substring(transform.indexOf("(")+1, transform.indexOf(")")).split(",");
	return [+arr[0], +arr[1]];
}

function onZoom() {
	// 鼠标滚轮缩放
	zoom = d3.zoom().on('zoom', () => {
		graphG.attr('transform', d3.event.transform);
		const { transform } = d3.event;
		scale = Number((transform.k * 100).toFixed());
	});
	svg.call(this.zoom).on("dblclick.zoom", null);
}

function setPosition(){
	const bounds = graphG.node().getBBox()
	const parent = graphG.node().parentElement || this.innerG.node().parentNode
	const fullWidth = parent.clientWidth || parent.parentNode.clientWidth
	const fullHeight = parent.clientHeight || parent.parentNode.clientHeight
	const width = bounds.width
	const height = bounds.height
	const midX = bounds.x + width / 2
	const midY = bounds.y + height / 2
	if (width === 0 || height === 0) return // nothing to fit
	var scale = 0.9 / Math.max(width / fullWidth, height / fullHeight)
	var translate = [fullWidth / 2 - scale * midX, fullHeight / 2 - scale * midY]
	var transform = d3.zoomIdentity.translate(translate[0], translate[1]).scale(scale)

	svg.transition().duration(this.animate || 0) .call(zoom.transform, transform)
}

var dx = 0;
var dy = 0;
var dragElem = null;
var dragElemText = null;
function dragstarted() {
	var transform = d3.select(this).attr("transform");
	var translate = getTranslate(transform);
	dx = d3.event.x - translate[0];
	dy = d3.event.y - translate[1];
	dragElem = d3.select(this);
	let textId = "node-text-"+dragElem.attr("id") ;
	dragElemText = d3.select("#"+textId);
}

function dragged() {
	dragElem.attr("transform", "translate(" + (d3.event.x - dx) + ", " + (d3.event.y - dy) + ")");
	dragElemText.attr("transform", "translate(" + (d3.event.x - dx) + ", " + (d3.event.y - dy) + ")");
	updateCable(dragElem);
}

function updateCable(elem) {
	var bound = elem.node().getBoundingClientRect();
	var width = bound.width;
	var height = bound.height;
	var id = elem.attr("id");
	var transform = elem.attr("transform");
	var t1 = getTranslate(transform);


	// 更新输出线的位置
	d3.selectAll('path[from="' + id + '"]')
		.each(function() {
			var start = d3.select(this).attr("start").split(",");
			start[0] = +start[0] + t1[0];
			start[1] = +start[1] + t1[1];

			var path = d3.select(this).attr("d");
			var end = path.substring(path.lastIndexOf(" ") + 1).split(",");
			end[0] = +end[0];
			end[1] = +end[1];

			d3.select(this).attr("d", function() {
				return "M" + start[0] + "," + start[1]
					+ " C" + end[0] + "," + end[1]
					+ " " + start[0] + "," +  end[1]
					+ " " + end[0] + "," + end[1];
			});
		});

	// 更新输入线的位置
	d3.selectAll('path[to="' + id + '"]')
		.each(function() {
			var path = d3.select(this).attr("d");
			var start = path.substring(1, path.indexOf("C")).split(",");
			start[0] = +start[0];
			start[1] = +start[1];

			var end = d3.select(this).attr("end").split(",");
			end[0] = +end[0] + t1[0];
			end[1] = +end[1] + t1[1];

			d3.select(this).attr("d", function() {
				return "M" + start[0] + "," + start[1]
					+ " C" + end[0] + "," + end[1]
					+ " " + start[0] + "," +  end[1]
					+ " " + end[0] + "," + end[1];
			});
		});

}

function dragended() {
	dx = dy = 0;
	var id = dragElem.attr("id");
	d3.selectAll('path[to="' + id + '"]') .each(function() {
		var path = d3.select(this).attr("d");
		for(let i = 0 ; i < workflow.links.length ; i ++){
			if(workflow.links[i].to == id){
				workflow.links[i].d = path ;
			}
		}
	});
	d3.selectAll('path[from="' + id + '"]') .each(function() {
		var path = d3.select(this).attr("d");
		for(let i = 0 ; i < workflow.links.length ; i ++){
			if(workflow.links[i].from == id){
				workflow.links[i].d = path ;
			}
		}
	});
	for(let i = 0 ; i < workflow.nodes.length ; i ++){
		if(workflow.nodes[i].id == id){
			workflow.nodes[i].x = dragElem._groups[0][0].transform.baseVal[0].matrix.e ;
			workflow.nodes[i].y = dragElem._groups[0][0].transform.baseVal[0].matrix.f ;
		}
	}
	dragElem = null;
	dragElemText = null;
}
var contextmenuSelectNode = null ;
var contextmenuSelectLink = null ;
var myMenu = document.getElementById("myMenu"); //鼠标右键

document.addEventListener("click", (event) => {
	myMenu.style.display = 'none';
	contextmenuSelectNode = null ;
	contextmenuSelectLink = null ;
});



function addNode(svg, node) {
	let fill = "#bcddff" ;
	let img = base_url+"/static/img/task.svg";

	if(node.status == 1){
		img = base_url+"/static/img/runing.svg";
		fill = "#FFAA25" ;
	}else if(node.status == 2){
		img = base_url+"/static/img/ok.svg";
		fill = "#F1E3BC" ;
	}else if(node.status == 3){
		img = base_url+"/static/img/error.svg";
		fill = "#8d0600" ;
	}else if(node.status == 4){
		img = base_url+"/static/img/error.svg";
		fill = "#FF5600" ;
	}
	if(node.dataId == 101){
		img = base_url+"/static/img/start.svg";
	}
	if(node.dataId == 102){
		img = base_url+"/static/img/end.svg";
	}

	var g = svg.append("g")
		.attr("class", "node")
		.attr("data-id", node.dataId)
		.attr("id", node.id)
		.attr("transform", 'translate(' + node.x + ', ' + node.y + ')')
		.on("contextmenu", (d) => { //鼠标右击事件
			d3.event.preventDefault();// 禁止系统默认右键
			d3.event.stopPropagation();// 阻止事件冒泡

			let menuHtml = "" ;
			let menuIndex = 0 ;
			if(node.dataId == 212){
				menuHtml += ' <li onClick="toShowLog(1)"><a href="javascript:void(0);">查询历史日志</a></li> \n' ;
				menuIndex ++ ;
				if(node.status != 0){
					menuHtml += ' <li onClick="toShowLog(2)"><a href="javascript:void(0);">执行日志</a></li> \n' ;
					menuIndex ++ ;
				}
				menuHtml += ' <li className="divider"></li>  \n' ;
				if(node.status == 1 ){
					menuHtml += ' <li onClick="toKillJobIde(1)"><a href="javascript:void(0);" >跳过执行</a></li>  \n' ;
					menuHtml += ' <li onClick="toKillJobIde(2)"><a href="javascript:void(0);" >终止任务</a></li>  \n' ;
					menuIndex  = menuIndex + 2 ;
				}
				if(node.type == 'GLUE' ){
					let codeUrl = base_url +'/jobcode?jobId='+ node.taskId;
					menuHtml += '<li><a href="'+ codeUrl +'" target="_blank" >GLUE IDE</a></li>\n';
					menuHtml += '<li class="divider"></li>\n';
					menuIndex ++ ;
				}
				if(node.status == 3 ){
					menuHtml += ' <li onClick="doRunRecord(1)"><a href="javascript:void(0);" className="job_trigger">重置当前及后续</a></li> \n ';
					menuHtml += ' <li onClick="doRunRecord(4)"><a href="javascript:void(0);" className="job_trigger">重置当前</a></li> \n ';
					menuHtml += ' <li onClick="doRunRecord(2)"><a href="javascript:void(0);" className="job_trigger">忽略</a></li> \n ';
					menuIndex  = menuIndex + 3 ;
				}
				if(node.status == 4 ){
					menuHtml += ' <li onClick="doRunRecord(1)"><a href="javascript:void(0);" className="job_trigger">重置当前及后续</a></li> \n ';
					menuHtml += ' <li onClick="doRunRecord(4)"><a href="javascript:void(0);" className="job_trigger">重置当前</a></li> \n ';
					menuIndex  += 2;
				}
				if(node.status == 2){
					menuHtml += ' <li onClick="doRunRecord(3)"><a href="javascript:void(0);" className="job_trigger">重置当前及后续</a></li> \n ';
					menuHtml += ' <li onClick="doRunRecord(4)"><a href="javascript:void(0);" className="job_trigger">重置当前</a></li> \n ';
					menuIndex  = menuIndex + 2 ;
				}

				if(node.status == 0){
					menuHtml += ' <li onClick="doRunOneTime()"><a href="javascript:void(0);" className="job_trigger">立即执行</a></li>  \n' ;
					menuHtml += ' <li onClick="doRunRecord(3)"><a href="javascript:void(0);" className="job_trigger">执行当前及后续</a></li>  \n' ;
					menuIndex ++ ;
				}

				if( runStatus == -1 && node.status == 0){
					menuHtml += ' <li onClick="editNode()"><a href="javascript:void(0);" className="update">编辑</a></li>  \n' ;
					menuHtml += ' <li onClick="removeNode()"><a href="javascript:void(0);" className="job_operate" _type="job_del">删除</a></li>  \n' ;
					menuIndex  = menuIndex + 2 ;
				}


			}else if(node.dataId == 101 && runStatus == -1){
				//开始节点，可以把启动一次调度，在调度处于未运行状态的时候
				menuHtml += ' <li onClick="triggerJob()"><a href="javascript:void(0);">调度一次</a></li> \n' ;
				menuIndex ++ ;
			}
			if(menuHtml != ""){
				myMenu.innerHTML = menuHtml ;
				let top = d3.event.clientY + "px";
				let left = d3.event.clientX + "px";
				var bound = d3.select("svg>g").node().getBoundingClientRect();
				var width = bound.width;
				var height = bound.height;
				let stepY = menuIndex * 30 ;
				if(height - d3.event.clientY <= (stepY + 60)){
					top = d3.event.clientY - stepY + "px";
				}
				if(width - d3.event.clientX <= 160){
					left = d3.event.clientX - 160 + "px";
				}

				myMenu.style.top = top ;
				myMenu.style.left = left ;
				myMenu.style.display = 'block';
			}
			contextmenuSelectNode = node ;
		}).on('mouseover',function(d){
			const button = d3.select(this);
			tipVisible(node.text);
			button.attr('fill', '#CACACA');
			let nodeId = node.id ;
			for(let i = 0 ; i < workflow.links.length ; i ++){
				if(workflow.links[i].from == nodeId){
					d3.select("#"+workflow.links[i].from+"-"+workflow.links[i].to).attr("class","cable-node-select") ;
				}
				if(workflow.links[i].to == nodeId){
					d3.select("#"+workflow.links[i].from+"-"+workflow.links[i].to).attr("class","cable-node-select") ;
				}
			}

			d3.event.stopPropagation();
		}) .on('mouseout',function (d){
			tipHidden();
			const button= d3.select(this);
			button.attr('fill', fill);

			let nodeId = node.id ;
			for(let i = 0 ; i < workflow.links.length ; i ++){
				if(workflow.links[i].from == nodeId){
					d3.select("#"+workflow.links[i].from+"-"+workflow.links[i].to).attr("class","cable") ;
				}
				if(workflow.links[i].to == nodeId){
					d3.select("#"+workflow.links[i].from+"-"+workflow.links[i].to).attr("class","cable") ;
				}
			}

			d3.event.stopPropagation();
		});


	var rect = g.append("rect")
		.attr("rx", 5)
		.attr("ry", 5)
		.attr("width", 5)
		.attr("height", 5)
		.attr("stroke-width", 1)
		.attr("stroke", "#F2F6F3")
		.attr("fill", fill);

	var bound = rect.node().getBoundingClientRect();
	var width = bound.width;
	var height = bound.height;
	// text
	svg.append("text")
		.text(function(item){
			return node.text
		})
		.attr("x", (width/2))
		.attr("y", height + 10)
		.attr("dominant-baseline", "central")
		.attr("text-anchor", "middle")
		.attr("id", "node-text-"+node.id)
		.attr("transform", 'translate(' + node.x + ', ' + node.y + ')');



	// left icon
	g.append("image")
		.attr("width", 40)
		.attr("height", 30)
		.attr("xlink:href", img)
		.attr("dominant-baseline", "central")
		.attr("text-anchor", "middle")
		.attr('font-family', 'FontAwesome')



	// input circle
	var inputs = node.inputs || 0;
	g.attr("inputs", inputs);
	for(var i = 0; i < inputs; i++) {
		g.append("circle")
			.attr("class", "input")
			.attr("input", (i + 1))
			.attr("cx", 0)
			.attr("cy", height * (i + 1) / (inputs + 1))
			.attr("r", 2);
	}

	// output circle
	var outputs = node.outputs || 0;
	g.attr("outputs", outputs);
	for(i = 0; i < outputs; i++) {
		g.append("circle")
			.attr("output", (i + 1))
			.attr("class", "output")
			.attr("cx", width )
			.attr("cy", height * (i + 1) / (outputs + 1))
			.attr("r", 2);
	}

	return g;
}

function showSelectJob(node){
	$('#doubleBox').modal({backdrop: false, keyboard: false}).modal('show');

	//清空原有数据
	$(".ue-container .filter").each(function(){$(this).val("")});
	$('#doubleChoose').empty();
	$("#bootstrap-duallistbox-selected-list_doubleChoose").empty();
	$("#bootstrap-duallistbox-nonselected-list_doubleChoose").empty();

	//当前弹出的doubleBox时，标记是add/update
	var modal = $(this).attr("modal");
	$('#doubleBox').attr("modal",modal);

	//所有任务
	var allList = new Map();
	//未依赖的任务
	var nonSelectedList = new Array();
	//依赖的任务
	var selectedList = new Array();

	//【读取所有任务】从第一条开始读取
	var start = 0;
	//【读取所有任务】每次读100条
	var length = 200;
	//读取所有任务
	while (true){
		var isBreak = true;
		$.ajax({
			type : 'POST',
			timeout : 3000,
			url: base_url + "/jobinfo/pageList",
			data : {
				"jobGroup":0,
				"triggerStatus":-1,
				"start" : start,
				"length":length
			},
			async:false,
			dataType : "json",
			success : function(data){
				if(data.data.length>0){
					start = start + length;
					isBreak = false;
				}
				data.data.forEach(function (jobInfo) {
					if(parseInt($("#"+modal+" .form input[name='id']").val())!=jobInfo.id){
						allList.set(jobInfo.id,jobInfo.jobDesc);
					}
				})
			}
		});

		if (isBreak==true){
			break;
		}
	}

	//在doublebox显示依赖的“上游任务”
	var upStreamJobList = $("#"+modal+" .form textarea[name='upStreamJobList']").val();
	if(upStreamJobList && upStreamJobList!=""){
		upStreamJobList.split(",").forEach(function (value) {
			var jobId = parseInt(value);
			if(allList.has(jobId)){
				selectedList.push({"roleId":jobId,"roleName":"["+jobId+"] "+allList.get(jobId)});

				//为简化代码，把selectedList从allList去掉，以供nonSelectedList使用
				allList.delete(jobId);
			}
		});
	}

	//在doublebox显示未依赖的“上游任务”
	allList.forEach(function (jobName,jobId) {
		if(parseInt($("#"+modal+" .form input[name='id']").val())!=jobId){
			nonSelectedList.push({"roleId":jobId,"roleName":"["+jobId+"] "+jobName});
		}
	});

	$("#doubleChoose").doublebox({
		nonSelectedListLabel: '任务列表',
		selectedListLabel: '选择的上游任务列表',
		preserveSelectionOnMove: 'moved',
		moveOnSelect: false,
		nonSelectedList:nonSelectedList,
		selectedList:selectedList,
		optionValue:"roleId",
		optionText:"roleName",
		doubleMove:true,
	});
	$(".flushUpStreaJobListBox").on('click',function() {
		//获取选择的上游任务
		//event.stopPropagation();
		var chooseList = $('#bootstrap-duallistbox-selected-list_doubleChoose option').map(function () {
			return {id:this.value,name:this.text};
		}).get() ;
		if(!chooseList || chooseList.length <= 0){
			layer.open({
				title: I18n.system_tips ,
				btn: [ I18n.system_ok ],
				content: "请选择一个JOB",
				icon: '2'
			});
			return false;
		}
		node['taskId']=chooseList[0].id
		node['text']=chooseList[0].name
		if(!node.edit){
			pushNode(node) ;
		}
		$(this).off() ;
		$(".flushUpStreaJobListBoxClose").off() ;
	});
	$(".flushUpStreaJobListBoxClose").on('click',function() {
		$(this).off() ;
		$(".flushUpStreaJobListBox").off() ;
	});
}

function saveNodes(){
	console.log(workflow.nodes)
	console.log(workflow.links)
	console.log(workflow.mangNodes)
	let dagInfo = {
		"dagJobId":$("#dagJobId").val() ,
		"nodes":workflow.nodes,
		"links":workflow.links
	}
	$.ajax({
		url: base_url+"/dagjob/updateDagInfo",
		type: "POST",
		data: JSON.stringify(dagInfo),
		dataType: "json",
		contentType: "application/json",
		success: function (data) {
			console.log(data);
			if (data.code == "200") {
				layer.open({
					title: I18n.system_tips ,
					btn: [ I18n.system_ok ],
					content: I18n.system_update_suc ,
					icon: '1',
					end: function(layero, index){
						closeWindow();
					}
				});
			} else {
				layer.open({
					title: I18n.system_tips ,
					btn: [ I18n.system_ok ],
					content: (data.msg || I18n.system_update_fail),
					icon: '2'
				});
			}
		},
		error:function () {
			layer.open({
				title: I18n.system_tips ,
				btn: [ I18n.system_ok ],
				content: (data.msg || I18n.system_update_fail),
				icon: '2'
			});
		}
	});

}

function closeWindow(){
	if(navigator.userAgent.indexOf("Firefox") != -1 || navigator.userAgent.indexOf("Chrome") != -1){
		//window.location.href = "about:blank";
		window.close();
	}else{
		window.opener = null;
		window.open("", "_self");
		window.close();
	}
}
//创建提示框
var tooltip = null ;
function createTooltip() {
	tooltip =  d3.select('body')
		.append('div')
		.classed('tooltip', true)
		.style('opacity', 0)
		.style('display', 'none');
}
//tooltip显示
function tipVisible(textContent) {
	tooltip.transition()
		.duration(400)
		.style('opacity', 0.9)
		.style('display', 'block');
	tooltip.html(textContent)
		.style('left', (d3.event.pageX + 15) + 'px')
		.style('top', (d3.event.pageY + 15) + 'px');
}
//tooltip隐藏
function tipHidden() {
	tooltip.transition()
		.duration(400)
		.style('opacity', 0)
		.style('display', 'none');

	d3.selectAll(".tooltip").each(function (item) {
		d3.select(this).transition()
			.duration(400)
			.style('opacity', 0)
			.style('display', 'none');
	});
}

function toKillJobIde(index){
	let jobId = contextmenuSelectNode.taskId ;
	let record = contextmenuSelectNode.record ;
	if(index == 1){
		layer.confirm( '任务正在执行，跳过执行会导致数据异常，确认要跳过该任务吗?', {
			icon: 3,
			title: I18n.system_tips ,
			btn: [ I18n.system_ok, I18n.system_cancel ]
		}, function(index) {
			layer.close(index);
			if(!record || record == ""){
				layer.open({
					title: I18n.system_tips ,
					btn: [ I18n.system_ok ],
					content: "运行记录加载异常，请刷新后再试！",
					icon: '2'
				});
				return ;
			}
			$.ajax({
				type : 'POST',
				url : base_url + "/dagjob/toNextStep",
				data : {
					"jobId" : jobId,
					"record" : record,
				},
				dataType : "json",
				success : function(data){
					if (data.code == 200) {
						$('#jobTriggerModal').modal('hide');
						layer.msg( "跳过任务成功 !" );
					} else {
						layer.msg( data.msg+"-跳过任务"+I18n.system_fail  );
					}
				}
			});
		});
	}
	if(index == 2){
		layer.confirm( '任务正在执行，终止会导致数据异常，确认要终止任务吗?', {
			icon: 3,
			title: I18n.system_tips ,
			btn: [ I18n.system_ok, I18n.system_cancel ]
		}, function(index) {
			layer.close(index);
			if(!record || record == ""){
				layer.open({
					title: I18n.system_tips ,
					btn: [ I18n.system_ok ],
					content: "运行记录加载异常，请刷新后再试！",
					icon: '2'
				});
				return ;
			}
			$.ajax({
				type : 'POST',
				url : base_url + "/dagjob/kill",
				data : {
					"jobId" : jobId,
					"record" : record,
				},
				dataType : "json",
				success : function(data){
					if (data.code == 200) {
						$('#jobTriggerModal').modal('hide');

						layer.msg( "终止任务 " + I18n.system_success );
					} else {
						layer.msg( data.msg || "终止任务 " + I18n.system_fail );
					}
				}
			});
		});
	}

}

function doRunOneTime(){
	let dagJobId = $("#dagJobId").val() ;
	let jobId = contextmenuSelectNode.taskId ;
	let record = contextmenuSelectNode.record ;
	if(!jobId || jobId == ""){
		layer.open({
			title: I18n.system_tips ,
			btn: [ I18n.system_ok ],
			content: "任务配置加载异常，请刷新后再试！",
			icon: '2'
		});
		return ;
	}
	$.ajax({
		type : 'POST',
		url : base_url + "/dagjob/triggerOneTime",
		data : {
			"dagJobId":dagJobId,
			"jobId" : jobId,
			"record": record
		},
		dataType : "json",
		success : function(data){
			if (data.code == 200) {
				$('#jobTriggerModal').modal('hide');

				layer.msg( I18n.jobinfo_opt_run + I18n.system_success );
			} else {
				layer.msg( data.msg || I18n.jobinfo_opt_run + I18n.system_fail ,{
					icon: 2,
					time: 2000 //2秒关闭（如果不配置，默认是3秒）
				});
			}
		}
	});
}

function doRunRecord(index){
	let jobId = contextmenuSelectNode.taskId ;
	let record = contextmenuSelectNode.record ;
	if(!record || record == ""){
		layer.open({
			title: I18n.system_tips ,
			btn: [ I18n.system_ok ],
			content: "运行记录加载异常，请刷新后再试！",
			icon: '2'
		});
		return ;
	}
	$.ajax({
		type : 'POST',
		url : base_url + "/dagjob/triggerAgain",
		data : {
			"jobId" : jobId,
			"record" : record,
			"type" : index
		},
		dataType : "json",
		success : function(data){
			if (data.code == 200) {
				$('#jobTriggerModal').modal('hide');
				if(index == 1){
					layer.msg( "补偿调度" + I18n.system_success );
				}
				if(index == 2){
					layer.msg( "异常忽略" + I18n.system_success );
				}
				if(index == 3){
					layer.msg( "重新调度" + I18n.system_success );
				}
				if(index == 4){
					layer.msg( I18n.jobinfo_opt_run + I18n.system_success );
				}

			} else {
				if(index == 1){
					layer.msg( data.msg || "补偿调度" + I18n.system_fail ,{
						icon: 2,
						time: 5000 //2秒关闭（如果不配置，默认是3秒）
					});
				}
				if(index == 2){
					layer.msg( data.msg || "异常忽略" + I18n.system_fail ,{
						icon: 2,
						time: 5000 //2秒关闭（如果不配置，默认是3秒）
					});
				}
				if(index == 3){
					layer.msg( data.msg || "重新调度" + I18n.system_fail ,{
						icon: 2,
						time: 5000 //2秒关闭（如果不配置，默认是3秒）
					});
				}
				if(index == 4){
					layer.msg( data.msg || I18n.jobinfo_opt_run + I18n.system_fail ,{
						icon: 2,
						time: 5000 //2秒关闭（如果不配置，默认是3秒）
					});
				}
			}
		}
	});
}

function toShowLog(index){
	let jobId = contextmenuSelectNode.taskId ;
	let record = contextmenuSelectNode.record ;
	if(index == 1){
		var area = [];
		if (window.screen.width > 1680 && window.screen.width <= 1920) {
			area = ['95%', '98%']
		} else if (window.screen.width > 1600 && window.screen.width <= 1680) {
			area = ['95%', '98%']
		} else if (window.screen.width > 1366 && window.screen.width <= 1600) {
			area = ['95%', '98%']
		} else if (window.screen.width <= 1366) {
			area = ['95%', '98%']
		}
		var logHref = base_url +'/joblog/logQueryView?jobId='+ jobId;
		let dialogIndex = layer.open({
			type: 2,
			title: '查看日志',
			maxmin: true,
			shadeClose: true, //点击遮罩关闭层
			area : area,
			content: logHref
		});
		//layer.full(dialogIndex);
	}
	if(index == 2){
		if(!record || record == ""){
			layer.open({
				title: I18n.system_tips ,
				btn: [ I18n.system_ok ],
				content: "无运行记录日志",
				icon: '2'
			});
			return ;
		}
		var area = [];
		if (window.screen.width > 1680 && window.screen.width <= 1920) {
			area = ['78%', '90%']
		} else if (window.screen.width > 1600 && window.screen.width <= 1680) {
			area = ['78%', '90%']
		} else if (window.screen.width > 1366 && window.screen.width <= 1600) {
			area = ['78%', '90%']
		} else if (window.screen.width <= 1366) {
			area = ['78%', '90%']
		}
		let dialogIndex = layer.open({
			type: 2,
			title: '查看日志',
			maxmin: true,
			shadeClose: true, //点击遮罩关闭层
			area : area,
			content: base_url + '/joblog/logDetailPageByDag?jobId=' + jobId+"&record="+record
		});
		//layer.full(dialogIndex);
	}

}

function toCodeEditIde(){

}

function editNode(){
	contextmenuSelectNode['edit'] = true ;
	showSelectJob(contextmenuSelectNode) ;
}

function removeLink(){
	for(let i = 0 ; i < workflow.links.length ; i ++){
		let id = workflow.links[i].from+"-"+workflow.links[i].to
		let idSelect = contextmenuSelectLink.attr('from')+"-"+contextmenuSelectLink.attr('to') ;
		if(id == idSelect){
			d3.select("#"+idSelect).remove() ;
			workflow.links.splice(i,1) ;
			break ;
		}
	}
	contextmenuSelectLink = null ;
}

function removeNode(){
	if(contextmenuSelectNode){
		for(let i = 0 ; i < workflow.nodes.length ; i ++){
			if(workflow.nodes[i].id == contextmenuSelectNode.id){
				d3.select("#"+workflow.nodes[i].id).remove() ;
				d3.select("#node-text-"+workflow.nodes[i].id).remove() ;
				workflow.nodes.splice(i,1) ;
				break ;
			}
		}
		for(let i = 0 ; i < workflow.links.length ; i ++){
			if(workflow.links[i].from == contextmenuSelectNode.id || workflow.links[i].to == contextmenuSelectNode.id){
				d3.select("#"+workflow.links[i].from+"-"+workflow.links[i].to).remove() ;
				workflow.links.splice(i,1) ;
				i -- ;
			}
		}
	}
}

function triggerJob(){
	let dagJobId = $("#dagJobId").val() ;
	layer.confirm('确认要调度一次吗?', {icon: 3, title:'操作提示'}, function(index){
		$.ajax({
			type : 'POST',
			url : base_url + "/dagjob/trigger",
			data : {
				"id" : dagJobId,
				"executorParam" : "",
				"addressList" : ""
			},
			dataType : "json",
			success : function(data){
				if (data.code == 200) {
					$('#jobTriggerModal').modal('hide');

					layer.msg( I18n.jobinfo_opt_run + I18n.system_success );
				} else {
					layer.msg( data.msg || I18n.jobinfo_opt_run + I18n.system_fail );
				}
			}
		});

		layer.close(index);
	});
}

function getDate(str){
	var oDate = new Date(str),
		oYear = oDate.getFullYear(),
		oMonth = oDate.getMonth()+1,
		oDay = oDate.getDate(),
		oHour = oDate.getHours(),
		oMin = oDate.getMinutes(),
		oSen = oDate.getSeconds(),
		oTime = oYear +'-'+ getzf(oMonth) +'-'+ getzf(oDay) +' '+ getzf(oHour) +':'+ getzf(oMin) //+':'+getzf(oSen);//最后拼接时间
	return oTime;
};
//补0操作
function getzf(num){
	if(parseInt(num) < 10){
		num = '0'+num;
	}
	return num;
}


