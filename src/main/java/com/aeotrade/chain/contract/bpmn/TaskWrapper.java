package com.aeotrade.chain.contract.bpmn;

import com.aeotrade.chain.contract.exception.ExtensionPropertyParseException;
import com.aeotrade.chain.contract.util.ProcessUtil;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.instance.ReceiveTask;
import org.camunda.bpm.model.bpmn.instance.SendTask;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.camunda.*;

import java.util.Collection;

import static com.aeotrade.chain.contract.constants.ContractVariableConstants.*;

@ToString
@EqualsAndHashCode
public class TaskWrapper {
    private final Task task;
    private CamundaProperty aliasId;
    private CamundaProperty memberId;
    private CamundaProperty connectorId;
    private CamundaProperty mainActivity;
    private CamundaProperty memberConnectorId;
    private CamundaProperty memberConnectorTaskId;
    private CamundaProperty eventActionId;
    private CamundaProperty connectorTaskPara;


    public TaskWrapper(Task task) {
        this.task = task;
        Collection<CamundaProperty> camundaPropertyInTask;
        try {
            camundaPropertyInTask = ProcessUtil.getCamundaPropertyInTask(task);
        } catch (ExtensionPropertyParseException e) {
            return;
        }
        for (CamundaProperty property : camundaPropertyInTask) {
            if(MEMBER_CONNECTOR_ID_NAME.equals(property.getCamundaName())){
                this.setMemberConnectorId(property);
            }else if(MEMBER_CONNECTOR_TASK_ID_NAME.equals(property.getCamundaName())){
                this.setMemberConnectorTaskId(property);
            }else if(MEMBER_ID_NAME.equals(property.getCamundaName())){
                this.setMemberId(property);
            }else if(CONNECTOR_ID_NAME.equals(property.getCamundaName())){
                this.setConnectorId(property);
            }else if(EVENT_ACTION_ID_NAME.equals(property.getCamundaName())){
                this.setEventActionId(property);
            }else if(ALIAS_ID_NAME.equals(property.getCamundaName())){
                this.setAliasId(property);
            }else if(CONNECTOR_PARA_NAME.equals(property.getCamundaName())){
                this.setConnectorTaskPara(property);
            }else if(MAIN_ACTIVITY_NAME.equals(property.getCamundaName())){
                this.setMainActivity(property);
            }
        }
    }

    public String getTaskId() {
        return this.task.getId();
    }

    public void setTaskId(String taskId) {
        this.task.setId(taskId);
    }

    public String getTaskName() {
        return this.task.getName();
    }

    public void setTaskName(String taskName) {
        this.task.setName(taskName);
    }

    public CamundaProperty getAliasId() {
        return aliasId;
    }

    public String getAliasIdString() {
        return aliasId!=null?aliasId.getCamundaValue():null;
    }

    public void setAliasId(CamundaProperty aliasId) {
        if(this.aliasId==null){
            aliasId= ProcessUtil.setProperty2Task(aliasId,this.task);
        }
        this.aliasId = aliasId;

    }

    public void setAliasIdString(String aliasId) {
        if(this.aliasId==null){
            this.aliasId=ProcessUtil.setProperty2Task(ALIAS_ID_NAME,aliasId,this.task);
        }else {
            this.aliasId.setCamundaValue(aliasId);
        }

    }

    public CamundaProperty getMemberId() {
        return memberId;
    }

    public String getMemberIdString() {
        return memberId!=null?memberId.getCamundaValue():null;
    }

    public void setMemberId(CamundaProperty memberId) {
        if(this.memberId==null){
            memberId=ProcessUtil.setProperty2Task(memberId,this.task);
        }
        this.memberId = memberId;
    }
    public void setMemberIdString(String memberId) {
        if(this.memberId==null){
            this.memberId = ProcessUtil.setProperty2Task(MEMBER_ID_NAME,memberId,this.task);
        }else {
            this.memberId.setCamundaValue(memberId);
        }
    }

    public CamundaProperty getConnectorId() {
        return connectorId;
    }

    public String getConnectorIdString() {
        return connectorId!=null?connectorId.getCamundaValue():null;
    }

    public void setConnectorId(CamundaProperty connectorId) {
        if(this.connectorId==null){
            connectorId=ProcessUtil.setProperty2Task(connectorId,this.task);
        }
        this.connectorId = connectorId;
    }

    public void setConnectorIdString(String connectorId) {
        if(this.connectorId==null){
            this.connectorId=ProcessUtil.setProperty2Task(CONNECTOR_ID_NAME,connectorId,this.task);
        }else {
            this.connectorId.setCamundaValue(connectorId);
        }
    }

    public CamundaProperty getMainActivity() {
        return mainActivity;
    }

    public boolean isMainActivity() {
        return mainActivity != null && (StringUtils.isBlank(mainActivity.getCamundaValue()) || "true".equalsIgnoreCase(mainActivity.getCamundaValue()));
    }

    public void setMainActivity(CamundaProperty mainActivity) {
        if(this.mainActivity==null){
            mainActivity=ProcessUtil.setProperty2Task(mainActivity,this.task);
        }
        this.mainActivity = mainActivity;
    }

    public void setMainActivityString(String mainActivity) {
        if(this.mainActivity==null){
            this.mainActivity=ProcessUtil.setProperty2Task(MAIN_ACTIVITY_NAME,mainActivity,this.task);
        }else {
            this.mainActivity.setCamundaValue(mainActivity);
        }
    }

    public CamundaProperty getMemberConnectorId() {
        return memberConnectorId;
    }

    public String getMemberConnectorIdString() {
        return memberConnectorId !=null? memberConnectorId.getCamundaValue():null;
    }

    public void setMemberConnectorId(CamundaProperty memberConnectorId) {
        if(this.memberConnectorId ==null){
            memberConnectorId=ProcessUtil.setProperty2Task(memberConnectorId,this.task);
        }
        this.memberConnectorId = memberConnectorId;
    }

    public void setMemberConnectorIdString(String memberConnectorId) {
        if(this.memberConnectorId ==null){
            this.memberConnectorId =ProcessUtil.setProperty2Task(MEMBER_CONNECTOR_ID_NAME,memberConnectorId,this.task);
        }else {
            this.memberConnectorId.setCamundaValue(memberConnectorId);
        }
    }

    public CamundaProperty getMemberConnectorTaskId() {
        return memberConnectorTaskId;
    }

    public String getMemberConnectorTaskIdString() {
        return memberConnectorTaskId !=null? memberConnectorTaskId.getCamundaValue():null;
    }

    public void setMemberConnectorTaskId(CamundaProperty memberConnectorTaskId) {
        this.memberConnectorTaskId = ProcessUtil.setProperty2Task(memberConnectorTaskId,this.task);
    }

    public void setMemberConnectorTaskIdString(String memberConnectorTaskId) {
        if(this.memberConnectorTaskId ==null){
            this.memberConnectorTaskId = ProcessUtil.setProperty2Task(MEMBER_CONNECTOR_TASK_ID_NAME,memberConnectorTaskId,this.task);
        }else {
            this.memberConnectorTaskId.setCamundaValue(memberConnectorTaskId);
        }
    }

    public CamundaProperty getEventActionId() {
        return eventActionId;
    }

    public String getEventActionIdString() {
        return eventActionId !=null? eventActionId.getCamundaValue():null;
    }

    public void setEventActionId(CamundaProperty eventActionId) {
        if(this.eventActionId ==null){
            eventActionId=ProcessUtil.setProperty2Task(eventActionId,this.task);
        }
        this.eventActionId = eventActionId;
    }

    public void setEventActionIdString(String eventActionId) {
        if(this.eventActionId ==null){
            this.eventActionId = ProcessUtil.setProperty2Task(EVENT_ACTION_ID_NAME,eventActionId,this.task);
        }else {
            this.eventActionId.setCamundaValue(eventActionId);
        }
    }

    public CamundaProperty getConnectorTaskPara() {
        return connectorTaskPara;
    }

    public String getConnectorTaskParaString() {
        return connectorTaskPara !=null? connectorTaskPara.getCamundaValue():null;
    }

    public void setConnectorTaskPara(CamundaProperty connectorTaskPara) {
        if(this.connectorTaskPara ==null){
            connectorTaskPara=ProcessUtil.setProperty2Task(connectorTaskPara,this.task);
        }
        this.connectorTaskPara = connectorTaskPara;
    }

    public void setConnectorTaskParaString(String connectorTaskPara) {
        if(this.connectorTaskPara ==null){
            this.connectorTaskPara =ProcessUtil.setProperty2Task(CONNECTOR_PARA_NAME,connectorTaskPara,this.task);
        }else {
            this.connectorTaskPara.setCamundaValue(connectorTaskPara);
        }
    }

    public void remove(CamundaProperty camundaProperty){
        CamundaProperties camundaProperties=this.task.getExtensionElements().getElementsQuery().filterByType(CamundaProperties.class).singleResult();
        if(camundaProperties!=null){
            camundaProperties.removeChildElement(camundaProperty);
        }
    }

    public boolean isTypeOf(Class<? extends Task> target){
        if(target==null){
            return false;
        }
        return target.isInstance(this.task);
    }

    public void setDxpId(String dxpId){
        Collection<CamundaInputOutput> camundaInputOutputs= task.getExtensionElements().getChildElementsByType(CamundaInputOutput.class);
        CamundaInputOutput camundaInputOutput;
        if(camundaInputOutputs.isEmpty()){
            camundaInputOutput= task.getExtensionElements().addExtensionElement(CamundaInputOutput.class);
        }else {
            camundaInputOutput=camundaInputOutputs.iterator().next();
        }
        Collection<CamundaInputParameter> camundaInputParameters=camundaInputOutput.getCamundaInputParameters();
        CamundaInputParameter inputParameter=null;
        String inputKey="";
        if(task instanceof SendTask){
            inputKey=RECEIVERS_VARIABLE_NAME;
        }else if(task instanceof ReceiveTask){
            inputKey=SENDER_VARIABLE_NAME;
        }
        if(!camundaInputParameters.isEmpty()){
            for (CamundaInputParameter camundaInputParameter : camundaInputParameters) {
                if(inputKey.equals(camundaInputParameter.getCamundaName())){
                    inputParameter=camundaInputParameter;
                    break;
                }
            }
        }
        if(inputParameter==null){
            inputParameter= task.getModelInstance().newInstance(CamundaInputParameter.class);
            inputParameter.setCamundaName(inputKey);
            camundaInputOutput.addChildElement(inputParameter);
        }

        if(inputParameter.getValue() instanceof CamundaMap){
            inputParameter.removeValue();
        }

        if(inputParameter.getValue() instanceof CamundaList){
            CamundaList list=inputParameter.getValue();
            CamundaValue camundaValue= task.getModelInstance().newInstance(CamundaValue.class);
            camundaValue.setTextContent(dxpId);
            list.getValues().add(camundaValue);
        }else {
            inputParameter.removeValue();
            inputParameter.setTextContent(dxpId);
        }
    }

}
