package com.aeotrade.chain.contract.controller;

import com.aeotrade.chain.contract.constants.ContractConstants;
import com.aeotrade.chain.contract.po.Contract;
import com.aeotrade.chain.contract.po.ContractOrgRelease;
import com.aeotrade.chain.contract.po.ContractRecord;
import com.aeotrade.chain.contract.po.ContractTemplateKindRel;
import com.aeotrade.chain.contract.function.JoinedContractQueryByContractTplIdConsumer;
import com.aeotrade.chain.contract.service.IContractOrgReleaseService;
import com.aeotrade.chain.contract.service.IContractRecordService;
import com.aeotrade.chain.contract.service.IContractService;
import com.aeotrade.chain.contract.service.IContractTemplateKindRelService;
import com.aeotrade.chain.contract.vo.ContractRecordListVo;
import com.aeotrade.chain.contract.vo.ResultBean;
import com.aeotrade.chain.contract.vo.StatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  协作日志
 * </p>
 *
 * @author chl
 * @since 2022-10-21
 */
@RestController
@RequestMapping("/contractrecord")
public class ContractRecordController {

    @Autowired
    private IContractRecordService iContractRecordService;
    @Autowired
    private IContractService iContractService;
    @Autowired
    private IContractOrgReleaseService iContractOrgReleaseService;
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private IContractTemplateKindRelService iContractTemplateKindRelService;

    @GetMapping()
    public ResponseEntity<ResultBean<IPage<ContractRecordListVo>>> indexList(@RequestParam(name = "contractname", required = false) String contractName,
                                                                             @RequestParam(name = "contractid", required = false) Long contractId,
                                                                             @RequestParam(name = "contractrecordid", required = false) Long contractRecordId,
                                                                             @RequestParam(name = "startdate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
                                                                             @RequestParam(name = "enddate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate,
                                                                             @RequestParam(name = "currentpage") int currentPage,
                                                                             @RequestParam(name = "pagesize") int pageSize,
                                                                             @RequestParam(name = "categorycode", required = false) List<String> categoryCode,
                                                                             @RequestHeader(name = "memberId") String memberId) {
        ResultBean<IPage<ContractRecordListVo>> resultBean = new ResultBean<>();
        if(StringUtils.isBlank(memberId)){
            resultBean.setStatusEnum(StatusEnum.PARAMETER_EXCEPTION);
            resultBean.setMessage("企业id不能为空");
            return ResponseEntity.unprocessableEntity().body(resultBean);
        }
        ContractRecord contractRecord = new ContractRecord();
        ContractOrgRelease contractOrgRelease = new ContractOrgRelease();
        contractOrgRelease.setCollaborationOrgId(memberId);
        contractOrgRelease.setReleaseStatus(ContractConstants.RELEASE_STATUS_PUBLISHED.getCode());
        if(contractId!=null){
            contractOrgRelease.setContractId(contractId);
        }
        QueryWrapper<ContractOrgRelease> releaseQueryWrapper = new QueryWrapper<>(contractOrgRelease);
        List<ContractOrgRelease> idList = iContractOrgReleaseService.list(releaseQueryWrapper);
        List<Long> ids = new ArrayList<>();
        for (ContractOrgRelease orgRelease : idList) {
            ids.add(orgRelease.getContractId());
        }
        if(CollectionUtils.isEmpty(ids)){
            contractRecord.setOrganizationId(memberId);
            if(contractId!=null){
                contractRecord.setContractId(contractId);
            }
        }
        if(contractRecordId!=null){
            contractRecord.setContractRecordId(contractRecordId);
        }
        QueryWrapper<ContractRecord> queryWrapper = new QueryWrapper<>(contractRecord);

        //根据分类查询
        List<Long> idsBykind = new ArrayList<>();
        if(!CollectionUtils.isEmpty(categoryCode)){
            List<ContractTemplateKindRel> contractTemplateKindRels = iContractTemplateKindRelService.queryContractTemplateKindRelByCategoryCode(categoryCode);
            QueryWrapper<Contract> contractQueryWrapper = new QueryWrapper<>();
            contractQueryWrapper.and(new JoinedContractQueryByContractTplIdConsumer(contractTemplateKindRels));
            contractQueryWrapper.select(Contract.CONTRACT_ID);
            List<Contract> list = iContractService.list(contractQueryWrapper);
            for (Contract contract : list) {
                idsBykind.add(contract.getContractId());
            }
            idsBykind = idsBykind.stream()
                    .distinct()
                    .collect(Collectors.toList());
            queryWrapper.in(ContractRecord.CONTRACT_ID,idsBykind);
        }
        if(!CollectionUtils.isEmpty(ids)){
            ids = ids.stream().distinct().collect(Collectors.toList());
            queryWrapper.in(ContractRecord.CONTRACT_ID,ids);
        }
        if(StringUtils.isNotBlank(contractName)){
            queryWrapper.like(ContractRecord.NAME, contractName);
        }
        if(null!=startDate && null!= endDate){
            queryWrapper.ge(ContractRecord.CREATE_DATE,startDate).le(ContractRecord.CREATE_DATE,endDate);
        }
        queryWrapper.orderByDesc(ContractRecord.CREATE_DATE);

        Page<ContractRecord> page = new Page<>(currentPage, pageSize);
        Page<ContractRecord> contractRecordListIPage = iContractRecordService.page(page, queryWrapper);

        List<ContractRecord> records = contractRecordListIPage.getRecords();
        List<ContractRecordListVo> contractRecordListVos = convertContractRecord2ContractRecordListVo(records);

        IPage<ContractRecordListVo> contractRecordListVOIPage = new Page<>();
        contractRecordListVOIPage.setRecords(contractRecordListVos);
        contractRecordListVOIPage.setSize(records.size());
        contractRecordListVOIPage.setCurrent(contractRecordListIPage.getCurrent());
        contractRecordListVOIPage.setPages(contractRecordListIPage.getPages());
        contractRecordListVOIPage.setTotal(contractRecordListIPage.getTotal());
        resultBean.setData(contractRecordListVOIPage);
        if(contractRecordListIPage.getTotal()<1){
            resultBean.setStatusEnum(StatusEnum.SUCCESS_NO_DATA);
        }else {
            resultBean.setStatusEnum(StatusEnum.SUCCESS);
        }
        return ResponseEntity.ok(resultBean);
    }


    private List<ContractRecordListVo> convertContractRecord2ContractRecordListVo(List<ContractRecord> records){
        List<ContractRecordListVo> contractRecordListVos = new ArrayList<>();
        for (ContractRecord contractRecordItem : records) {
            ContractRecordListVo contractRecordListVO = new ContractRecordListVo();
            HistoryService historyService = this.processEngine.getHistoryService();
            List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(contractRecordItem.getProcessInstanceId()).finished()
                    .orderByHistoricActivityInstanceEndTime()
                    .desc()
                    .list();
            for (HistoricActivityInstance historicActivityInstance : list) {
                if(historicActivityInstance.getActivityType().equalsIgnoreCase(ActivityTypes.TASK_SEND_TASK) || historicActivityInstance.getActivityType().equalsIgnoreCase(ActivityTypes.TASK_RECEIVE_TASK)){
                    contractRecordListVO.setActivity(historicActivityInstance.getActivityId()+"  "+historicActivityInstance.getActivityName());
                    contractRecordListVO.setActivityTime(historicActivityInstance.getEndTime());
                    break;
                }
            }
            contractRecordListVO.setContractRecordId(String.valueOf(contractRecordItem.getContractRecordId()));
            contractRecordListVO.setContractId(String.valueOf(contractRecordItem.getContractId()));
            contractRecordListVO.setContractName(contractRecordItem.getName());
            contractRecordListVO.setCreateTime(contractRecordItem.getCreateDate());
            contractRecordListVO.setEntContractRecordNo(contractRecordItem.getOrgContractRecordNo());
            contractRecordListVos.add(contractRecordListVO);
        }
        return contractRecordListVos;
    }


}
