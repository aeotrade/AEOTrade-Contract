package com.aeotrade.chain.contract.controller;

import cn.hutool.core.collection.CollUtil;
import com.aeotrade.chain.contract.blockchain.SmartContractClient;
import com.aeotrade.chain.contract.blockchain.Smartcontract;
import com.aeotrade.chain.contract.bpmn.TaskWrapper;
import com.aeotrade.chain.contract.connectormanage.*;
import com.aeotrade.chain.contract.constants.ContractConstants;
import com.aeotrade.chain.contract.exception.ApplicationException;
import com.aeotrade.chain.contract.exception.BpmnModelParseException;
import com.aeotrade.chain.contract.exception.ProcessParseException;
import com.aeotrade.chain.contract.exception.StatusException;
import com.aeotrade.chain.contract.function.*;
import com.aeotrade.chain.contract.mapstruct.ContractConvert;
import com.aeotrade.chain.contract.mapstruct.ContractOrgAliasConvert;
import com.aeotrade.chain.contract.po.*;
import com.aeotrade.chain.contract.service.*;
import com.aeotrade.chain.contract.util.ContractProcessEntity;
import com.aeotrade.chain.contract.util.ProcessUtil;
import com.aeotrade.chain.contract.util.UserUtil;
import com.aeotrade.chain.contract.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.ReceiveTask;
import org.camunda.bpm.model.bpmn.instance.SendTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.aeotrade.chain.contract.vo.StatusEnum.PARAMETER_DISCREPANCY;
import static com.aeotrade.chain.contract.vo.StatusEnum.SUCCESS_NO_DATA;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author chl
 * @since 2022-10-21
 */
@RestController
@RequestMapping("/contract")
@Slf4j
public class ContractController {

    @Autowired
    private IContractTemplateService iContractTemplateService;

    @Autowired
    private IContractService iContractService;

    @Autowired
    private ConnectorManageClient connectorManageClient;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private IContractTemplateOrgAliasService iContractTemplateOrgAliasService;

    @Autowired
    private IContractOrgAliasService iContractOrgAliasService;
    @Autowired
    private IContractOrgReleaseService iContractOrgReleaseService;
    @Autowired
    private SmartContractClient smartContractClient;
    @Autowired
    private ContractOrgAliasConvert contractOrgAliasConvert;
    @Autowired
    private ContractConvert contractConvert;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private IContractTemplateKindRelService iContractTemplateKindRelService;
    @Autowired
    private IContractTemplateKindService iContractTemplateKindService;
    @Autowired
    private IContractOrgConfigService iContractOrgConfigService;

    @PostMapping()
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<ResultBean<ContractVo>> createContract(@Validated @RequestBody ContractSave saveVo, @RequestHeader(name = "memberId")@NotBlank String entId) {
        log.info("新增合约，名称:{},发布状态：{}",saveVo.getName(),saveVo.getReleaseStatus());
        ResultBean<ContractVo> resultBean = new ResultBean<>();
        String contractTplId = saveVo.getContractTplId();
        ContractTemplate contractTemplate = new ContractTemplate();
        if(StringUtils.isNotBlank(contractTplId)){
            contractTemplate = iContractTemplateService.getById(Long.valueOf(contractTplId));
        }
        if(null==contractTemplate){
            resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
            resultBean.setMessage("不存在的合约模板");
            log.warn("创建合约时参数合约模板id不存在，{}",contractTplId);
            return ResponseEntity.badRequest().body(resultBean);
        }
        Contract contract = new Contract();
        contract.setContractId(IdWorker.getId());
        contract.setCustomerContractId(contract.getContractId().toString());
        contract.setOrganizationId(entId);
        contract.setIcon(contractTemplate.getIcon());
        if(StringUtils.isNotBlank(contractTplId)){
            contract.setContractTemplateId(Long.valueOf(contractTplId));
        }
        contract.setName(saveVo.getName());
        if(StringUtils.isNotBlank(contractTplId)){
            contract.setDecription(contractTemplate.getDecription());
        }else{
            contract.setDecription(saveVo.getDecription());
        }
        contract.setType(contractTemplate.getType());
        contract.setProcessDefintion(saveVo.getProcessDefine());
        contract.setVersion(contractTemplate.getVersion());
        contract.setCreateDate(new Date());
        Long userId = UserUtil.getUserId();
        contract.setCreateUid(String.valueOf(userId));
        contract.setWriteTime(new Date());
        contract.setWriteUid(String.valueOf(userId));
        contract.setReleaseStatus(saveVo.getReleaseStatus());
        contract.setExecuteStatus(ContractConstants.EXECUTE_STATUS_WAIT.getCode());
        List<OrgAliasVo> orgAliasSaves = saveVo.getOrgAlias();
        if(ContractConstants.RELEASE_STATUS_PUBLISHED.getCode().equalsIgnoreCase(saveVo.getReleaseStatus())){
            //创建合约即发布时的处理逻辑
            publishDirectlyWhenCreate(entId, resultBean, contract, orgAliasSaves);
        }
        boolean save = iContractService.save(contract);
        if(!save){
            resultBean.setStatusEnum(StatusEnum.UPDATE_FAIL);
            resultBean.setMessage("保存失败，请重新尝试后联系客服处理");
            log.error("保存合约信息失败,合约id{}",contract.getContractId());
            return ResponseEntity.badRequest().body(resultBean);
        }
        //保存合约角色表
        List<OrgAliasVo> orgAliasVos = saveOrgAliasList(contract, orgAliasSaves);
        ContractVo contractVO = this.contractConvert.dtoToVo(contract);
        contractVO.setOrgAlias(orgAliasVos);
        resultBean.setStatusEnum(StatusEnum.SUCCESS);
        resultBean.setData(contractVO);
        return ResponseEntity.ok(resultBean);
    }

    /**
     * 创建时直接发布
     *
     * @param entId         ent id
     * @param resultBean    结果豆
     * @param contract      合同
     * @param orgAliasSaves org别名保存
     *///创建合约即发布时的处理逻辑
    private void publishDirectlyWhenCreate(String entId, ResultBean<ContractVo> resultBean, Contract contract, List<OrgAliasVo> orgAliasSaves) {
        if(orgAliasSaves.size()==1 && StringUtils.equalsIgnoreCase(orgAliasSaves.get(0).getCollaborationOrgId(), entId)) {
            contract.setReleaseStatus(ContractConstants.RELEASE_STATUS_PUBLISHED.getCode());
            validateProcess(contract.getProcessDefintion(), resultBean);
            this.saveMemberConnectorId(contract, entId);
            this.createOrUpdateOrgConnectorInProcess(contract, entId);
            this.publishProcess(contract);
            this.vote2SmartContract(contract, entId);
            //添加到协作组织状态表
            this.insertOrgRelease(orgAliasSaves, contract.getContractId(), entId);
        }else{
            contract.setReleaseStatus(ContractConstants.RELEASE_STATUS_PART.getCode());
            //添加到协作组织状态表
            this.insertOrgRelease(orgAliasSaves, contract.getContractId(), entId);
        }
    }

    /**
     * 保存协作角色列表
     *
     * @param contract      合同
     * @param orgAliasSaves org别名保存
     *///保存合约角色表
    private List<OrgAliasVo> saveOrgAliasList(Contract contract, List<OrgAliasVo> orgAliasSaves) {
        List<OrgAliasVo> resultList = new ArrayList<>();
        for (OrgAliasVo orgAliasSave : orgAliasSaves) {
            ContractOrgAlias contractOrgAlias = new ContractOrgAlias();
            contractOrgAlias.setAliasSeq(IdWorker.getId());
            if (null==orgAliasSave.getAliasId()){
                contractOrgAlias.setAliasId(IdWorker.getId());
            }else if(contract.getContractTemplateId()!=null){
                //如果角色id没有在模板角色表中查到丢弃数据
                ContractTemplateOrgAlias contractTemplateOrgAlias = new ContractTemplateOrgAlias();
                contractTemplateOrgAlias.setContractTemplateId(contract.getContractTemplateId());
                contractTemplateOrgAlias.setAliasId(orgAliasSave.getAliasId());
                QueryWrapper<ContractTemplateOrgAlias> orgAliasQueryWrapper = new QueryWrapper<>(contractTemplateOrgAlias);
                long count = iContractTemplateOrgAliasService.count(orgAliasQueryWrapper);
                if (count>0){
                    contractOrgAlias.setAliasId(orgAliasSave.getAliasId());
                }
            }
            if(contractOrgAlias.getAliasId()==null){
                continue;
            }
            contractOrgAlias.setAliasName(orgAliasSave.getAliasName());
            contractOrgAlias.setContractId(contract.getContractId());
            contractOrgAlias.setCollaborationOrgId(orgAliasSave.getCollaborationOrgId());
            boolean saveOrgAlias = iContractOrgAliasService.save(contractOrgAlias);
            if(saveOrgAlias){
                OrgAliasVo orgAliasVO = this.contractOrgAliasConvert.dto2vo(contractOrgAlias);
                resultList.add(orgAliasVO);
            }else{
                log.error("保存协作角色列表失败,{}",contractOrgAlias);
                throw new ApplicationException(HttpStatus.BAD_REQUEST,StatusEnum.OPERATION_NOT_ALLOWED.getCode(),"保存失败，请重新尝试后联系客服处理");
            }
        }
        return resultList;
    }


    @PutMapping()
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<ResultBean<ContractVo>> updateContract(@Validated @RequestBody ContractUpDate contractUpDate, @RequestHeader(name = "memberId")@NotBlank String entId){
        log.info("编辑合约，合约id：{}，名称:{},发布状态：{}",contractUpDate.getContractId(),contractUpDate.getName(),contractUpDate.getReleaseStatus());
        ResultBean<ContractVo> resultBean = new ResultBean<>();
        Contract contract = new Contract();
        contract.setContractId(Long.valueOf(contractUpDate.getContractId()));
        QueryWrapper<Contract> contractQueryWrapper = new QueryWrapper<>(contract);
        Contract byId = iContractService.getOne(contractQueryWrapper,false);
        this.judgeNotExistContract(byId);
        boolean isOwn=StringUtils.equalsIgnoreCase(byId.getOrganizationId(),entId);
        boolean hidden = this.isHidden(contractUpDate.getContractId(), entId);
        if(hidden){
            resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
            resultBean.setMessage("隐藏的合约不允许编辑");
            log.warn("不合法的操作：对隐藏的合约进行编辑请求，合约id：{}",contractUpDate.getContractId());
            return ResponseEntity.badRequest().body(resultBean);
        }
        if(ContractConstants.RELEASE_STATUS_UNPUBLISHED.getCode().equalsIgnoreCase(byId.getReleaseStatus())){
            resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
            resultBean.setMessage("取消引用的合约不允许编辑");
            log.warn("不合法的操作：对取消引用的合约进行编辑请求，合约id：{}",contractUpDate.getContractId());
            return ResponseEntity.badRequest().body(resultBean);
        }

        boolean isEditDetails = ContractConstants.EXECUTE_STATUS_WAIT.getCode().equalsIgnoreCase(byId.getExecuteStatus()) || ContractConstants.EXECUTE_STATUS_TERMINATE.getCode().equalsIgnoreCase(byId.getExecuteStatus());
        //发起者编辑逻辑
        if(isOwn){
            //合约发起者无论合约状态都可进行编辑基本信息
            byId.setName(contractUpDate.getName());
            byId.setDecription(contractUpDate.getDecription());
            //根据状态判断是否可编辑详细信息
            if(isEditDetails){
                try {
                    updateOwnedContract(byId,contractUpDate,entId);
                } catch (StatusException e) {
                    log.info(e.getResult().toString());
                }
            }
        //参与方编辑逻辑
        }else{
            //根据状态判断是否可编辑详细信息
            if(isEditDetails){
                //参与方合约编辑逻辑
                updateCollaborationContract(byId,contractUpDate,entId);
            }else{
                resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
                resultBean.setMessage("合约当前执行状态不允许编辑");
                log.warn("不合法的操作：提交修改不允许修改的合约请求，合约id：{}，合约当前执行状态：{}",byId.getContractId(),byId.getExecuteStatus());
                return ResponseEntity.badRequest().body(resultBean);
            }
        }
        boolean b = iContractService.updateById(byId);
        if(!b){
            resultBean.setStatusEnum(StatusEnum.UPDATE_FAIL);
            resultBean.setMessage("编辑失败，请重新尝试后联系客服处理");
            log.error("编辑合约失败,合约id{}",byId.getContractId());
            return ResponseEntity.badRequest().body(resultBean);
        }
        ContractVo contractVO = this.contractConvert.dtoToVo(byId);
        //获取协作组织角色信息
        List<OrgAliasVo> orgAliasVos = new ArrayList<>();
        ContractOrgAlias contractOrgAlias = new ContractOrgAlias();
        contractOrgAlias.setContractId(byId.getContractId());
        QueryWrapper<ContractOrgAlias> orgAliasQueryWrapper = new QueryWrapper<>(contractOrgAlias);
        List<ContractOrgAlias> list = iContractOrgAliasService.list(orgAliasQueryWrapper);
        for (ContractOrgAlias orgAlias : list) {
            OrgAliasVo orgAliasVO = this.contractOrgAliasConvert.dto2vo(orgAlias);
            orgAliasVos.add(orgAliasVO);
        }
        contractVO.setOrgAlias(orgAliasVos);
        resultBean.setStatusEnum(StatusEnum.SUCCESS);
        resultBean.setData(contractVO);
        return ResponseEntity.ok(resultBean);
    }


    /* @param contractId 合约ID
     * @param entId 企业ID
     * @return 是否隐藏
     */
    private boolean isHidden(String contractId, String entId) {
        ContractOrgConfig contractOrgConfig = new ContractOrgConfig();
        contractOrgConfig.setCollaborationOrgId(entId);
        contractOrgConfig.setContractId(Long.valueOf(contractId));
        // 设置隐藏标志为true
        contractOrgConfig.setHidden(true);
        QueryWrapper<ContractOrgConfig> configQueryWrapper = new QueryWrapper<>(contractOrgConfig);
        // 仅查询合约ID字段
        configQueryWrapper.select(ContractOrgConfig.CONTRACT_ID);
        // 查询合约组织配置表中是否存在符合条件的记录
        long count = iContractOrgConfigService.count(configQueryWrapper);
        // 如果存在记录，则表示该合约属于隐藏状态，返回true,否则返回false
        if(count>0){
            return true;
        }else{
            return false;
        }

    }


    /**
     * 协作组织更新合约
     *
     * @param contract       合同
     * @param contractUpDate 合同上日期
     * @param entId          ent id
     */
    private void updateCollaborationContract(Contract contract,ContractUpDate contractUpDate,String entId){
        //别人发起的合约编辑逻辑
        //先检查自己是不是协作方
        ContractOrgAlias contractOrgAlias = new ContractOrgAlias();
        contractOrgAlias.setContractId(contract.getContractId());
        contractOrgAlias.setCollaborationOrgId(entId);
        QueryWrapper<ContractOrgAlias> contractOrgAliasQueryWrapper = new QueryWrapper<>(contractOrgAlias);
        long count = iContractOrgAliasService.count(contractOrgAliasQueryWrapper);
        if(count==0){
            throw new ApplicationException(HttpStatus.BAD_REQUEST,StatusEnum.OPERATION_NOT_ALLOWED.getCode(),"不允许编辑未参与的合约！");
        }
        ContractOrgRelease orgQuery = new ContractOrgRelease();
        orgQuery.setContractId(contract.getContractId());
        orgQuery.setCollaborationOrgId(entId);
        QueryWrapper<ContractOrgRelease> orgQueryWrapper = new QueryWrapper<>(orgQuery);
        List<ContractOrgRelease> contractOrgReleases = iContractOrgReleaseService.list(orgQueryWrapper);
        if(CollectionUtils.isEmpty(contractOrgReleases)){
            throw new ApplicationException(HttpStatus.BAD_REQUEST,StatusEnum.OPERATION_NOT_ALLOWED.getCode(),"不允许编辑未参与或参与但发起方没有启用的合约内容！");
        }
        if(ContractConstants.RELEASE_STATUS_PUBLISHED.getCode().equalsIgnoreCase(contractOrgReleases.get(0).getReleaseStatus())){
            throw new ApplicationException(HttpStatus.BAD_REQUEST,StatusEnum.OPERATION_NOT_ALLOWED.getCode(),"已经启用的合约不允许编辑！");
        }
        //看入参状态是不是发布
        if(!ContractConstants.RELEASE_STATUS_PART.getCode().equalsIgnoreCase(contract.getReleaseStatus())){
            throw new ApplicationException(HttpStatus.BAD_REQUEST,StatusEnum.OPERATION_NOT_ALLOWED.getCode(),"不允许重复启用！");
        }

        //更新合约协作组织发布情况
        ContractOrgRelease contractOrgRelease = new ContractOrgRelease();
        contractOrgRelease.setContractId(contract.getContractId());
        contractOrgRelease.setCollaborationOrgId(entId);
        contractOrgRelease.setReleaseStatus(contractUpDate.getReleaseStatus());
        contractOrgRelease.setReleaseTime(new Date());
        iContractOrgReleaseService.update(contractOrgRelease,orgQueryWrapper);

        //获取参与方可编辑内容
        if(StringUtils.isNotBlank(contractUpDate.getProcessDefine())){
            this.partakeEdit(contract,contractUpDate,entId);
        }
        if(ContractConstants.RELEASE_STATUS_PUBLISHED.getCode().equalsIgnoreCase(contractUpDate.getReleaseStatus())){
            this.saveMemberConnectorId(contract,entId);
            this.createOrUpdateOrgConnectorInProcess(contract, entId);
            this.vote2SmartContract(contract,entId);
        }

        //开始处理是否所有协作组织全部启用
        ContractOrgRelease orgAll = new ContractOrgRelease();
        orgAll.setContractId(contract.getContractId());
        QueryWrapper<ContractOrgRelease> orgAllQueryWrapper = new QueryWrapper<>(orgAll);
        List<ContractOrgRelease> list = iContractOrgReleaseService.list(orgAllQueryWrapper);
        boolean isWhole=true;
        if(!CollectionUtils.isEmpty(list)){
            for (ContractOrgRelease orgRelease : list) {
                if(!ContractConstants.RELEASE_STATUS_PUBLISHED.getCode().equalsIgnoreCase(orgRelease.getReleaseStatus())){
                    isWhole =false;
                    break;
                }
            }
        }else{
            log.error("合约id{}，没有状态表相关数据",contract.getContractId());
            throw new ApplicationException(HttpStatus.BAD_REQUEST,StatusEnum.OPERATION_NOT_ALLOWED);
        }

        if(isWhole){
            //全部启用则发布
            this.publishProcess(contract);
            contract.setReleaseStatus(ContractConstants.RELEASE_STATUS_PUBLISHED.getCode());
        }
    }

    /**
     * 发起者更新合约
     *
     * @param contract       合同
     * @param contractUpDate 合同上日期
     * @param entId          ent id
     */
    private void updateOwnedContract(Contract contract,ContractUpDate contractUpDate,String entId){
        String releaseStatus=contract.getReleaseStatus();
        String executeStatus = contract.getExecuteStatus();
        //自己发起的合约编辑逻辑
//        contract.setName(contractUpDate.getName());
        //检查协作方信息是否合法
        ContractOrgAlias contractOrgAlias = new ContractOrgAlias();
        contractOrgAlias.setContractId(contract.getContractId());
        QueryWrapper<ContractOrgAlias> orgAliasQueryWrapper = new QueryWrapper<>(contractOrgAlias);
        List<ContractOrgAlias> contractOrgAliasListInDb = iContractOrgAliasService.list(orgAliasQueryWrapper);
        List<OrgAliasVo> orgAliasUpdates = contractUpDate.getOrgAlias();

        List<ContractOrgAlias> updateDo = new ArrayList<>();
        List<ContractOrgAlias> saveDo = new ArrayList<>();
        //验证和处理协作角色与协作组织关系
        verifyAndDealOrgAlias(contract, entId,  contractOrgAliasListInDb, orgAliasUpdates, updateDo, saveDo);

        if(contractUpDate.getReleaseStatus().equalsIgnoreCase(ContractConstants.RELEASE_STATUS_UNPUBLISHED.getCode()) && ContractConstants.RELEASE_STATUS_PUBLISHED.getCode().equalsIgnoreCase(releaseStatus)){
            this.unReleaseContract(contract);
        }else if(ContractConstants.RELEASE_STATUS_PUBLISHED.getCode().equalsIgnoreCase(contractUpDate.getReleaseStatus()) && (ContractConstants.EXECUTE_STATUS_WAIT.getCode().equalsIgnoreCase(executeStatus) || ContractConstants.EXECUTE_STATUS_TERMINATE.getCode().equalsIgnoreCase(executeStatus) )){
            this.updateOwnedContractRelease(contract,contractUpDate,entId,orgAliasUpdates);
        }else if(ContractConstants.RELEASE_STATUS_SAVE.getCode().equalsIgnoreCase(contractUpDate.getReleaseStatus()) && ContractConstants.RELEASE_STATUS_SAVE.getCode().equalsIgnoreCase(releaseStatus)){
            this.updateOwnedContractSaved(contract,contractUpDate,entId);
        }
        //更新协作组织信息
        iContractOrgAliasService.removeBatchByIds(contractOrgAliasListInDb);
        iContractOrgAliasService.updateBatchById(updateDo);
        iContractOrgAliasService.saveBatch(saveDo);
    }

    /**
     * 验证并处理协作角色关系
     *
     * @param contract                 合同
     * @param entId                    ent id
     * @param contractOrgAliasListInDb 合同组织数据库别名列表中
     * @param orgAliasUpdates          org别名更新
     * @param updateDo                 更新完成
     * @param saveDo                   保存所做
     */
    private void verifyAndDealOrgAlias(Contract contract, String entId, List<ContractOrgAlias> contractOrgAliasListInDb, List<OrgAliasVo> orgAliasUpdates, List<ContractOrgAlias> updateDo, List<ContractOrgAlias> saveDo) {
        boolean isHaveCreator = false;

        //去除协作角色与协作组织关系的重复项，并同时拿出新增协作角色
        List<OrgAliasVo> requestOrgAliasSaveList=new ArrayList<>(orgAliasUpdates.size());
        for (OrgAliasVo orgAliasUpdate : orgAliasUpdates) {
            if(orgAliasUpdate.getCollaborationOrgId().equalsIgnoreCase(entId)){
                isHaveCreator =true;
            }
            //所有没id的，视为新增
            if(orgAliasUpdate.getAliasId()==null){
                ContractOrgAlias orgAlias = new ContractOrgAlias();
                orgAlias.setAliasSeq(IdWorker.getId());
                orgAlias.setAliasId(IdWorker.getId());
                orgAlias.setContractId(contract.getContractId());
                orgAlias.setAliasName(orgAliasUpdate.getAliasName());
                orgAlias.setCollaborationOrgId(orgAliasUpdate.getCollaborationOrgId());
                saveDo.add(orgAlias);
                continue;
            }
            //1、判断存在相同的请求协作角色，则丢弃
            if(!requestOrgAliasSaveList.contains(orgAliasUpdate)){
                dealOrgAliasHadAliasId(contract, contractOrgAliasListInDb, orgAliasUpdates, updateDo, saveDo, requestOrgAliasSaveList, orgAliasUpdate);
            }
        }

        //协作组织中必须有创建者，否则提示：“协作组织中必须包含创建者”
        if(!isHaveCreator){
            throw new ApplicationException(HttpStatus.BAD_REQUEST, StatusEnum.OPERATION_NOT_ALLOWED.getCode(),"协作组织中必须包含创建者");
        }
    }


    /**
     * 处理含有别名ID的关系
     * 1、存在同角色id、同组织id但角色名称不同，则数据有误。2、相同组织不同角色id的报错。
     *
     * @param contract                 合同
     * @param contractOrgAliasListInDb 合同组织数据库别名列表中
     * @param orgAliasUpdates          org别名更新
     * @param updateDo                 更新完成
     * @param saveDo                   保存所做
     * @param requestOrgAliasSaveList  请求组织别名保存列表
     * @param orgAliasUpdate           org别名更新
     */
    private void dealOrgAliasHadAliasId(Contract contract, List<ContractOrgAlias> contractOrgAliasListInDb, List<OrgAliasVo> orgAliasUpdates, List<ContractOrgAlias> updateDo, List<ContractOrgAlias> saveDo, List<OrgAliasVo> requestOrgAliasSaveList, OrgAliasVo orgAliasUpdate) {
        verifyDuplicateOrgAlias(orgAliasUpdates, requestOrgAliasSaveList, orgAliasUpdate);
        requestOrgAliasSaveList.add(orgAliasUpdate);
        //过滤是否在数据库中存在
        boolean notInDb=true;
        for (ContractOrgAlias orgAlias : contractOrgAliasListInDb) {
            if(Objects.equals(orgAliasUpdate.getAliasId(),orgAlias.getAliasId())){
                orgAlias.setContractId(contract.getContractId());
                orgAlias.setAliasName(orgAliasUpdate.getAliasName());
                orgAlias.setCollaborationOrgId(orgAliasUpdate.getCollaborationOrgId());
                updateDo.add(orgAlias);
                notInDb=false;
                //如果存在于数据库中，又存在于请求中，放入更新列表后，无需再次处理，最后列表中保留的仅是请求中不存在的
                contractOrgAliasListInDb.remove(orgAlias);
                break;
            }
        }

        if(notInDb){
            //如果角色id不存在与合约角色关联表中，则去查询模板表是否存在,不存在丢弃
            ContractTemplateOrgAlias contractTemplateOrgAlias = new ContractTemplateOrgAlias();
            contractTemplateOrgAlias.setAliasId(orgAliasUpdate.getAliasId());
            contractTemplateOrgAlias.setContractTemplateId(contract.getContractTemplateId());
            QueryWrapper<ContractTemplateOrgAlias> templateOrgAliasQueryWrapper = new QueryWrapper<>(contractTemplateOrgAlias);
            long count = iContractTemplateOrgAliasService.count(templateOrgAliasQueryWrapper);
            if(count>0){
                ContractOrgAlias orgAlias = new ContractOrgAlias();
                orgAlias.setAliasSeq(IdWorker.getId());
                orgAlias.setAliasId(orgAliasUpdate.getAliasId());
                orgAlias.setContractId(contract.getContractId());
                orgAlias.setAliasName(orgAliasUpdate.getAliasName());
                orgAlias.setCollaborationOrgId(orgAliasUpdate.getCollaborationOrgId());
                saveDo.add(orgAlias);
            }
        }
    }

    /**
     * 验证重复的协作角色
     *
     * @param orgAliasUpdates         org别名更新
     * @param requestOrgAliasSaveList 请求组织别名保存列表
     * @param orgAliasUpdate          org别名更新
     */
    private static void verifyDuplicateOrgAlias(List<OrgAliasVo> orgAliasUpdates, List<OrgAliasVo> requestOrgAliasSaveList, OrgAliasVo orgAliasUpdate) {
        for (OrgAliasVo orgAliasSave : requestOrgAliasSaveList) {
            if(StringUtils.equals(orgAliasUpdate.getCollaborationOrgId(),orgAliasSave.getCollaborationOrgId()) && Objects.equals(orgAliasUpdate.getAliasId(),orgAliasSave.getAliasId())){
                log.error("角色数据有误，不同角色名称，关联了相同的角色id和组织id，请求信息：{}", orgAliasUpdates);
                throw new ApplicationException(HttpStatus.BAD_REQUEST, StatusEnum.OPERATION_NOT_ALLOWED.getCode(),"角色数据有误，请联系客服。");
            }
            if(StringUtils.equals(orgAliasUpdate.getCollaborationOrgId(),orgAliasSave.getCollaborationOrgId()) && !Objects.equals(orgAliasUpdate.getAliasId(),orgAliasSave.getAliasId())){
                throw new ApplicationException(HttpStatus.BAD_REQUEST, StatusEnum.OPERATION_NOT_ALLOWED.getCode(),"一个组织不能同时关联多个角色。");
            }
        }
    }

    /**
     * 更新操作，发布发起的合约
     *
     * @param contract        合同
     * @param contractUpDate  合同上日期
     * @param entId           ent id
     * @param orgAliasUpdates org别名更新
     */
    private void updateOwnedContractRelease(Contract contract,ContractUpDate contractUpDate,String entId,List<OrgAliasVo> orgAliasUpdates){
        isCanModifyOwnedContract(contract, entId);
        validateProcess(contractUpDate.getProcessDefine(),null);
        //启用逻辑
        String originatorEdit = this.forbidden4OriginatorEdit(contractUpDate.getProcessDefine(),entId);
        if(StringUtils.isBlank(originatorEdit)){
            contract.setProcessDefintion(contractUpDate.getProcessDefine());
        }else{
            contract.setProcessDefintion(originatorEdit);
        }
        this.saveMemberConnectorId(contract,entId);
        this.createOrUpdateOrgConnectorInProcess(contract, entId);
        if(orgAliasUpdates.size()==1 && orgAliasUpdates.get(0).getCollaborationOrgId().equalsIgnoreCase(entId)){
            contract.setReleaseStatus(ContractConstants.RELEASE_STATUS_PUBLISHED.getCode());
            this.publishProcess(contract);
            //写入合约协作组织状态表
            this.insertOrgRelease(orgAliasUpdates, contract.getContractId(), entId);
        }else{
            contract.setReleaseStatus(ContractConstants.RELEASE_STATUS_PART.getCode());
            //写入合约协作组织状态表
            this.insertOrgRelease(orgAliasUpdates, contract.getContractId(), entId);
        }
        this.vote2SmartContract(contract,entId);
    }

    private void insertOrgRelease(List<OrgAliasVo> orgAliasUpdates,Long contractId,String entId){
        ContractOrgRelease contractOrgReleaseDel = new ContractOrgRelease();
        contractOrgReleaseDel.setContractId(contractId);
        QueryWrapper<ContractOrgRelease> contractOrgReleaseQueryWrapper = new QueryWrapper<>(contractOrgReleaseDel);
        boolean remove = iContractOrgReleaseService.remove(contractOrgReleaseQueryWrapper);
        if(!remove){
            log.warn("删除组织状态信息失败，合约id：{}", contractId);
        }
        for (OrgAliasVo orgAlias : orgAliasUpdates) {
            ContractOrgRelease contractOrgRelease = new ContractOrgRelease();
            contractOrgRelease.setContractId(contractId);
            contractOrgRelease.setCollaborationOrgId(orgAlias.getCollaborationOrgId());
            if(entId.equalsIgnoreCase(orgAlias.getCollaborationOrgId())){
                contractOrgRelease.setReleaseStatus(ContractConstants.RELEASE_STATUS_PUBLISHED.getCode());
            }
            contractOrgRelease.setReleaseTime(new Date());
            iContractOrgReleaseService.save(contractOrgRelease);
        }
    }

    /**
     * 更新操作，暂存发起的合约
     *
     * @param contract       合同
     * @param contractUpDate 合同上日期
     * @param entId          ent id
     */
    private void updateOwnedContractSaved(Contract contract,ContractUpDate contractUpDate,String entId){
        isCanModifyOwnedContract(contract, entId);
        if(StringUtils.isNotBlank(contractUpDate.getProcessDefine())){
            String originatorEdit = this.forbidden4OriginatorEdit(contractUpDate.getProcessDefine(), entId);
            if(StringUtils.isBlank(originatorEdit)){
                contract.setProcessDefintion(contractUpDate.getProcessDefine());
            }else{
                contract.setProcessDefintion(originatorEdit);
            }
        }
    }

    private void isCanModifyOwnedContract(Contract contract, String entId) {
        if(ContractConstants.RELEASE_STATUS_PUBLISHED.getCode().equalsIgnoreCase(contract.getReleaseStatus())){
            throw new StatusException(HttpStatus.BAD_REQUEST,StatusEnum.OPERATION_NOT_ALLOWED.getCode(),"全部启用的合约不允许编辑");
        }
        ContractOrgRelease contractOrgReleaseState = new ContractOrgRelease();
        contractOrgReleaseState.setContractId(contract.getContractId());
        contractOrgReleaseState.setCollaborationOrgId(entId);
        contractOrgReleaseState.setReleaseStatus(ContractConstants.RELEASE_STATUS_PUBLISHED.getCode());
        QueryWrapper<ContractOrgRelease> stateQueryWrapper = new QueryWrapper<>(contractOrgReleaseState);
        long count = iContractOrgReleaseService.count(stateQueryWrapper);
        if(count>0){
            throw new StatusException(HttpStatus.BAD_REQUEST,StatusEnum.OPERATION_NOT_ALLOWED.getCode(),"启用的合约不允许编辑");
        }
    }

    /**
     * 暂停已发布的合约
     *
     * @param contract 合同
     */
    private void unReleaseContract(Contract contract){
        if(StringUtils.isBlank(contract.getProcessDefinitionId())){
            throw new ApplicationException(HttpStatus.BAD_REQUEST,StatusEnum.OPERATION_NOT_ALLOWED.getCode(),"没有流程定义id无法取消引用");
        }
        Process process=this.getProcessFromContract(contract);
        //取消引用
        RepositoryService repositoryService = this.processEngine.getRepositoryService();
        repositoryService.suspendProcessDefinitionById(contract.getProcessDefinitionId());
        //删除连接器
        List<TaskWrapper> allConnectorInst=ProcessUtil.getAllTaskWrapperWithMainActivityEnd(process);
        if(!CollectionUtils.isEmpty(allConnectorInst)){
            for (TaskWrapper id : allConnectorInst) {
                log.info("调用删除连接器接口，入参MemberConnectorTaskId：{}，memberid：{}",id.getMemberConnectorTaskIdString(),id.getMemberIdString());
                this.connectorManageClient.deleteOrgConnectorTask(id.getMemberConnectorTaskIdString(),id.getMemberIdString());
            }
        }
        //执行状态更新为暂停
        contract.setExecuteStatus(ContractConstants.EXECUTE_STATUS_TERMINATE.getCode());
    }


    @GetMapping("/{contractid}")
    public ResponseEntity<ResultBean<ContractVo>> getContract(@RequestHeader(name = "memberId")@NotBlank String entId,
                                                              @PathVariable(name = "contractid") @NotBlank Long contractId) {
        ResultBean<ContractVo> resultBean = new ResultBean<>();

        ContractOrgRelease contractOrgRelease = new ContractOrgRelease();
        contractOrgRelease.setCollaborationOrgId(entId);
        contractOrgRelease.setContractId(contractId);
        QueryWrapper<ContractOrgRelease> releaseQueryWrapper = new QueryWrapper<>(contractOrgRelease);
        long count = iContractOrgReleaseService.count(releaseQueryWrapper);
        Contract contract = new Contract();
        if (count<=0){
            contract.setOrganizationId(entId);
        }
        contract.setContractId(contractId);
        QueryWrapper<Contract> contractQueryWrapper = new QueryWrapper<>(contract);
        Contract byId = iContractService.getOne(contractQueryWrapper, false);
        if(null==byId){
            resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
            resultBean.setMessage("没有相关数据");
            return ResponseEntity.badRequest().body(resultBean);
        }
        ContractVo contractVO = this.contractConvert.dtoToVo(byId);
        if(StringUtils.isNotBlank(byId.getProcessDefintion())){
            //处理不是自己参与的活动的参数
            ContractProcessEntity contractProcessEntity = this.parseContractDefine(byId.getProcessDefintion().getBytes(StandardCharsets.UTF_8));
            BpmnModelInstance bpmnModelInstance=contractProcessEntity.getBpmnModelInstance();
            List<TaskWrapper> tasks = contractProcessEntity.getTasks();
            for (TaskWrapper task : tasks) {
                this.deleteNotMyTaskConnectorPara(entId, task);
            }
            String processDefinitionXml = Bpmn.convertToString(bpmnModelInstance);
            contractVO.setProcessDefine(processDefinitionXml);
        }
        //获取协作组织角色信息
        List<OrgAliasVo> orgAliasVos = new ArrayList<>();
        ContractOrgAlias contractOrgAlias = new ContractOrgAlias();
        contractOrgAlias.setContractId(byId.getContractId());
        QueryWrapper<ContractOrgAlias> orgAliasQueryWrapper = new QueryWrapper<>(contractOrgAlias);
        List<ContractOrgAlias> list = iContractOrgAliasService.list(orgAliasQueryWrapper);
        for (ContractOrgAlias orgAlias : list) {
            OrgAliasVo orgAliasVO = this.contractOrgAliasConvert.dto2vo(orgAlias);
            orgAliasVos.add(orgAliasVO);
        }
        contractVO.setOrgAlias(orgAliasVos);
        resultBean.setData(contractVO);
        resultBean.setStatusEnum(StatusEnum.SUCCESS);
        return ResponseEntity.ok(resultBean);
    }

    /**
     * 删除不是我任务的连接器参数
     *
     * @param entId             ent id
     * @param taskWrapper task的扩展类
     */
    private void deleteNotMyTaskConnectorPara(String entId, TaskWrapper taskWrapper) {
        if(!StringUtils.equals(entId,taskWrapper.getMemberIdString())){
            taskWrapper.setConnectorTaskParaString(null);
        }
    }

    @GetMapping("/participate")
    public ResponseEntity<ResultBean<List<String>>> getOrgId(@RequestHeader(name = "memberId")@NotBlank String entId) {
        return this.getOriginator(entId);
    }

    @GetMapping("/mycontract/originator")
    public ResponseEntity<ResultBean<List<String>>> getOriginator(@RequestHeader(name = "memberId")@NotBlank String entId) {
        ResultBean<List<String>> resultBean = new ResultBean<>();
        ContractOrgAlias contractOrgAlias  = new ContractOrgAlias();
        contractOrgAlias.setCollaborationOrgId(entId);
        QueryWrapper<ContractOrgAlias> queryWrapper = new QueryWrapper<>(contractOrgAlias);
        List<ContractOrgAlias> list = iContractOrgAliasService.list(queryWrapper);
        QueryWrapper<Contract> contractQueryWrapper = new QueryWrapper<>();
        contractQueryWrapper.func(new OrgContractQueryConsumer(entId,list));
        List<Contract> contracts = iContractService.list(contractQueryWrapper);
        List<String> orgIds= new ArrayList<>();
        for (Contract contract : contracts) {
            orgIds.add(contract.getOrganizationId());
        }
        orgIds = orgIds.stream().distinct().collect(Collectors.toList());
        resultBean.setData(orgIds);
        resultBean.setStatusEnum(orgIds.isEmpty()?SUCCESS_NO_DATA:StatusEnum.SUCCESS);
        return ResponseEntity.ok(resultBean);
    }


    @GetMapping()
    public ResponseEntity<ResultBean<IPage<ContractListVO>>> indexList(@RequestHeader(name = "memberId")@NotBlank String entId,
                                                                       @RequestParam(name = "contractname" , required = false) String contractName,
                                                                       @RequestParam(name = "orgid", required = false) String orgId,
                                                                       @RequestParam(name = "releasestatus", required = false) String releaseStatus,
                                                                       @RequestParam(name = "executestatus", required = false) String executeStatus,
                                                                       @RequestParam(name = "categorycode", required = false) List<String> categoryCode,
                                                                       @RequestParam(name = "hidden", required = false) Boolean hidden,
                                                                       @RequestParam(name = "currentpage") int currentPage,
                                                                       @RequestParam(name = "pagesize") int pageSize) {
        ResultBean<IPage<ContractListVO>> resultBean = new ResultBean<>();
        IPage<ContractListVO> contractListVOIPage = new Page<>();
        List<ContractListVO> contractListVOS = new ArrayList<>();
        Contract contract = new Contract();
        contract.setOrganizationId(orgId);
        contract.setReleaseStatus(releaseStatus);
        contract.setExecuteStatus(executeStatus);
        QueryWrapper<Contract> queryWrapper = new QueryWrapper<>(contract);
        if(hidden != null ){
            //查询企业是否存在隐藏数据
            ContractOrgConfig contractOrgConfig = new ContractOrgConfig();
            contractOrgConfig.setCollaborationOrgId(entId);
            contractOrgConfig.setHidden(true);
            QueryWrapper<ContractOrgConfig> configQueryWrapper = new QueryWrapper<>(contractOrgConfig);
            configQueryWrapper.select(ContractOrgConfig.CONTRACT_ID);
            List<ContractOrgConfig> contractOrgConfigs = this.iContractOrgConfigService.list(configQueryWrapper);
            if(hidden){
                if(CollUtil.isEmpty(contractOrgConfigs)){
                    //当前企业查询条件为隐藏 但没有隐藏的合约数据的情况，直接返回。
                    resultBean.setStatusEnum(StatusEnum.SUCCESS_NO_DATA);
                    return ResponseEntity.ok().body(resultBean);
                }else{
                    // 如果存在隐藏数据，则使用JoinedContractQueryByHiddenConsumer来创建查询条件
                    queryWrapper.and(new JoinedContractQueryByHiddenConsumer(contractOrgConfigs));
                }
            }
            if(!hidden && CollUtil.isNotEmpty(contractOrgConfigs)){
                //使用notin
                queryWrapper.and(new JoinedContractQueryByNotHiddenConsumer(contractOrgConfigs));
            }

        }
        if(StringUtils.isNotBlank(contractName)){
            queryWrapper=queryWrapper.like(Contract.NAME,contractName);
        }
        //如果传入orgid，且orgid和企业id相等，则不需要再过滤作为协作组织的合约，只过滤发起者合约即可。
        //如果传入orgid，和企业id不同，则仅查询协作组织包含企业id的合约后，过滤合约发起者是orgid。
        //如果未传入orgid，则查询发起者是当前企业或者当前企业作为协作组织的合约。
        if(!StringUtils.equals(entId,orgId)){
            ContractOrgAlias contractOrgAlias = new ContractOrgAlias();
            contractOrgAlias.setCollaborationOrgId(entId);
            QueryWrapper<ContractOrgAlias> queryWrapper1 = new QueryWrapper<>(contractOrgAlias);
            List<ContractOrgAlias> list = iContractOrgAliasService.list(queryWrapper1);
            if(StringUtils.isNotBlank(orgId)) {
                if(CollectionUtils.isEmpty(list)){
                    //对于查询合约发起单位不是操作企业，操作企业又没有参与的合约，则结果直接为0
                    resultBean.setStatusEnum(StatusEnum.SUCCESS_NO_DATA);
                    return ResponseEntity.ok().body(resultBean);
                }
                queryWrapper.and(new JoinedContractQueryByContractIdConsumer(list));
            }else {
                queryWrapper.and(new OrgContractQueryConsumer(entId,list));
            }
        }
        //根据分类查询
        if(!CollectionUtils.isEmpty(categoryCode)){
            List<ContractTemplateKindRel> contractTemplateKindRels = iContractTemplateKindRelService.queryContractTemplateKindRelByCategoryCode(categoryCode);
            queryWrapper.and(new JoinedContractQueryByContractTplIdConsumer(contractTemplateKindRels));
        }

        if(!CollectionUtils.isEmpty(categoryCode)){
            QueryWrapper<ContractTemplateKind> templateKindQueryWrapper = new QueryWrapper<>();
            templateKindQueryWrapper.in(ContractTemplateKind.CATEGORY_CODE,categoryCode);
            List<ContractTemplateKind> contractTemplateKinds = iContractTemplateKindService.list(templateKindQueryWrapper);
            if(CollectionUtils.isEmpty(contractTemplateKinds)){
                resultBean.setStatusEnum(StatusEnum.SUCCESS_NO_DATA);
                return ResponseEntity.ok().body(resultBean);
            }
            List<String> ids = new ArrayList<>();
            for (ContractTemplateKind contractTemplateKind : contractTemplateKinds) {
                ids.add(String.valueOf(contractTemplateKind.getKindId()));
            }

            ids=ids.stream().distinct().collect(Collectors.toList());
            QueryWrapper<ContractTemplateKindRel> kindRelQueryWrapper = new QueryWrapper<>();
            kindRelQueryWrapper.in(ContractTemplateKindRel.KIND_ID,ids);
            List<ContractTemplateKindRel> list = iContractTemplateKindRelService.list(kindRelQueryWrapper);
            if(CollectionUtils.isEmpty(list)){
                resultBean.setStatusEnum(StatusEnum.SUCCESS_NO_DATA);
                return ResponseEntity.ok().body(resultBean);
            }
            queryWrapper.and(new JoinedContractQueryByContractTplIdConsumer(list));
        }
        queryWrapper.orderByDesc(Contract.CREATE_DATE);
        queryWrapper.select(Contract.class,info -> !info.getProperty().equals("processDefintion"));

        Page<Contract> page = new Page<>(currentPage, pageSize);
        Page<Contract> indexPage = this.iContractService.page(page, queryWrapper);
        List<Contract> contractRecords = indexPage.getRecords();
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        List<CompletableFuture<ContractListVO>> results=new ArrayList<>(contractRecords.size());
        for (Contract contractRecord : contractRecords) {
            results.add(CompletableFuture.supplyAsync(new ContractListVoSupplier(contractRecord,this.connectorManageClient,this.iContractService,this.iContractOrgAliasService,this.processEngine.getHistoryService(),this.contractConvert,this.contractOrgAliasConvert,authentication)));
        }

        for (CompletableFuture<ContractListVO> contractListVOCompletableFuture : results) {
            try {
                ContractListVO contractListVO = contractListVOCompletableFuture.get();

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
                contractListVOS.add(contractListVO);
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            } catch(Exception e) {
                log.error("查询合约列表的异步查询出错。",e);
            }
        }
        contractListVOIPage.setRecords(contractListVOS);
        contractListVOIPage.setSize(contractRecords.size());
        contractListVOIPage.setCurrent(indexPage.getCurrent());
        contractListVOIPage.setPages(indexPage.getPages());
        contractListVOIPage.setTotal(indexPage.getTotal());
        resultBean.setData(contractListVOIPage);
        if(indexPage.getTotal()<1){
            resultBean.setStatusEnum(StatusEnum.SUCCESS_NO_DATA);
        }else {
            resultBean.setStatusEnum(StatusEnum.SUCCESS);
        }
        return ResponseEntity.ok(resultBean);
    }


    @PutMapping("/{contractid}/start")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<ResultBean<String>> startContract(@RequestHeader(name = "memberId")@NotBlank String entId,
                                                            @PathVariable(name = "contractid") @NotBlank String contractId) {
        ResultBean<String> resultBean = new ResultBean<>();
        Contract contract = new Contract();
        contract.setOrganizationId(entId);
        contract.setContractId(Long.valueOf(contractId));
        QueryWrapper<Contract> queryWrapper = new QueryWrapper<>(contract);
        Contract byId = iContractService.getOne(queryWrapper,false);

        this.judgeNotExistContract(byId);
        String releaseStatus = byId.getReleaseStatus();
        boolean hidden = this.isHidden(contractId, entId);
        if(hidden){
            resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
            resultBean.setMessage("隐藏的合约不允许编辑");
            return ResponseEntity.badRequest().body(resultBean);
        }
        if(ContractConstants.RELEASE_STATUS_UNPUBLISHED.getCode().equalsIgnoreCase(releaseStatus)){
            resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
            resultBean.setMessage("取消引用的合约不允许启动");
            return ResponseEntity.badRequest().body(resultBean);
        }
        if(ContractConstants.RELEASE_STATUS_SAVE.getCode().equalsIgnoreCase(byId.getReleaseStatus()) || ContractConstants.RELEASE_STATUS_PART.getCode().equalsIgnoreCase(byId.getReleaseStatus())){
            resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
            resultBean.setMessage("请先启用合约后执行操作");
            return ResponseEntity.badRequest().body(resultBean);
        }
        if(!ContractConstants.EXECUTE_STATUS_WAIT.getCode().equalsIgnoreCase(byId.getExecuteStatus())
                && !ContractConstants.EXECUTE_STATUS_TERMINATE.getCode().equalsIgnoreCase(byId.getExecuteStatus())
                && !ContractConstants.EXECUTE_STATUS_EXCEPTION.getCode().equalsIgnoreCase(byId.getExecuteStatus())){
            resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
            resultBean.setMessage("该状态不允许启动");
            return ResponseEntity.badRequest().body(resultBean);
        }
        if(StringUtils.isBlank(byId.getProcessDefinitionId())) {
            resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
            resultBean.setMessage("缺少流程定义信息，无法操作");
            return ResponseEntity.badRequest().body(resultBean);
        }
        Process process=this.getProcessFromContract(byId);
        List<TaskWrapper> allConnectorTask=ProcessUtil.getAllTaskWrapperWithMainActivityEnd(process);
        List<TaskWrapper> startedConnectorTaskList=new ArrayList<>(allConnectorTask.size());
        boolean successToStart=true;
        String message=null;
        TaskWrapper currentTask=null;
        try{
            for (TaskWrapper taskWrapper : allConnectorTask) {
                currentTask=taskWrapper;
                log.info("调用启动连接器任务接口，入参MemberConnectorTaskId：{}，企业id：{}",taskWrapper.getMemberConnectorTaskIdString(),byId.getOrganizationId());
                ConnectorTaskResult connectorTaskResult =this.connectorManageClient.startOrgConnectorTask(taskWrapper.getMemberConnectorTaskIdString(),byId.getOrganizationId());
                if(connectorTaskResult.getCode()<0){
                    log.warn("未正常启动连接器任务，连接器任务信息：{}",connectorTaskResult);
                    message= connectorTaskResult.getMessage();
                    stopAllConnectorInstanceWithoutStop(startedConnectorTaskList,entId);
                    successToStart=false;
                    break;
                }
                startedConnectorTaskList.add(taskWrapper);
            }
        }catch (Exception e){
            stopAllConnectorInstanceWithoutStop(startedConnectorTaskList,entId);
            log.error("启动连接器时出现异常，出错任务信息：{}",currentTask,e);
            message="启动合约出现异常。";
            successToStart=false;
        }
        ResponseEntity<ResultBean<String>> result;
        if(successToStart){
            byId.setExecuteStatus(ContractConstants.EXECUTE_STATUS_DOING.getCode());
            resultBean.setStatusEnum(StatusEnum.SUCCESS);
            result=ResponseEntity.ok(resultBean);
        }else {
            byId.setExecuteStatus(ContractConstants.EXECUTE_STATUS_EXCEPTION.getCode());
            resultBean.setMessage(message);
            result= ResponseEntity.unprocessableEntity().body(resultBean);
        }

        boolean b = iContractService.updateById(byId);
        if(!b){
            stopAllConnectorInstanceWithoutStop(startedConnectorTaskList,entId);
            resultBean.setMessage("启动失败，请重试后联系客服处理");
            log.warn("更新合约状态失败，合约id：{}", byId.getContractId());
            result= ResponseEntity.unprocessableEntity().body(resultBean);
        }
        return result;
    }

    @PutMapping("/{contractid}/shutdown")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<ResultBean<String>> shutdownContract(@RequestHeader(name = "memberId")@NotBlank String entId,
                                                               @PathVariable(name = "contractid") @NotBlank String contractId) {
        ResultBean<String> resultBean = new ResultBean<>();
        Contract contract = new Contract();
        contract.setOrganizationId(entId);
        contract.setContractId(Long.valueOf(contractId));
        QueryWrapper<Contract> queryWrapper = new QueryWrapper<>(contract);
        Contract byId = iContractService.getOne(queryWrapper,false);
        this.judgeNotExistContract(byId);
        String releaseStatus = byId.getReleaseStatus();
        boolean hidden = this.isHidden(contractId, entId);
        if(hidden){
            resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
            resultBean.setMessage("隐藏的合约不允许编辑");
            return ResponseEntity.badRequest().body(resultBean);
        }
        if(ContractConstants.RELEASE_STATUS_UNPUBLISHED.getCode().equalsIgnoreCase(releaseStatus)){
            resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
            resultBean.setMessage("该状态不允许暂停");
            return ResponseEntity.badRequest().body(resultBean);
        }
        if(!ContractConstants.EXECUTE_STATUS_DOING.getCode().equalsIgnoreCase(byId.getExecuteStatus()) && !ContractConstants.EXECUTE_STATUS_DOING_WARING.getCode().equalsIgnoreCase(byId.getExecuteStatus())){
            resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
            resultBean.setMessage("该状态不允许暂停");
            return ResponseEntity.badRequest().body(resultBean);
        }
        if(StringUtils.isBlank(byId.getProcessDefinitionId())) {
            resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
            resultBean.setMessage("缺少流程定义信息，无法操作");
            return ResponseEntity.badRequest().body(resultBean);
        }
        Process process=this.getProcessFromContract(byId);
        List<TaskWrapper> allConnectorTaskList=ProcessUtil.getAllTaskWrapperWithMainActivityFront(process);
        List<TaskWrapper> stopedTaskList=new ArrayList<>(allConnectorTaskList.size());
        boolean stopAll=true;
        TaskWrapper currentTask=null;
        try {
            for (TaskWrapper taskWrapper : allConnectorTaskList) {
                currentTask=taskWrapper;
                log.info("调用停止连接器任务接口，入参MemberConnectorTaskId：{}，企业id：{}",taskWrapper.getMemberConnectorTaskIdString(),entId);
                ConnectorTaskResult connectorTaskResult =this.connectorManageClient.stopOrgConnectorTask(taskWrapper.getMemberConnectorTaskIdString(),entId);
                if(connectorTaskResult.getCode()<0){
                    log.warn("停止连接器出错，停止响应结果：{}", connectorTaskResult);
                    stopAll=false;
                    break;
                }
                stopedTaskList.add(taskWrapper);
            }
        }catch (Exception e){
            log.error("停止连接器出现异常，出错任务信息：{}",currentTask,e);
            stopAll=false;
        }

        if(!stopAll){
            startAllConnectorInstanceWithoutStop(stopedTaskList,entId);
            resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
            resultBean.setMessage("暂停失败，有连接器停止时出错，请联系管理员处理");
            return ResponseEntity.unprocessableEntity().body(resultBean);
        }

        byId.setExecuteStatus(ContractConstants.EXECUTE_STATUS_TERMINATE.getCode());
        boolean b = iContractService.updateById(byId);
        if(!b){
            startAllConnectorInstanceWithoutStop(stopedTaskList,entId);
            resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
            resultBean.setMessage("暂停失败，请联系管理员处理");
            return ResponseEntity.unprocessableEntity().body(resultBean);
        }
        resultBean.setStatusEnum(StatusEnum.SUCCESS);
        return ResponseEntity.ok(resultBean);
    }


    @DeleteMapping("/{contractid}")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<ResultBean<String>> delContract(@RequestHeader(name = "memberId")@NotBlank String entId,
                                                          @PathVariable(name = "contractid") @NotBlank String contractId) {
        ResultBean<String> resultBean = new ResultBean<>();
        Contract contract = new Contract();
        contract.setOrganizationId(entId);
        contract.setContractId(Long.valueOf(contractId));
        QueryWrapper<Contract> queryWrapper = new QueryWrapper<>(contract);
        Contract byId = iContractService.getOne(queryWrapper,false);
        this.judgeNotExistContract(byId);
        boolean hidden = this.isHidden(contractId, entId);
        if(hidden){
            resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
            resultBean.setMessage("隐藏的合约不允许编辑");
            return ResponseEntity.badRequest().body(resultBean);
        }
        if(!ContractConstants.RELEASE_STATUS_SAVE.getCode().equals(byId.getReleaseStatus())){
            resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
            resultBean.setMessage("该状态不允许删除");
            return ResponseEntity.badRequest().body(resultBean);
        }
        boolean remove = iContractService.remove(queryWrapper);
        if(!remove){
            resultBean.setMessage("删除失败，请重试后联系客服处理");
            log.warn("删除合约失败：{}", contract);
            return ResponseEntity.badRequest().body(resultBean);
        }
        //清理合约角色表相关数据
        ContractOrgAlias contractOrgAlias = new ContractOrgAlias();
        contractOrgAlias.setContractId(Long.valueOf(contractId));
        QueryWrapper<ContractOrgAlias> orgAliasQueryWrapper = new QueryWrapper<>(contractOrgAlias);
        iContractOrgAliasService.remove(orgAliasQueryWrapper);
        resultBean.setStatusEnum(StatusEnum.SUCCESS);
        return ResponseEntity.ok(resultBean);
    }


    /**
     * 启动所有连接器实例，遇到异常也不终止
     *
     * @param connInstIds 康涅狄格州本月id
     * @param memberId    成员身份
     */
    private void startAllConnectorInstanceWithoutStop(List<TaskWrapper> connInstIds,String memberId){
        for (TaskWrapper connInstId : connInstIds) {
            try{
                this.connectorManageClient.startOrgConnectorTask(connInstId.getMemberConnectorTaskIdString(),memberId);
            }catch (Exception e){
                log.warn("启动连接器任务出错，连接器任务ID：{}",connInstId);
            }
        }
    }

    /**
     * 停止所有连接器，遇到异常不停止
     *
     * @param connInstIds 康涅狄格州本月id
     * @param memberId    成员身份
     */
    private void stopAllConnectorInstanceWithoutStop(List<TaskWrapper> connInstIds,String memberId){
        for (TaskWrapper connInstId : connInstIds) {
            try{
                this.connectorManageClient.stopOrgConnectorTask(connInstId.getMemberConnectorTaskIdString(),memberId);
            }catch (Exception e){
                log.warn("停止连接器任务出错，连接器任务ID：{}",connInstId);
            }

        }
    }


    /**
     * 创建或更新合约中的组织连接器
     *
     * @param contract 合同
     * @param entId    ent id
     */
    private void createOrUpdateOrgConnectorInProcess(Contract contract, String entId)  {
        ResultBean<String> resultBean = new ResultBean<>();
        if(StringUtils.isBlank(contract.getProcessDefintion())){
            return ;
        }
        ContractProcessEntity contractProcessEntity=this.parseContractDefine(contract.getProcessDefintion().getBytes(StandardCharsets.UTF_8));
        BpmnModelInstance bpmnModelInstance = contractProcessEntity.getBpmnModelInstance();
        Process process=contractProcessEntity.getProcess();
        if(contract.getContractId()!=null){
            process.setId("contract_"+contract.getContractId());
        }
        process.setName(contract.getName());
        ProcessUtil.fillSendtaskImplementation(process);
        List<TaskWrapper> tasks = contractProcessEntity.getTasks();
        if(tasks.isEmpty()){
            log.warn("流程定义缺少活动配置信息,合约id{}",contract.getContractId());
            throw new ApplicationException(HttpStatus.UNPROCESSABLE_ENTITY,PARAMETER_DISCREPANCY.getCode(),"流程定义缺少活动配置信息");
        }
        for (TaskWrapper task : tasks) {
            createOrUpdateOrgConnectorInTask(contract, entId, resultBean, task);
        }
        String processDefinitionXml = Bpmn.convertToString(bpmnModelInstance);
        contract.setProcessDefintion(processDefinitionXml);
    }

    /**
     * 创建或更新在任务中的组织连接器
     *
     * @param contract          合同
     * @param entId             ent id
     * @param resultBean        结果豆
     * @param task              任务
     */
    private void createOrUpdateOrgConnectorInTask(Contract contract, String entId, ResultBean<String> resultBean,  TaskWrapper task) {
        if(!task.isTypeOf(SendTask.class) && !task.isTypeOf(ReceiveTask.class)){
            resultBean.setMessage("不受支持的活动类型，活动名称："+ task.getTaskName());
            throw new ApplicationException(HttpStatus.UNPROCESSABLE_ENTITY, resultBean);
        }


        if(StringUtils.isBlank(task.getMemberIdString())){
            resultBean.setMessage("活动无协作组织，活动名称："+ task.getTaskName());
            log.warn("发布的流程中，活动id【{}】，活动名称【{}】的活动，没有组织ID信息。", task.getTaskId(),task.getTaskName());
            throw new ApplicationException(HttpStatus.UNPROCESSABLE_ENTITY, resultBean);
        }
        if(!StringUtils.equals(task.getMemberIdString(),entId)){
            return;
        }

        if (StringUtils.isBlank(task.getMemberConnectorIdString())) {
            resultBean.setMessage("活动无连接器信息，活动名称："+ task.getTaskName());
            log.warn("发布的流程中，活动id【{}】的活动，没有连接器ID信息。", task.getTaskId());
            throw new ApplicationException(HttpStatus.UNPROCESSABLE_ENTITY, resultBean);
        }
        if (StringUtils.isBlank(task.getEventActionIdString())) {
            resultBean.setMessage("活动的连接器缺少信息，活动名称："+ task.getTaskName());
            log.warn("发布的流程中，活动id【{}】的活动，没有连接器的执行动作/触发事件ID信息。", task.getTaskId());
            throw new ApplicationException(HttpStatus.UNPROCESSABLE_ENTITY, resultBean);
        }

        ConnectorTask connectorTask = getConnectorTaskId4ProcessTask(contract, resultBean, task);
        task.setDxpId(connectorTask.getDxpId());
    }

    /**
     * 对task创建或更新连接器任务
     *
     * @param contract          合同
     * @param resultBean        结果豆
     * @param task              任务
     * @return {@link ConnectorTask}
     */

    private ConnectorTask getConnectorTaskId4ProcessTask(Contract contract, ResultBean<String> resultBean, TaskWrapper task) {
        ConnectorTask connectorTask=null;
        if(StringUtils.isNotBlank(task.getMemberConnectorTaskIdString())){
            log.info("调用连接器任务详情接口，入参MemberConnectorTaskId：{}，企业id：{}",task.getMemberConnectorTaskIdString(),task.getMemberIdString());
            ConnectorTaskResult instanceResult=this.connectorManageClient.getOrgConnectorTask(task.getMemberConnectorTaskIdString(), task.getMemberIdString());
            if(instanceResult.getCode()==0){
                connectorTask =instanceResult.getData();
                if(!StringUtils.equals(connectorTask.getOrgConnectorId(), task.getMemberConnectorIdString())){
                    log.info("调用删除连接器接口，入参ConnectorTaskId：{}，memberid：{}",connectorTask.getConnectorTaskId(),task.getMemberIdString());
                    this.connectorManageClient.deleteOrgConnectorTask(connectorTask.getConnectorTaskId(), task.getMemberIdString());
                    connectorTask =null;
                    task.getMemberConnectorTaskId().setCamundaValue(null);
                }
            }else {
                task.getMemberConnectorTaskId().setCamundaValue(null);
            }
        }else if(task.getMemberConnectorTaskId()!=null){
            task.getMemberConnectorTaskId().setCamundaValue(null);
        }
        ConnectorTaskResult connectorTaskResult;
        if(connectorTask ==null){
            connectorTask =new ConnectorTask();
            connectorTask.setContractId(String.valueOf(contract.getContractId()));
            connectorTask.setActivityCode(task.getTaskId());
            connectorTask.setEventActionId(task.getEventActionIdString());
            connectorTask.setOrgConnectorId(task.getMemberConnectorIdString());
            if(StringUtils.isNotBlank(task.getConnectorTaskParaString())){
                try {
                    connectorTask.setEventActionParams(this.objectMapper.readValue(task.getConnectorTaskParaString(),Object.class));
                } catch (JsonProcessingException e) {
                    resultBean.setMessage("活动参数不符合格式规范："+ task.getTaskName());
                    log.warn("发布的流程中，活动id【{}】，活动名称【{}】的活动，参数不符合规范，参数：{}。", task.getTaskId(),task.getTaskName(),task.getConnectorTaskParaString());
                    throw new ApplicationException(HttpStatus.UNPROCESSABLE_ENTITY, resultBean);
                }
            }
            log.info("调用创建连接器任务接口，入参：{}，企业id：{}",connectorTask,contract.getOrganizationId());
            connectorTaskResult =this.connectorManageClient.createOrgConnectorTask(connectorTask, contract.getOrganizationId());
        }else {
            if(StringUtils.isNotBlank(task.getConnectorTaskParaString())){
                try {
                    connectorTask.setEventActionParams(this.objectMapper.readValue(task.getConnectorTaskParaString(),Object.class));
                } catch (JsonProcessingException e) {
                    resultBean.setMessage("活动参数不符合格式规范："+ task.getTaskName());
                    log.warn("发布的流程中，活动id【{}】，活动名称【{}】的活动，参数不符合规范，参数：{}。", task.getTaskId(),task.getTaskName(),task.getConnectorTaskParaString());
                    throw new ApplicationException(HttpStatus.UNPROCESSABLE_ENTITY, resultBean);
                }
            }
            log.info("调用修改连接器任务接口，入参ConnectorTaskId：{}，企业id：{}",connectorTask,contract.getOrganizationId());
            connectorTaskResult =this.connectorManageClient.updateOrgConnectorTask(connectorTask.getConnectorTaskId(), connectorTask, contract.getOrganizationId());
        }

        return dealConnectorTaskResult(contract, resultBean, task, connectorTask, connectorTaskResult);
    }

    /**
     * 处理创建或更新连接器任务的结果
     *
     * @param contract            合同
     * @param resultBean          结果豆
     * @param task                任务
     * @param connectorTask       连接器任务
     * @param connectorTaskResult 连接器任务结果
     * @return {@link ConnectorTask}
     */
    private ConnectorTask dealConnectorTaskResult(Contract contract, ResultBean<String> resultBean, TaskWrapper task, ConnectorTask connectorTask, ConnectorTaskResult connectorTaskResult) {
        if(connectorTaskResult.getCode()!=0){
            if(-10001001== connectorTaskResult.getCode()){
                resultBean.setMessage("["+ task.getTaskName()+"活动的参数不符合检验规则，请检查参数配置。"+"]");
            }else {
                resultBean.setMessage("连接器发布失败，可以尝试重新发布，如多次失败，请联系客服。活动名称："+ task.getTaskName());
            }
            log.error("发布的流程中，活动id【{}】，活动【{}】的组织连接器任务创建/更新失败，协作组织：{},参数：{},结果：{}。", task.getTaskId(), task.getTaskName(), contract.getOrganizationId(), connectorTask, connectorTaskResult);
            throw new ApplicationException(HttpStatus.UNPROCESSABLE_ENTITY, resultBean);
        }
        ConnectorTask connectorTaskNew = connectorTaskResult.getData();
        if(StringUtils.isBlank(connectorTaskNew.getConnectorTaskId())){
            resultBean.setMessage("连接器发布失败，不能创建活动对应的连接器，可以尝试重新发布，如多次失败，请联系客服。活动名称："+ task.getTaskName());
            log.error("发布的流程中，活动id【{}】的组织连接器任务创建/更新失败，未返回连接器任务ID，协作组织：{},参数：{},结果：{}。", task.getTaskId(), contract.getOrganizationId(), connectorTask, connectorTaskResult);
            throw new ApplicationException(HttpStatus.UNPROCESSABLE_ENTITY, resultBean);
        }

        if(StringUtils.isBlank(connectorTaskNew.getDxpId())){
            resultBean.setMessage("连接器发布失败，活动对应的连接器没有传输信息，可以尝试重新发布，如多次失败，请联系客服。活动名称："+ task.getTaskName());
            log.error("发布的流程中，活动id【{}】的组织连接器任务创建/更新失败，未返回连接器的传输ID，协作组织：{},参数：{},结果：{}。", task.getTaskId(), contract.getOrganizationId(), connectorTask, connectorTaskResult);
            throw new ApplicationException(HttpStatus.UNPROCESSABLE_ENTITY, resultBean);
        }
        task.setMemberConnectorTaskIdString(connectorTaskNew.getConnectorTaskId());
        return connectorTaskNew;
    }


    /**
     * 发布合约
     *
     * @param contract 合同
     */
    private void publishProcess(Contract contract) {
        BpmnModelInstance bpmnModelInstance;
        try(InputStream stream = new ByteArrayInputStream(contract.getProcessDefintion().getBytes(StandardCharsets.UTF_8))) {
            bpmnModelInstance = Bpmn.readModelFromStream(stream);
        } catch (Exception e) {
            log.error("流程图解析异常.",e);
            throw new ApplicationException(HttpStatus.UNPROCESSABLE_ENTITY,"流程图解析出错，请检查是否规范！");
        }
        RepositoryService repositoryService = this.processEngine.getRepositoryService();
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
                .addModelInstance(contract.getName()+".bpmn",bpmnModelInstance)
                .name(contract.getName());

        DeploymentWithDefinitions deploymentWithDefinitions;
        try {
            deploymentWithDefinitions = deploymentBuilder.deployWithResult();
        } catch (Exception e) {
            log.error("部署流程图时，出现异常，流程图：{}",contract.getProcessDefintion(),e);
            throw new ApplicationException(HttpStatus.UNPROCESSABLE_ENTITY,"发布失败,如重复失败，请联系客服。");
        }
        List<ProcessDefinition> deployedProcessDefinitions = deploymentWithDefinitions.getDeployedProcessDefinitions();

        if(CollectionUtil.isEmpty(deployedProcessDefinitions)){
            throw new ApplicationException(HttpStatus.UNPROCESSABLE_ENTITY,"发布失败,没有返回流程定义信息");
        }else{
            for(ProcessDefinition processDefinition : deployedProcessDefinitions){
                InputStream processModelIn=repositoryService.getProcessModel(processDefinition.getId());
                bpmnModelInstance = Bpmn.readModelFromStream(processModelIn);
                contract.setProcessDefintion(Bpmn.convertToString(bpmnModelInstance));

                contract.setProcessDefinitionId(processDefinition.getId());
                contract.setProcessDefinitionKey(processDefinition.getKey());
            }
        }
    }

    /**
     * 向区块链智能合约投票
     *
     * @param contract 合同
     * @param orgId    org id
     */
    private void vote2SmartContract(Contract contract , String orgId){
        if(StringUtils.isBlank(contract.getProcessDefintion())){
            return;
        }
        List<String> memberIds = new ArrayList<>();
        ContractProcessEntity contractProcessEntity=this.parseContractDefine(contract.getProcessDefintion().getBytes(StandardCharsets.UTF_8));
        List<TaskWrapper> tasks = contractProcessEntity.getTasks();
        if(tasks.isEmpty()){
            return ;
        }
        for (TaskWrapper task : tasks) {
            memberIds.add(task.getMemberIdString());
        }
        List<String> collect = memberIds.stream().distinct().collect(Collectors.toList());
        Smartcontract smartcontract = new Smartcontract();
        smartcontract.setVoteTall(collect.size());
        smartcontract.setContractName(contract.getContractId().toString());
        smartcontract.setOrgId(orgId);
        CommonResult<String> stringCommonResult = smartContractClient.vote(smartcontract, orgId);
        if(stringCommonResult.getCode()!=200){
            log.error("企业{}发送智能合约出错,返回信息{}",orgId,stringCommonResult.getMessage());
            throw new ApplicationException(HttpStatus.UNPROCESSABLE_ENTITY,stringCommonResult.getMessage());
        }
    }

    /**
     * 保存组织连接器id
     *
     * @param contract 合同
     * @param orgId    org id
     */
    private void saveMemberConnectorId(Contract contract ,String orgId){
        if(StringUtils.isBlank(contract.getProcessDefintion())){
            return ;
        }

        ContractProcessEntity contractProcessEntity=this.parseContractDefine(contract.getProcessDefintion().getBytes(StandardCharsets.UTF_8));
        List<TaskWrapper> tasks = contractProcessEntity.getTasks();
        if(tasks.isEmpty()){
            log.warn("流程定义缺少活动配置信息,合约id{}",contract.getContractId());
            throw new ApplicationException(HttpStatus.UNPROCESSABLE_ENTITY,PARAMETER_DISCREPANCY.getCode(),"流程定义缺少活动配置信息");
        }
        for (TaskWrapper task : tasks) {
            this.saveMemberConnectorIdInTask(orgId,task);
        }

        String processDefinitionXml = Bpmn.convertToString(contractProcessEntity.getBpmnModelInstance());
        contract.setProcessDefintion(processDefinitionXml);
    }

    /**
     * 保存任务中的连接器id
     *
     * @param orgId    org id
     * @param task     任务
     */
    private void saveMemberConnectorIdInTask(String orgId, TaskWrapper task) {
        if(StringUtils.isBlank(task.getMemberIdString())) {
            throw new ApplicationException(HttpStatus.UNPROCESSABLE_ENTITY,PARAMETER_DISCREPANCY.getCode(),"活动中组织不能为空");
        }
        if(StringUtils.equals(orgId,task.getMemberConnectorIdString()) && StringUtils.isNotBlank(task.getConnectorIdString())){
            String orgConnectorId=getOrgConnectorIdByConnectorId(orgId,task,task.getConnectorIdString());
            task.setMemberConnectorIdString(orgConnectorId);
        }


    }

    /**
     * 通过连接器id获取组织连接器ID
     *
     * @param orgId           org id
     * @param task            任务
     * @param connectorId       连接器ID
     * @return {@link String}
     */
    private String getOrgConnectorIdByConnectorId(String orgId, TaskWrapper task, String connectorId) {
        log.info("调用组织连接器接口，入参connectorId：{}，企业id：{}",connectorId,orgId);
        OraConnectorListResult orgConnectors = this.connectorManageClient.getOrgConnectors(connectorId, orgId);
        if(orgConnectors.getCode()<0){
            log.error("没有查询到活动{}的连接器id为{}的组织连接器id", task.getTaskId(), connectorId);
            throw new ApplicationException(HttpStatus.UNPROCESSABLE_ENTITY,"没有查询到活动{"+ task.getTaskName()+"}的组织连接器。");
        }else{
            List<OrgConnector> data = orgConnectors.getData();
            if(data.size()!=1){
                log.error("没有查询到活动{}的连接器id为{}的组织连接器，超过1个。", task.getTaskId(), connectorId);
                throw new ApplicationException(HttpStatus.UNPROCESSABLE_ENTITY,"数据有误，活动{"+ task.getTaskName()+"的组织连接器不唯一。");
            }
            return data.get(0).getOrgConnectorId();

        }
    }

    private void partakeEdit(Contract contract ,ContractUpDate contractUpDate,String endId) {
        //获取我的所有活动可编辑信息
        if (StringUtils.isBlank(contract.getProcessDefintion()) || StringUtils.isBlank(contractUpDate.getProcessDefine())) {
            return ;
        }

        ContractProcessEntity contractProcessEntity=this.parseContractDefine(contract.getProcessDefintion().getBytes(StandardCharsets.UTF_8));
        List<TaskWrapper> tasksInDb = contractProcessEntity.getTasks();
        List<TaskWrapper> taskOwned=new ArrayList<>();
        for (TaskWrapper taskWrapper : tasksInDb) {
            if(StringUtils.equals(taskWrapper.getMemberIdString(),endId)){
                taskOwned.add(taskWrapper);
            }
        }
        if(taskOwned.isEmpty()){
            return;
        }
        Map<String,TaskWrapper> tasksUpdate=getAllUpdateTask(contractUpDate.getProcessDefine());
        for (TaskWrapper taskWrapper : taskOwned) {
            TaskWrapper taskWrapperUpdate = tasksUpdate.get(taskWrapper.getTaskId());
            if(taskWrapperUpdate!=null){
                taskWrapper.setConnectorTaskParaString(taskWrapperUpdate.getConnectorTaskParaString());
                taskWrapper.setMemberConnectorIdString(taskWrapperUpdate.getMemberConnectorIdString());
                taskWrapper.setMemberConnectorTaskIdString(taskWrapperUpdate.getMemberConnectorTaskIdString());
                taskWrapper.setMainActivity(taskWrapperUpdate.getMainActivity());
                taskWrapper.setEventActionIdString(taskWrapperUpdate.getEventActionIdString());
                taskWrapper.setConnectorIdString(taskWrapperUpdate.getConnectorIdString());
            }
        }

        String processDefinitionXml = Bpmn.convertToString(contractProcessEntity.getBpmnModelInstance());
        contract.setProcessDefintion(processDefinitionXml);

    }


    private Map<String,TaskWrapper> getAllUpdateTask(String processDefine){
        ContractProcessEntity contractProcessEntity=this.parseContractDefine(processDefine.getBytes(StandardCharsets.UTF_8));
        List<TaskWrapper> tasksUpdate = contractProcessEntity.getTasks();
        Map<String,TaskWrapper> taskWrapperMap=new HashMap<>(tasksUpdate.size());
        for (TaskWrapper taskWrapper : tasksUpdate) {
            taskWrapperMap.put(taskWrapper.getTaskId(),taskWrapper);
        }
        return taskWrapperMap;
    }

    /**
     * 禁止发起者编辑的内容
     *
     * @param processDefinitionNew 流程定义新
     * @param orgId                org id
     * @return {@link String}
     */
    private String forbidden4OriginatorEdit(String processDefinitionNew, String orgId) {
        if(StringUtils.isBlank(processDefinitionNew)){
            return null;
        }
        ContractProcessEntity contractProcessEntity=this.parseContractDefine(processDefinitionNew.getBytes(StandardCharsets.UTF_8));
        List<TaskWrapper> tasks = contractProcessEntity.getTasks();
        for (TaskWrapper task : tasks) {
            if(!StringUtils.equals(task.getMemberIdString(),orgId)){
                task.setConnectorTaskParaString(null);
            }
        }
        return Bpmn.convertToString(contractProcessEntity.getBpmnModelInstance());
    }

    /**
     * 校验流程定义
     *
     * @param processDefinition 流程定义
     * @param resultBean        结果豆
     */
    private void validateProcess(String processDefinition,ResultBean<?> resultBean){
        if(resultBean==null){
            resultBean=new ResultBean<>();
        }
        if(!ProcessUtil.validateProcess(processDefinition,resultBean)){
            throw new ApplicationException(HttpStatus.UNPROCESSABLE_ENTITY,resultBean);
        }
    }

    private ContractProcessEntity parseContractDefine(byte[] xml){
        try {
            return new ContractProcessEntity(xml);
        } catch (BpmnModelParseException | ProcessParseException e) {
            throw new ApplicationException(HttpStatus.UNPROCESSABLE_ENTITY,PARAMETER_DISCREPANCY.getCode(),"活动缺少必要的信息。");
        }
    }

    private Process getProcessFromContract(Contract contract){
        try {
            return ProcessUtil.getProcessFromXml(contract.getProcessDefintion().getBytes(StandardCharsets.UTF_8));
        } catch (BpmnModelParseException |ProcessParseException e ) {
            log.warn("bpmn中无流程定义，无法完成操作，合约编号：{}。",contract.getContractId(),e);
            throw new ApplicationException(HttpStatus.BAD_REQUEST,StatusEnum.OPERATION_NOT_ALLOWED.getCode(),"无合约定义，无法完成操作。");
        }catch (Exception e){
            log.warn("解析bpmn中出现异常，无法完成操作，合约编号：{}。",contract.getContractId(),e);
            throw new ApplicationException(HttpStatus.BAD_REQUEST);
        }
    }

    private void judgeNotExistContract(Contract contract){
        if(null==contract){
            throw new ApplicationException(HttpStatus.BAD_REQUEST,StatusEnum.OPERATION_NOT_ALLOWED.getCode(),"不存在的合约");
        }
    }

}
