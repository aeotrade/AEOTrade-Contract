package com.aeotrade.chain.contract.bpmn;

import com.aeotrade.chain.contract.util.ProcessUtil;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.xml.validation.ModelElementValidator;
import org.camunda.bpm.model.xml.validation.ValidationResultCollector;
import org.springframework.util.CollectionUtils;

import java.util.Collection;

public class ProcessValidator implements ModelElementValidator<Process> {
    @Override
    public Class<Process> getElementType() {
        return Process.class;
    }

    @Override
    public void validate(Process process, ValidationResultCollector validationResultCollector) {
        Collection<StartEvent> startEvents = process.getChildElementsByType(StartEvent.class);
        if(CollectionUtils.isEmpty(startEvents)){
            validationResultCollector.addError(-30000001,"合约定义没有起始点。");
        }else if(startEvents.size()>1){
            validationResultCollector.addError(-30000001,"流程不可以包含多个起始点。");
        }

        if(CollectionUtils.isEmpty(process.getChildElementsByType(EndEvent.class))){
            validationResultCollector.addError(-30000001,"合约定义没有结束点。");
        }

        if(CollectionUtils.isEmpty(ProcessUtil.getAllTaskInProcess(process))){
            validationResultCollector.addError(-30000001,"合约定义没有活动。");
        }

        Collection<FlowNode> flowNodes = process.getChildElementsByType(FlowNode.class);
        for (FlowNode flowNode : flowNodes) {
            this.validateElement(flowNode,validationResultCollector);
        }
    }

    private void validateElement(FlowNode flowNode,ValidationResultCollector validationResultCollector){
        if(flowNode instanceof StartEvent){
            this.validateOutgoing(flowNode,validationResultCollector);
        }else if(flowNode instanceof EndEvent){
            this.validateIncoming(flowNode,validationResultCollector);

        }else if(flowNode instanceof SendTask || flowNode instanceof ReceiveTask){
            this.validateIncomingAndOutgoing(flowNode,validationResultCollector);
        }else if(flowNode instanceof SubProcess){
            Collection<StartEvent> startEvents = flowNode.getChildElementsByType(StartEvent.class);
            if(CollectionUtils.isEmpty(startEvents)){
                validationResultCollector.addError(-30000001,"有子流程定义没有起始点。");
            }
            this.validateIncomingAndOutgoing(flowNode,validationResultCollector);
            Collection<FlowNode> flowNodes = flowNode.getChildElementsByType(FlowNode.class);
            for (FlowNode node : flowNodes) {
                this.validateElement(node,validationResultCollector);
            }
        }else if(flowNode instanceof SequenceFlow){
            SequenceFlow sequenceFlow=(SequenceFlow) flowNode;
            if(sequenceFlow.getSource()==null){
                validationResultCollector.addError(-30000001, "ID为["+flowNode.getName()+"]的连接线，没有任何前置活动。");
            }
            if(sequenceFlow.getTarget()==null){
                validationResultCollector.addError(-30000001, "ID为["+flowNode.getName()+"]的连接线，没有任何后续活动。");
            }
        }
    }

    private void validateIncomingAndOutgoing(FlowNode flowNode,ValidationResultCollector validationResultCollector){
        this.validateIncoming(flowNode,validationResultCollector);
        this.validateOutgoing(flowNode,validationResultCollector);
    }

    private void validateIncoming(FlowNode flowNode,ValidationResultCollector validationResultCollector){
        Collection<SequenceFlow> incoming = flowNode.getIncoming();
        if(CollectionUtils.isEmpty(incoming)){
            validationResultCollector.addError(-30000001, "活动["+flowNode.getName()+"]，没有任何前置活动。");
        }
    }

    private void validateOutgoing(FlowNode flowNode,ValidationResultCollector validationResultCollector){
        Collection<SequenceFlow> outgoing = flowNode.getOutgoing();
        if(CollectionUtils.isEmpty(outgoing)){
            validationResultCollector.addError(-30000001, "活动["+flowNode.getName()+"]，没有任何后续活动。");
        }
    }

}
