package com.aeotrade.chain.contract.controller;

import com.aeotrade.chain.contract.connectormanage.ConnectorManageClient;
import com.aeotrade.chain.contract.connectormanage.ConnectorTask;
import com.aeotrade.chain.contract.connectormanage.ConnectorTaskResult;
import com.aeotrade.chain.contract.po.ContractOrgRelease;
import com.aeotrade.chain.contract.po.ContractRecord;
import com.aeotrade.chain.contract.service.IContractOrgReleaseService;
import com.aeotrade.chain.contract.service.IContractRecordService;
import com.aeotrade.chain.contract.vo.ContractProcessListVo;
import com.aeotrade.chain.contract.vo.ContractProcessVo;
import com.aeotrade.chain.contract.vo.ResultBean;
import com.aeotrade.chain.contract.vo.StatusEnum;
import com.alibaba.cloud.commons.io.IOUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static com.aeotrade.chain.contract.constants.ContractVariableConstants.MEMBER_CONNECTOR_TASK_ID_NAME;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author chl
 * @since 2022-10-21
 */
@RestController
@RequestMapping("/contractprocess")
@Slf4j
public class ContractProcessController {
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private IContractRecordService iContractRecordService;
    @Autowired
    private ConnectorManageClient instanceClient;
    @Autowired
    private IContractOrgReleaseService iContractOrgReleaseService;
    @GetMapping()
    public ResponseEntity<ResultBean<ContractProcessListVo>> indexList(@RequestParam(name = "contractrecordid", required = false) @NotBlank Long contractRecordId,
                                                                       @RequestHeader(name = "memberId")@NotBlank String entId) throws IOException {
        ResultBean<ContractProcessListVo> resultBean = new ResultBean<>();
        ContractRecord contractRecord = iContractRecordService.getById(contractRecordId);
        if(null==contractRecord || StringUtils.isBlank(contractRecord.getProcessInstanceId())){
            resultBean.setStatusEnum(StatusEnum.SUCCESS_NO_DATA);
            resultBean.setMessage("没有相关数据");
            return ResponseEntity.ok(resultBean);
        }
        //检查该合约是否为参与方
        if (!StringUtils.equals(contractRecord.getOrganizationId(),entId)){
            ContractOrgRelease contractOrgRelease = new ContractOrgRelease();
            contractOrgRelease.setCollaborationOrgId(entId);
            contractOrgRelease.setContractId(contractRecord.getContractId());
            QueryWrapper<ContractOrgRelease> releaseQueryWrapper = new QueryWrapper<>(contractOrgRelease);
            long count = iContractOrgReleaseService.count(releaseQueryWrapper);
            if (count<=0){
                resultBean.setStatusEnum(StatusEnum.SUCCESS_NO_DATA);
                resultBean.setMessage("没有相关数据");
                return ResponseEntity.ok(resultBean);
            }
        }
        List<ContractProcessVo> contractProcessVos =this.getProcessAllActivity(contractRecord.getProcessInstanceId(), entId);

        ContractProcessListVo contractProcessListVO = new ContractProcessListVo();
        contractProcessListVO.setContractProcessVos(contractProcessVos);

        RepositoryService repositoryService = this.processEngine.getRepositoryService();
        InputStream processModel = repositoryService.getProcessModel(contractRecord.getProcessDefinitionId());
        String str = IOUtils.toString(processModel, StandardCharsets.UTF_8);
        contractProcessListVO.setProcessDefine(str);

        resultBean.setData(contractProcessListVO);
        resultBean.setStatusEnum(StatusEnum.SUCCESS);
        return ResponseEntity.ok(resultBean);
    }

    private List<ContractProcessVo> getProcessAllActivity(String processInstanceId,String entId){
        HistoryService historyService = this.processEngine.getHistoryService();
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).finished()
                .orderByHistoricActivityInstanceEndTime()
                .asc()
                .list();
        HashMap<String, String> map = new HashMap<>();
        if(!CollectionUtils.isEmpty(list)){
            map.putAll(getPropertyByProcessDefinitionId(list.get(0).getProcessDefinitionId()));
        }
        List<ContractProcessVo> contractProcessVos = new ArrayList<>();
        for (HistoricActivityInstance historicActivityInstance : list) {
            if(historicActivityInstance.getEndTime()==null){
                continue;
            }
            ContractProcessVo contractProcessVO = new ContractProcessVo();
            contractProcessVO.setActivityType(historicActivityInstance.getActivityType());
            contractProcessVO.setActivityId(historicActivityInstance.getActivityId());
            contractProcessVO.setActivityName(historicActivityInstance.getActivityName());
            contractProcessVO.setActivityTriggerTime(historicActivityInstance.getEndTime());
            //调用接口获取主数据模型
            if(historicActivityInstance.getActivityType().equalsIgnoreCase(ActivityTypes.TASK_SEND_TASK) || historicActivityInstance.getActivityType().equalsIgnoreCase(ActivityTypes.TASK_RECEIVE_TASK)){
                contractProcessVO.setOnTable(true);
                getTaskMasterDataModelName(entId, map, historicActivityInstance, contractProcessVO);
            }else{
                contractProcessVO.setOnTable(false);
            }
            contractProcessVos.add(contractProcessVO);
        }
        return contractProcessVos;
    }

    private void getTaskMasterDataModelName(String entId, HashMap<String, String> map, HistoricActivityInstance historicActivityInstance, ContractProcessVo contractProcessVO) {
        String id="";
        if(!CollectionUtils.isEmpty(map)){
            id = map.get(historicActivityInstance.getActivityId());
        }
        if(StringUtils.isNotBlank(id)){
            try {
                ConnectorTaskResult connectorTaskResult =this.instanceClient.getOrgConnectorTask(id, entId);
                ConnectorTask data = connectorTaskResult.getData();
                if(null!=data){
                    contractProcessVO.setMasterDataModelName(data.getModelName());
                }
            }catch (Exception e){
                log.error("查询连接器主数据模型出现异常，连接器任务id：{}",id,e);
            }
        }
    }


    private HashMap<String,String> getPropertyByProcessDefinitionId(String processDefinitionId){
        HashMap<String, String> map = new HashMap<>();
        ProcessDefinition processDefinition=this.processEngine.getRepositoryService().createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
        InputStream processModelIn = this.processEngine.getRepositoryService().getProcessModel(processDefinition.getId());
        BpmnModelInstance modelInstance= Bpmn.readModelFromStream(processModelIn);
        Collection<Task> tasks = modelInstance.getModelElementsByType(Task.class);

        for (Task task : tasks) {
            ExtensionElements extensionElements = task.getExtensionElements();
            Collection<CamundaProperty> properties = extensionElements.getElementsQuery()
                    .filterByType(CamundaProperties.class)
                    .singleResult()
                    .getCamundaProperties();
            for (CamundaProperty property : properties) {
                if(MEMBER_CONNECTOR_TASK_ID_NAME.equals(property.getCamundaName())){
                    map.put(task.getId(),property.getCamundaValue());
                }
            }
        }
        return map;
    }


}
