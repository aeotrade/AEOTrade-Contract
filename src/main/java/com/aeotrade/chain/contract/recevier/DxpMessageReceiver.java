package com.aeotrade.chain.contract.recevier;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.CharsetDetector;
import cn.hutool.core.util.NumberUtil;
import com.aeotrade.chain.contract.constants.ContractConstants;
import com.aeotrade.chain.contract.constants.ContractVariableConstants;
import com.aeotrade.chain.contract.message.AddInfo;
import com.aeotrade.chain.contract.message.DxpMsg;
import com.aeotrade.chain.contract.po.Contract;
import com.aeotrade.chain.contract.po.ContractRecord;
import com.aeotrade.chain.contract.service.IContractRecordService;
import com.aeotrade.chain.contract.service.IContractService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.impl.javax.el.PropertyNotFoundException;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.aeotrade.chain.contract.constants.MessageConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DxpMessageReceiver {
    private final ProcessEngine processEngine;
    private final IContractRecordService contractRecordService;
    private final IContractService contractService;

    @RabbitListener(queues = "${contract.message.receivequeue}")
    @Transactional(rollbackFor = Exception.class)
    public void receiveMessage(DxpMsg dxpMsg) {
        if(dxpMsg==null){
            log.warn("报文为空，无法处理。");
            return;
        }

        if(dxpMsg.getTransInfo()==null){
            log.warn("DXP报文传输信息为空：{}", dxpMsg);
            return;
        }

        AddInfo addInfo = verifyDxpMsg(dxpMsg);
        if (addInfo == null){
            return;
        }

        AddInfo.BizKey bizKey=addInfo.getBizKey();
        String processTemplateNo=null;
        String bizCollaborationId=null;
        String taskId=null;
        for (AddInfo.BizKey.Key key : bizKey.getKey()) {
            if(BIZKEY_PROCESS_INSTANCE_KEY.equals(key.getName())){
                bizCollaborationId=key.getValue();
            }else if(BIZKEY_PROCESS_TEMPLATE_KEY.equals(key.getName())){
                processTemplateNo=key.getValue();
            }else if(BIZKEY_PROCESS_ACTIVITYID_KEY.equals(key.getName())){
                taskId=key.getValue();
            }
        }
        if(StringUtils.isEmpty(bizCollaborationId)){
            log.warn("消息协作记录编号为空，无法进入合约。");
            return;
        }
        if(!NumberUtil.isLong(processTemplateNo)){
            log.warn("消息的协作合约编号不符合目前规范。");
            return;
        }
        log.info("开始处理合约编号：{}，协作编号：{}，活动编号：{}",processTemplateNo,bizCollaborationId,taskId);
        ContractRecord contractRecord=new ContractRecord();
        contractRecord.setContractId(Long.valueOf(processTemplateNo));
        contractRecord.setOrgContractRecordNo(bizCollaborationId);
        QueryWrapper<ContractRecord> contractRecordQueryWrapper=new QueryWrapper<>(contractRecord);
        List<ContractRecord> contractRecords=this.contractRecordService.list(contractRecordQueryWrapper);
        boolean newInstance=false;
        if(CollUtil.isEmpty(contractRecords)){
            if(StringUtils.isEmpty(processTemplateNo)){
                log.warn("消息协作记录编号是未启动合约实例，但消息不存在合约编号。");
                return;
            }
            contractRecord=this.startNewProcess(processTemplateNo,bizCollaborationId,dxpMsg);
            if(contractRecord==null){
                return;
            }
            newInstance=true;
        }else{
            contractRecord=contractRecords.get(0);
        }

        if(StringUtils.isBlank(taskId)){
            log.warn("消息的协作记录编号是已启动流程，但消息不存在活动编号。");
            return;
        }
        boolean done = this.doTask(newInstance, contractRecord, taskId, dxpMsg);
        log.info("完成合约编号：{}，协作编号：{}，活动编号：{}，结果：{}",processTemplateNo,bizCollaborationId,taskId,done);

    }

    private AddInfo verifyDxpMsg(DxpMsg dxpMsg) {
        if(StringUtils.isEmpty(dxpMsg.getTransInfo().getSenderId())){
            log.warn("发送者为空，不再进行处理。");
            return null;
        }

        if(dxpMsg.getAddInfo()==null){
            log.warn("附加信息为空，流程相关信息不存在。");
            return null;
        }
        AddInfo addInfo=dxpMsg.getAddInfo();
        if(addInfo.getBizKey()==null){
            log.warn("bizkey为空，流程相关信息不存在。");
            return null;
        }
        return addInfo;
    }

    private ContractRecord startNewProcess(String processTemplateNo,String bizCollaborationId,DxpMsg dxpMsg){
        Contract contract=new Contract();
        contract.setContractId(Long.valueOf(processTemplateNo));
        QueryWrapper<Contract> contractQueryWrapper=new QueryWrapper<>(contract);
        List<Contract> contractList=this.contractService.list(contractQueryWrapper);

        if(CollUtil.isEmpty(contractList)){
            log.warn("消息的合约编号无对应合约。");
            return null;
        }
        contract=contractList.get(0);
        if(!ContractConstants.RELEASE_STATUS_PUBLISHED.getCode().equals(contract.getReleaseStatus())){
            log.warn("消息的合约编号未启用，无法启动合约。");
            return null;
        }

        if(StringUtils.isEmpty(contract.getProcessDefinitionId())){
            log.warn("消息的合约编号未发布，无法启动合约。");
            return null;
        }


        VariableMap variables = getVariables(dxpMsg);
        ProcessInstance processInstance;
        try {
            processInstance = processEngine.getRuntimeService().startProcessInstanceById(contract.getProcessDefinitionId(),variables);
        } catch (Exception e) {
            log.error("启动流程时出错。",e);
            log.warn("无法启动消息对应的合约实例，具体信息请查看日志。");
            return null;
        }
        ContractRecord contractRecord = new ContractRecord();
        contractRecord.setContractRecordId(IdWorker.getId());
        contractRecord.setContractId(contract.getContractId());
        contractRecord.setName(contract.getName());
        contractRecord.setOrganizationId(contract.getOrganizationId());
        contractRecord.setOrgContractRecordNo(bizCollaborationId);
        contractRecord.setProcessInstanceId(processInstance.getProcessInstanceId());
        contractRecord.setProcessDefinitionId(contract.getProcessDefinitionId());
        contractRecord.setCreateDate(new Date());
        this.contractRecordService.save(contractRecord);

        return contractRecord;
    }

    private VariableMap getVariables(DxpMsg dxpMsg) {
        TypedValue bizData=null;
        VariableMap variableMap= Variables.createVariables().putValueTyped(ContractVariableConstants.TRANSFER_DATA_VARIABLE_NAME,Variables.stringValue(dxpMsg.toString(),true)).putValueTyped(ContractVariableConstants.MESSAGE_TYPE_VARIABLE_NAME,Variables.stringValue(dxpMsg.getTransInfo().getMsgType(),true));
        if(dxpMsg.getData()!=null){
            Charset charset=findBizDataCharSet(dxpMsg);
            if(charset!=null){
                bizData=Variables.stringValue(new String(dxpMsg.getData().getValue(),charset),true);
                variableMap=variableMap.putValue(ContractVariableConstants.BIZ_DATA_CHARSET_VARIABLE_NAME,charset.name());
            }else {
                bizData=Variables.byteArrayValue(dxpMsg.getData().getValue(),true);
            }

        }
        return variableMap.putValueTyped(ContractVariableConstants.BIZ_DATA_VARIABLE_NAME,bizData);
    }

    private Charset findBizDataCharSet(DxpMsg dxpMsg){
        Charset charset=null;
        if(dxpMsg.getAddInfo()!=null && dxpMsg.getAddInfo().getIsText()!=null  ){
            if(StringUtils.isNotEmpty(dxpMsg.getAddInfo().getIsText().getEncode()) && Charset.isSupported(dxpMsg.getAddInfo().getIsText().getEncode())){
                charset=Charset.forName(dxpMsg.getAddInfo().getIsText().getEncode());
            }
            if(charset==null){
                ByteArrayInputStream inputStream=new ByteArrayInputStream(dxpMsg.getData().getValue());
                charset= CharsetDetector.detect(inputStream);
            }
            if(charset==null){
                Collection<Charset> charsets=Charset.availableCharsets().values();
                ByteArrayInputStream inputStream=new ByteArrayInputStream(dxpMsg.getData().getValue());
                charset=CharsetDetector.detect(inputStream,charsets.toArray(new Charset[0]));
            }
        }
        return charset;
    }

    private boolean doTask(boolean newInstance,ContractRecord contractRecord,String taskId,DxpMsg dxpMsg){
        RuntimeService runtimeService=processEngine.getRuntimeService();
        ActivityInstance activityInstance=verifyProcessInstanceError(newInstance, contractRecord, taskId, runtimeService);
        if(activityInstance==null){
            return newInstance;
        }
        boolean hasTask2Do=false;
        for (ActivityInstance childActivityInstance : activityInstance.getChildActivityInstances()) {
            VariableMap variables = getVariables(dxpMsg);
            try {
                hasTask2Do=toDoTask(contractRecord.getProcessInstanceId(),taskId,childActivityInstance,variables);
            } catch (Exception e){
                processExceptionInTask(e);
                return false;
            }
        }

        if(!hasTask2Do){
            log.warn("消息的所对应的活动已经完成或未到执行时。");
        }

        return hasTask2Do;
    }


    private boolean toDoTask(String processInstanceId,String taskId,ActivityInstance childActivityInstance,VariableMap variables){
        boolean flag=false;
        if (StringUtils.equals(ActivityTypes.TASK_USER_TASK, childActivityInstance.getActivityType())) {
            Task task = processEngine.getTaskService().createTaskQuery().processInstanceId(processInstanceId).taskDefinitionKey(taskId).singleResult();
            if(task!=null){
                processEngine.getTaskService().complete(task.getId(),variables);
                flag=true;
            }
        } else if (StringUtils.equals(ActivityTypes.TASK_RECEIVE_TASK, childActivityInstance.getActivityType()) && StringUtils.equals(childActivityInstance.getActivityId(),taskId)) {
            for (String executionId : childActivityInstance.getExecutionIds()) {
                processEngine.getRuntimeService().signal(executionId,variables);
            }
            flag=true;
        } else if (StringUtils.equals(ActivityTypes.MULTI_INSTANCE_BODY, childActivityInstance.getActivityType())) {
            flag=loopToDoTask(processInstanceId,taskId,childActivityInstance,variables);
        }else if(StringUtils.equals(ActivityTypes.SUB_PROCESS,childActivityInstance.getActivityType())){
            flag=loopToDoTask(processInstanceId,taskId,childActivityInstance,variables);
        }
        return flag;

    }

    private boolean loopToDoTask(String processInstanceId,String taskId,ActivityInstance childActivityInstance,VariableMap variables){
        Boolean flag=null;
        for (ActivityInstance activityInstance : childActivityInstance.getChildActivityInstances()) {
            if(flag==null){
                flag=toDoTask(processInstanceId,taskId,activityInstance,variables);
            }else {
                flag=flag&&toDoTask(processInstanceId,taskId,activityInstance,variables);
            }
        }
        return flag != null && flag;
    }

    private void processExceptionInTask(Exception e) {
        String messgage="合约处理出现问题，具体请查看程序日志。";
        if(e instanceof ProcessEngineException && e.getCause() instanceof PropertyNotFoundException){
            messgage="合约配置出现问题，活动所使用的属性未找到。";
        }
        log.warn("进行任务时出错,{}",messgage, e);
    }

    private ActivityInstance verifyProcessInstanceError(boolean newInstance,ContractRecord contractRecord, String taskId, RuntimeService runtimeService) {
        ProcessInstance processInstance= runtimeService.createProcessInstanceQuery().processInstanceId(contractRecord.getProcessInstanceId()).singleResult();
        if(processInstance==null){
            if(newInstance){
                List<HistoricActivityInstance> activityInstances=this.processEngine.getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(contractRecord.getProcessInstanceId()).list();
                for (HistoricActivityInstance activityInstance : activityInstances) {
                    if(activityInstance.getActivityId().equals(taskId)){
                        return null;
                    }
                }
            }
            log.warn("协作记录编号所对应的合约实例不存在。");
            return null;
        }
        BpmnModelInstance modelInstance =processEngine.getRepositoryService().getBpmnModelInstance(processInstance.getProcessDefinitionId());
        if(modelInstance.getModelElementById(taskId)==null){
            log.warn("消息所对应的活动在合约中不存在。");
            return null;
        }
        boolean finish=processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(contractRecord.getProcessInstanceId()).finished().count()>0;
        if(finish){
            log.warn("消息所对应的合约实例已经完成。");
            return null;
        }
        ActivityInstance activityInstance=runtimeService.getActivityInstance(contractRecord.getProcessInstanceId());
        if(activityInstance==null){
            log.warn("协作记录编号所对应的合约实例不存在待处理的活动。");
            return null;
        }
        return activityInstance;
    }




}
