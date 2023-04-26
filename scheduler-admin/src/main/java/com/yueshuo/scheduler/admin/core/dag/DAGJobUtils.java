package com.yueshuo.scheduler.admin.core.dag;

import com.yueshuo.scheduler.admin.core.model.PstDagJobInfo;
import com.yueshuo.scheduler.admin.vo.Edge;
import com.yueshuo.scheduler.admin.vo.JobDagInfoVO;
import com.yueshuo.scheduler.admin.vo.Node;
import com.yueshuo.scheduler.core.util.GsonTool;

import java.util.ArrayList;
import java.util.List;

/**
 * dag任务关系依赖相关处理
 * */
public class DAGJobUtils {

    /***
     * 获取 dag 调度的开始节点
     * @param dagInfo
     * @return
     */
    public static Node getStartNode(JobDagInfoVO dagInfo){
        Node startNode = new Node() ;
        for (Node node : dagInfo.getNodes()){
            if(node.getDataId() == 101){
                startNode = node ;
                break;
            }
        }
        return startNode;
    }

    /***
     * 获取 dag 调度的结束节点
     * @param dagInfo
     * @return
     */
    public static Node getEndNode(JobDagInfoVO dagInfo){
        Node endNode = new Node() ;
        for (Node node : dagInfo.getNodes()){
            if(node.getDataId() == 102){
                endNode = node ;
                break;
            }
        }
        return endNode;
    }

    /***
     * 获取 dag 调度的开始节点后的任务节点信息
     * @param dagInfo
     * @return
     */
    public static List<Node> getStartNodeChildJobs(JobDagInfoVO dagInfo){
        Node startNode = new Node() ;
        for (Node node : dagInfo.getNodes()){
            if(node.getDataId() == 101){
                startNode = node ;
                break;
            }
        }
        return getCurrNodeChildJobs(dagInfo,startNode) ;
    }

    /***
     * 获取 dag 调度的开始节点后的任务节点信息
     * @param dagInfo
     * @return
     */
    public static List<Node> getNodeChildJobs(PstDagJobInfo dagInfo, long jobId){
        JobDagInfoVO jobDag = GsonTool.fromJson(dagInfo.getDagInfo(),JobDagInfoVO.class) ;
        Node startNode = new Node() ;
        for (Node node : jobDag.getNodes()){
            if(node.getTaskId() == jobId){
                startNode = node ;
                break;
            }
        }
        return getCurrNodeChildJobs(jobDag,startNode) ;
    }

    public static List<Node> getCurrNodeChildJobs(JobDagInfoVO dagInfo,Node currNode){
        List<Node> results = new ArrayList<>() ;
        for (Edge link : dagInfo.getLinks()){
            if(link.getFrom().equals(currNode.getId())){
                for (Node node : dagInfo.getNodes()){
                    if(node.getId().equals(link.getTo())){
                        results.add(node) ;
                    }
                }
            }
        }
        return results ;
    }

    /***
     * 获取 dag 调度的开始节点后的任务节点信息
     * @param dagInfo
     * @return
     */
    public static List<Node> getNodeParentJobs(PstDagJobInfo dagInfo, Node currNode){
        JobDagInfoVO jobDag = GsonTool.fromJson(dagInfo.getDagInfo(),JobDagInfoVO.class) ;
        List<Node> results = new ArrayList<>() ;
        for (Edge link : jobDag.getLinks()){
            if(link.getTo().equals(currNode.getId())){
                for (Node node : jobDag.getNodes()){
                    if(node.getId().equals(link.getFrom())){
                        results.add(node) ;
                    }
                }
            }
        }
        return results ;
    }

    /**
     * 获取当前节点的后续依赖关联的所有节点，后续所有层级的关联
     * @param dagInfo
     * @param currNode
     * @return
     */
    public static List<Node> getCurrNodeAllChildPathNodes(PstDagJobInfo dagInfo, Node currNode){
        JobDagInfoVO jobDag = GsonTool.fromJson(dagInfo.getDagInfo(),JobDagInfoVO.class) ;
        List<Node> results = new ArrayList<>() ;
        for (Edge link : jobDag.getLinks()){
            if(link.getFrom().equals(currNode.getId())){
                for (Node node : jobDag.getNodes()){
                    if(node.getId().equals(link.getTo())){
                        List<Node> list =  getCurrNodeAllChildPathNodes(dagInfo,node) ;
                        results.add(node) ;
                        results.addAll(list) ;
                    }
                }
            }
        }
        return results ;
    }

    /***
     * 根据节点JOBID 和 dao调度信息获取一个节点
     * @param dagInfo
     * @return
     */
    public static  Node getNodeByJobId(PstDagJobInfo dagInfo, long jobId){
        JobDagInfoVO jobDag = GsonTool.fromJson(dagInfo.getDagInfo(),JobDagInfoVO.class) ;
        Node node = new Node() ;
        for (Node n  : jobDag.getNodes()){
            if(n.getTaskId() == jobId){
                node = n ;
                break;
            }
        }
        return node ;
    }
}
