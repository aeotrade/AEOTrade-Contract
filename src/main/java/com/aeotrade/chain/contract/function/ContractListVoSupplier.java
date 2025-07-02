package com.aeotrade.chain.contract.function;

import com.aeotrade.chain.contract.bpmn.TaskWrapper;
import com.aeotrade.chain.contract.connectormanage.ConnectorManageClient;
import com.aeotrade.chain.contract.connectormanage.ConnectorTask;
import com.aeotrade.chain.contract.connectormanage.ConnectorTaskResult;
import com.aeotrade.chain.contract.constants.ContractConstants;
import com.aeotrade.chain.contract.mapstruct.ContractConvert;
import com.aeotrade.chain.contract.mapstruct.ContractOrgAliasConvert;
import com.aeotrade.chain.contract.po.Contract;
import com.aeotrade.chain.contract.po.ContractOrgAlias;
import com.aeotrade.chain.contract.service.IContractOrgAliasService;
import com.aeotrade.chain.contract.service.IContractService;
import com.aeotrade.chain.contract.util.ContractProcessEntity;
import com.aeotrade.chain.contract.util.ProcessUtil;
import com.aeotrade.chain.contract.vo.ContractListVO;
import com.aeotrade.chain.contract.vo.OrgAliasVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.aeotrade.chain.contract.constants.ContractVariableConstants.CONNECTOR_STATUS_ERROR;
import static com.aeotrade.chain.contract.constants.ContractVariableConstants.CONNECTOR_STATUS_WARNING;


@AllArgsConstructor
@Slf4j
public class ContractListVoSupplier implements Supplier<ContractListVO> {
    private final Contract contract;
    private final ConnectorManageClient connectorManageClient;
    private final IContractService iContractService;
    private final IContractOrgAliasService iContractOrgAliasService;
    private final HistoryService historyService;

    private final ContractConvert contractConvert;
    private final ContractOrgAliasConvert contractOrgAliasConvert;

    private final Authentication authentication;

    @Override
    public ContractListVO get() {

        String msg = this.refreshContract();

        ContractListVO contractListVO = this.contractConvert.dtoToListVo(contract);
        //已协作数
        if (StringUtils.isNotBlank(contract.getProcessDefinitionId())) {
            long count = historyService.createHistoricProcessInstanceQuery().processDefinitionId(contract.getProcessDefinitionId()).finished().count();
            contractListVO.setExecutedNum(Math.toIntExact(count));
        }
//        contractListVO.setExecuteStatusMessage(msg);

        //添加协作方信息
        ContractOrgAlias orgAlias = new ContractOrgAlias();
        orgAlias.setContractId(contractListVO.getContractId());
        QueryWrapper<ContractOrgAlias> contractOrgAliasQueryWrapper = new QueryWrapper<>(orgAlias);
        List<ContractOrgAlias> contractOrgAliases = iContractOrgAliasService.list(contractOrgAliasQueryWrapper);
        List<OrgAliasVo> orgAliasVos = new ArrayList<>();
        for (ContractOrgAlias alias : contractOrgAliases) {
            orgAliasVos.add(contractOrgAliasConvert.dto2vo(alias));
        }
        contractListVO.setOrgAlias(orgAliasVos);
        return contractListVO;
    }

    private String refreshContract() {
        //只有处于执行、异常、执行告警的状态，进行状态刷新和数量刷新。
        boolean refreshStatusAndNumberFlag = ContractConstants.EXECUTE_STATUS_DOING.getCode().equalsIgnoreCase(contract.getExecuteStatus())
                || ContractConstants.EXECUTE_STATUS_EXCEPTION.getCode().equalsIgnoreCase(contract.getExecuteStatus())
                || ContractConstants.EXECUTE_STATUS_DOING_WARING.getCode().equalsIgnoreCase(contract.getExecuteStatus());
        StringBuilder instanceErrorMsg = new StringBuilder();
        if (refreshStatusAndNumberFlag && StringUtils.isNotBlank(contract.getProcessDefintion())) {
            List<CompletableFuture<ConnectorTaskResult>> futures = new ArrayList<>();
            CompletableFuture<ConnectorTaskResult> mainActivityTask = null;
            ContractProcessEntity contractProcessEntity;
            try {
                contractProcessEntity = new ContractProcessEntity(contract.getProcessDefintion().getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                log.error("在获取合约列表过程中，解析合约报错。", e);
                return null;
            }
            Process process = contractProcessEntity.getProcess();
            if (process == null) {
                log.warn("bpmn中无流程定义，无法完成操作，合约编号：{}。", contract.getContractId());
                return null;
            }

            List<TaskWrapper> taskWrapperList = ProcessUtil.getAllTaskProxyInProcess(process);
            for (TaskWrapper taskWrapper : taskWrapperList) {
                CompletableFuture<ConnectorTaskResult> connectorTaskResultCompletableFuture = CompletableFuture.supplyAsync(new ConnectorTaskSupplier(taskWrapper.getMemberConnectorTaskIdString(), taskWrapper.getMemberIdString(), this.connectorManageClient,this.authentication));
                futures.add(connectorTaskResultCompletableFuture);
                if (taskWrapper.isMainActivity()) {
                    mainActivityTask = connectorTaskResultCompletableFuture;
                }
            }
            //获取连接器状态
            ConnectorTaskResult mainActivityResult = refreshTaskStatus(futures, mainActivityTask, instanceErrorMsg);
            //计算待协作数
            calculateRemainNumber(mainActivityResult);

            boolean update = this.iContractService.updateById(contract);
            if (!update) {
                log.info("更新合约状态失败,合约id={}", contract.getContractId());
            }

        }

        return instanceErrorMsg.toString();
    }

    @Nullable
    private ConnectorTaskResult refreshTaskStatus(List<CompletableFuture<ConnectorTaskResult>> futures, CompletableFuture<ConnectorTaskResult> mainActivityTask, StringBuilder instanceErrorMsg) {
        String instanceStatus = ContractConstants.EXECUTE_STATUS_DOING.getCode();
        ConnectorTaskResult mainActivityResult = null;
        int i = 1;
        for (CompletableFuture<ConnectorTaskResult> future : futures) {
            ConnectorTaskResult connectorTaskResult = null;
            try {
                //获取内容
                connectorTaskResult = future.get();
            } catch (InterruptedException e) {
                log.error("查询连接器时出现中断异常，合约id：{}", contract.getContractId(), e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("查询连接器时出现其他异常，合约id：{}", contract.getContractId(), e);
            }
            if (connectorTaskResult == null) {
                continue;
            }
            if (Objects.equals(mainActivityTask, future)) {
                mainActivityResult = connectorTaskResult;
            }
            AtomicBoolean haveErrorMsg=new AtomicBoolean(false);

            instanceStatus = dealConnectorTaskResult(instanceErrorMsg, instanceStatus, connectorTaskResult,i,haveErrorMsg);
            if(haveErrorMsg.get()){
                i++;
            }

        }

        contract.setExecuteStatus(instanceStatus);
        return mainActivityResult;
    }

    private String dealConnectorTaskResult(StringBuilder instanceErrorMsg, String instanceStatus, ConnectorTaskResult connectorTaskResult,int i,AtomicBoolean haveErrorMsg) {
        if (connectorTaskResult.getCode() == 0) {
            ConnectorTask data = connectorTaskResult.getData();
            if (CONNECTOR_STATUS_WARNING.equalsIgnoreCase(data.getStatus())) {
                if (!ContractConstants.EXECUTE_STATUS_EXCEPTION.getCode().equalsIgnoreCase(instanceStatus)) {
                    instanceStatus = ContractConstants.EXECUTE_STATUS_DOING_WARING.getCode();
                }
                instanceErrorMsg.append(i).append("、").append(data.getStatusMessage()).append("  ");
                haveErrorMsg.set(true);
            } else if (CONNECTOR_STATUS_ERROR.equalsIgnoreCase(data.getStatus())) {
                instanceStatus = ContractConstants.EXECUTE_STATUS_EXCEPTION.getCode();
                instanceErrorMsg.append(i).append("、").append(data.getStatusMessage()).append("  ");
                haveErrorMsg.set(true);
            }
        }
        return instanceStatus;
    }

    private void calculateRemainNumber(ConnectorTaskResult mainActivityResult) {
        if (mainActivityResult != null) {
            ConnectorTask data = mainActivityResult.getData();
            if(data==null){
                log.warn("查询主活动，未返回结果，无法更新此合约的待执行数，合约信息：{},获取主活动的结果：{}",this.contract,mainActivityResult);
                return;
            }
            if (data.getTargetQty() == null) {
                contract.setRemainingNum(null);
            } else if (data.getTargetQty() < 0) {
                contract.setRemainingNum(-1);
            } else {
                int targetQty = data.getTargetQty();
                int doneQty = Math.toIntExact(this.historyService.createHistoricProcessInstanceQuery().processDefinitionId(contract.getProcessDefinitionId()).finished().count());
                int remainingNum = targetQty - doneQty;
                contract.setRemainingNum(remainingNum);
                //待协作数：主活动反馈的协作总数-主活动反馈的已协作数＋协作合约流程中正在执行数
                if (remainingNum == 0) {
                    //判断是否已经完成 data.getTargetQty()=data.getDoneQty()如果相等更新状态为已完成。
                    contract.setExecuteStatus(ContractConstants.EXECUTE_STATUS_FINISH.getCode());
                }
            }
        } else {
            contract.setRemainingNum(-1);
        }



    }

    @AllArgsConstructor
    @Slf4j
    private static class ConnectorTaskSupplier implements Supplier<ConnectorTaskResult> {
        private final String connectorTaskId;
        private final String orgId;
        private final ConnectorManageClient connectorManageClient;
        private final Authentication authentication;

        @Override
        public ConnectorTaskResult get() {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            ConnectorTaskResult orgConnectorTask;
            try {
                orgConnectorTask = this.connectorManageClient.getOrgConnectorTask(connectorTaskId, orgId);
            }catch (Exception e){
                log.error("调用连接器任务详情时，出现异常。",e);
                return null;
            }

            if(orgConnectorTask.getCode()!=0){
                log.warn("请求任务详情结果未成功，任务id：{}，组织ID：{},返回结果：{}",connectorTaskId,orgId,orgConnectorTask);
            }
            return orgConnectorTask;

        }
    }
}
