package com.aeotrade.chain.contract.dealer;

import com.aeotrade.chain.contract.constants.ContractVariableConstants;
import com.aeotrade.chain.contract.constants.MessageConstants;
import com.aeotrade.chain.contract.message.AddInfo;
import com.aeotrade.chain.contract.message.DxpMsg;
import com.aeotrade.chain.contract.message.TransInfoType;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.BytesValue;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Setter
public class DxpMsgTransferComponet implements JavaDelegate {

    @NonNull
    private final String processDxpId;
    @NonNull
    private final RabbitTemplate rabbitTemplate;
    @NonNull
    private final Jaxb2Marshaller  jaxb2Marshaller;
    private final DatatypeFactory datatypeFactory;
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        if(!delegateExecution.hasVariable(ContractVariableConstants.TRANSFER_DATA_VARIABLE_NAME)){
            log.warn("本活动未接收到传输消息，流程实例:{}",delegateExecution.getProcessInstance());
            throw new BpmnError("未接收到传输消息");
        }
        if(!delegateExecution.hasVariable(ContractVariableConstants.RECEIVERS_VARIABLE_NAME)){
            log.warn("本活动未配置接收者，流程实例:{}",delegateExecution.getProcessInstance());
            throw new BpmnError("未配置接收者");
        }

        StringValue transData=delegateExecution.getVariableTyped(ContractVariableConstants.TRANSFER_DATA_VARIABLE_NAME);
        TypedValue bizDataTypedValue=delegateExecution.getVariableTyped(ContractVariableConstants.BIZ_DATA_VARIABLE_NAME);
        TypedValue typedValue=delegateExecution.getVariableTyped(ContractVariableConstants.RECEIVERS_VARIABLE_NAME);
        String receiverId = findReceivers(typedValue);

        if(StringUtils.isEmpty(receiverId)){
            log.warn("接收者为空，流程信息：{}",delegateExecution.getProcessInstanceId());
            return;
        }
        DxpMsg dxpMsg;
        try{
            StringReader reader=new StringReader(transData.getValue());
            dxpMsg=(DxpMsg) this.jaxb2Marshaller.unmarshal(new StreamSource(reader));
        }catch (Exception e){
            log.error("将传输报文转换时出错。",e);
            log.error("传输报文:{}",transData.getValue());
            return;
        }

        dxpMsg.getTransInfo().setSenderId(this.processDxpId);
        TransInfoType.ReceiverIds receiverIds=dxpMsg.getTransInfo().getReceiverIds();
        receiverIds.getReceiverId().clear();
        receiverIds.getReceiverId().add(receiverId);

        reformMsg(delegateExecution, dxpMsg);

        doSendMsg(delegateExecution, bizDataTypedValue, dxpMsg);

    }

    /**
     * 根据当前活动，重新设置报文活动id
     *
     * @param delegateExecution 委托执行
     * @param dxpMsgDocument    dxp味精文档
     */
    private void reformMsg(DelegateExecution delegateExecution, DxpMsg dxpMsgDocument) {
        AddInfo addInfo= dxpMsgDocument.getAddInfo();
        if(addInfo==null){
            addInfo=new AddInfo();
            dxpMsgDocument.setAddInfo(addInfo);
        }
        AddInfo.BizKey bizKey=addInfo.getBizKey();
        if(bizKey==null){
            bizKey=new AddInfo.BizKey();
            addInfo.setBizKey(bizKey);
        }

        List<AddInfo.BizKey.Key> keyList=bizKey.getKey();
        AddInfo.BizKey.Key key=null;
        if(keyList==null || keyList.isEmpty()){
            key=new AddInfo.BizKey.Key();
            key.setName(MessageConstants.BIZKEY_PROCESS_ACTIVITYID_KEY);
        }else {
            for (AddInfo.BizKey.Key keyItem : keyList) {
                if(MessageConstants.BIZKEY_PROCESS_ACTIVITYID_KEY.equals(keyItem.getName())){
                    key=keyItem;
                }
            }
            if(key==null){
                key=new AddInfo.BizKey.Key();
                key.setName(MessageConstants.BIZKEY_PROCESS_ACTIVITYID_KEY);
            }
        }
        key.setValue(delegateExecution.getCurrentActivityId());
    }

    private String findReceivers(TypedValue typedValue) {
        if(typedValue.getType().isPrimitiveValueType() && ValueType.STRING.equals(typedValue.getType())){
                StringValue reciever=(StringValue) typedValue;
                return reciever.getValue();
            }
        return null;
    }


    private void doSendMsg(DelegateExecution delegateExecution, TypedValue bizDataTypedValue, DxpMsg dxpMsg) {
        TransInfoType transInfo = dxpMsg.getTransInfo();
        transInfo.setCopMsgId(transInfo.getSenderId()+ UUID.randomUUID().toString());
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        transInfo.setCreatTime(datatypeFactory.newXMLGregorianCalendar(gregorianCalendar));
        if(bizDataTypedValue !=null && bizDataTypedValue.getValue()!=null && bizDataTypedValue.getType().isPrimitiveValueType()){
            if(ValueType.STRING.equals(bizDataTypedValue.getType())){
                StringValue bizData=(StringValue) bizDataTypedValue;
                Charset charset=Charset.forName((String)delegateExecution.getVariable(ContractVariableConstants.BIZ_DATA_CHARSET_VARIABLE_NAME));
                dxpMsg.getData().setValue(bizData.getValue().getBytes(charset));
            }else if(ValueType.BYTES.equals(bizDataTypedValue.getType())){
                BytesValue bizData=(BytesValue) bizDataTypedValue;
                dxpMsg.getData().setValue(bizData.getValue());
            }
        }

        log.info("发送消息，消息ID：{}，发送者：{}，接收者：{}，附属业务信息：{}",dxpMsg.getTransInfo().getCopMsgId(),dxpMsg.getTransInfo().getSenderId(),dxpMsg.getTransInfo().getReceiverIds(),dxpMsg.getAddInfo().getBizKey());
        rabbitTemplate.convertAndSend(dxpMsg);
    }

}
