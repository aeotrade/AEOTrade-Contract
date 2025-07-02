package com.aeotrade.chain.contract.util;

import com.aeotrade.chain.contract.bpmn.ProcessValidator;
import com.aeotrade.chain.contract.bpmn.TaskWrapper;
import com.aeotrade.chain.contract.exception.BpmnModelParseException;
import com.aeotrade.chain.contract.exception.ExtensionPropertyParseException;
import com.aeotrade.chain.contract.exception.ProcessParseException;
import com.aeotrade.chain.contract.vo.ResultBean;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.validation.ModelElementValidator;
import org.camunda.bpm.model.xml.validation.ValidationResult;
import org.camunda.bpm.model.xml.validation.ValidationResults;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.aeotrade.chain.contract.constants.ContractVariableConstants.DXP_SEND_COMPONET_BEAN_NAME;
import static com.aeotrade.chain.contract.vo.StatusEnum.PARAMETER_DISCREPANCY;


@UtilityClass
@Slf4j
public class ProcessUtil {
    private static final String TRANSFER_COMPONET_BEAN_EXPRESS="${"+DXP_SEND_COMPONET_BEAN_NAME+"}";

    public void fillSendtaskImplementation(Process process){
        if(process ==null){
            return;
        }
        List<Task> allTaskInProcess = getAllTaskInProcess(process);
        for (Task taskInProcess : allTaskInProcess) {
            if(taskInProcess instanceof SendTask){
                ((SendTask)taskInProcess).setCamundaDelegateExpression(TRANSFER_COMPONET_BEAN_EXPRESS);
            }
        }
    }

    public List<TaskWrapper> getAllTaskWrapperWithMainActivityEnd(Process process){
        List<TaskWrapper> allTaskWrapperInProcesses = getAllTaskProxyInProcess(process);
        List<TaskWrapper> mainActivityList=new ArrayList<>();
        List<TaskWrapper> commonTaskList=new ArrayList<>();
        for (TaskWrapper taskWrapperInProcess : allTaskWrapperInProcesses) {
            if(taskWrapperInProcess.isMainActivity()){
                mainActivityList.add(taskWrapperInProcess);
            }else {
                commonTaskList.add(taskWrapperInProcess);
            }
        }
        allTaskWrapperInProcesses.clear();
        allTaskWrapperInProcesses.addAll(commonTaskList);
        allTaskWrapperInProcesses.addAll(mainActivityList);

        return allTaskWrapperInProcesses;
    }

    public List<TaskWrapper> getAllTaskWrapperWithMainActivityFront(Process process){
        List<TaskWrapper> result=getAllTaskWrapperWithMainActivityEnd(process);
        Collections.reverse(result);
        return result;
    }

    public List<TaskWrapper> getAllTaskProxyInProcess(Process process){
        List<Task> taskList=getAllTaskInProcess(process);
        List<TaskWrapper> taskWrapperList =new ArrayList<>(taskList.size());
        for (Task task : taskList) {
            TaskWrapper taskWrapper =new TaskWrapper(task);
            taskWrapperList.add(taskWrapper);
        }
        return taskWrapperList;
    }



    public List<Task> getAllTaskInProcess(Process process){
        return getAllTaskInModelElementInstance(process);
    }

    private List<Task> getAllTaskInModelElementInstance(ModelElementInstance modelElementInstance){
        Collection<Task> allTasks =modelElementInstance.getChildElementsByType(Task.class);
        Collection<SubProcess> allSubProcess = modelElementInstance.getChildElementsByType(SubProcess.class);
        if(!CollectionUtils.isEmpty(allSubProcess)){
            for (SubProcess process : allSubProcess) {
                allTasks.addAll(getAllTaskInModelElementInstance(process));
            }
        }
        return new ArrayList<>(allTasks);
    }

    public boolean validateProcess(String processDefinition, ResultBean<?> resultBean){
        if(StringUtils.isBlank(processDefinition)){
            resultBean.setStatusEnum(PARAMETER_DISCREPANCY);
            resultBean.setMessage("流程图为空！");
            return false;
        }
        BpmnModelInstance bpmnModelInstance = null;
        try(InputStream stream = new ByteArrayInputStream(processDefinition.getBytes(StandardCharsets.UTF_8))) {
            bpmnModelInstance = Bpmn.readModelFromStream(stream);
        } catch (Exception e) {
            log.error("流程图解析异常.",e);
            resultBean.setStatusEnum(PARAMETER_DISCREPANCY);
            resultBean.setMessage("流程图解析出错，请检查是否规范！");
            return false;
        }
        Collection<ModelElementValidator<?>> modelElementValidators=new ArrayList<>();
        modelElementValidators.add(new ProcessValidator());
        ValidationResults validationResults=bpmnModelInstance.validate(modelElementValidators);
        if(validationResults.hasErrors()){
            StringBuilder errors=new StringBuilder();
            if(StringUtils.isNotBlank(resultBean.getMessage())){
                errors.append(resultBean.getMessage());
                errors.append("\r\n");
            }
            for (List<ValidationResult> validationResultList : validationResults.getResults().values()) {
                for (ValidationResult validationResult : validationResultList) {
                    errors.append(validationResult.getMessage());
                    errors.append("\r\n");
                }
            }
            resultBean.setStatusEnum(PARAMETER_DISCREPANCY);
            resultBean.setMessage("流程定义缺少活动配置信息");
            return false;
        }
        return true;
    }

    public Process getProcessFromBpmnModel(BpmnModelInstance bpmnModelInstance)throws ProcessParseException {
        if(null!=bpmnModelInstance){
            Collection<Process> processes=bpmnModelInstance.getModelElementsByType(Process.class);
            if(processes.size()!=1){
                throw new ProcessParseException("只允许包含一个流程图！");
            }
            return processes.iterator().next();
        }else {
            throw new ProcessParseException("未解析到流程图");
        }
    }

    public Process getProcessFromXml(byte[] xml) throws BpmnModelParseException, ProcessParseException {
        return getProcessFromBpmnModel(parseBpmnModel(xml));
    }


    public BpmnModelInstance parseBpmnModel(byte[] xml)throws BpmnModelParseException{
        try (InputStream stream = new ByteArrayInputStream(xml)) {
            return Bpmn.readModelFromStream(stream);
        } catch (Exception e) {
            log.error("流程图解析异常.", e);
            throw new BpmnModelParseException("流程图解析出错，请检查是否规范！");
        }
    }

    public Collection<CamundaProperty> getCamundaPropertyInTask(Task task)throws ExtensionPropertyParseException {
        ExtensionElements extensionElements = task.getExtensionElements();
        if(null==extensionElements){
            throw new ExtensionPropertyParseException("流程定义缺少活动配置信息");
        }
        Collection<CamundaProperty> properties = extensionElements.getElementsQuery()
                .filterByType(CamundaProperties.class)
                .singleResult()
                .getCamundaProperties();
        if(CollectionUtils.isEmpty(properties)){
            throw new ExtensionPropertyParseException("合约不符合要求，请调整合约后重试");
        }
        return properties;
    }

    public CamundaProperty setProperty2Task(String key,String value,Task task){
        CamundaProperty camundaProperty = task.getModelInstance().newInstance(CamundaProperty.class);
        camundaProperty.setCamundaName(key);
        camundaProperty.setCamundaValue(value);
        return setProperty2Task(camundaProperty,task);
    }

    public CamundaProperty setProperty2Task(CamundaProperty source, Task task){
        if(source==null){
            return null;
        }
        ExtensionElements extensionElements=task.getExtensionElements();
        if(extensionElements==null){
            extensionElements=task.getModelInstance().newInstance(ExtensionElements.class);
            task.setExtensionElements(extensionElements);
        }

        Collection<CamundaProperties> camundaPropertiesCollection = extensionElements.getChildElementsByType(CamundaProperties.class);
        CamundaProperties camundaProperties;
        if(CollectionUtils.isEmpty(camundaPropertiesCollection)){
            camundaProperties =extensionElements.addExtensionElement(CamundaProperties.class);
        }else {
            camundaProperties=camundaPropertiesCollection.iterator().next();
        }
        Collection<CamundaProperty> properties=camundaProperties.getCamundaProperties();
        CamundaProperty target=null;
        for (CamundaProperty property : properties) {
            if(StringUtils.equals(source.getCamundaName(),property.getCamundaName())){
                property.setCamundaValue(source.getCamundaValue());
                target=property;
            }
        }
        if(target==null){
            camundaProperties.addChildElement(source);
            target=source;
        }

        return target;

    }

}
