package com.aeotrade.chain.contract.util;

import com.aeotrade.chain.contract.bpmn.TaskWrapper;
import com.aeotrade.chain.contract.exception.BpmnModelParseException;
import com.aeotrade.chain.contract.exception.ProcessParseException;
import lombok.Data;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.Task;

import java.util.ArrayList;
import java.util.List;

@Data
public class ContractProcessEntity {
    private final BpmnModelInstance bpmnModelInstance ;
    private final Process process;
    private final List<TaskWrapper> tasks=new ArrayList<>();

    public ContractProcessEntity(byte[] xml) throws BpmnModelParseException, ProcessParseException {
        this.bpmnModelInstance=ProcessUtil.parseBpmnModel(xml);
        this.process=ProcessUtil.getProcessFromBpmnModel(this.bpmnModelInstance);
        List<Task> taskList=ProcessUtil.getAllTaskInProcess(this.process);
        for (Task task : taskList) {
            this.tasks.add(new TaskWrapper(task));
        }
    }
}
