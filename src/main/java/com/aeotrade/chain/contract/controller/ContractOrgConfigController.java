package com.aeotrade.chain.contract.controller;

import com.aeotrade.chain.contract.po.ContractOrgAlias;
import com.aeotrade.chain.contract.po.ContractOrgConfig;
import com.aeotrade.chain.contract.mapstruct.ContractOrgConfigConvert;
import com.aeotrade.chain.contract.service.IContractOrgAliasService;
import com.aeotrade.chain.contract.service.IContractOrgConfigService;
import com.aeotrade.chain.contract.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author chl
 * @since 2023-06-26
 */
@RestController
@RequestMapping("/coc")
@Slf4j
public class ContractOrgConfigController {

    @Autowired
    private IContractOrgConfigService iContractOrgConfigService;
    @Autowired
    private ContractOrgConfigConvert contractOrgConfigConvert;
    @Autowired
    private IContractOrgAliasService iContractOrgAliasService;

    @PostMapping()
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<ResultBean<ContractOrgConfigVo>> hiddenContract(@Validated @RequestBody ContractOrgConfigSave saveVo, @RequestHeader(name = "memberId")@NotBlank String entId) {
        ResultBean<ContractOrgConfigVo> resultBean = new ResultBean<>();
        //检查要隐藏的合约和当前企业是否有关联
        ContractOrgAlias contractOrgAlias = new ContractOrgAlias();
        contractOrgAlias.setContractId(Long.valueOf(saveVo.getContractId()));
        contractOrgAlias.setCollaborationOrgId(entId);
        QueryWrapper<ContractOrgAlias> aliasQueryWrapper = new QueryWrapper<>(contractOrgAlias);
        long count = iContractOrgAliasService.count(aliasQueryWrapper);
        if(count==0){
            resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
            resultBean.setMessage("没有相关权限");
            return ResponseEntity.badRequest().body(resultBean);
        }
        ContractOrgConfig contractOrgConfig = new ContractOrgConfig();
        contractOrgConfig.setContractId(Long.valueOf(saveVo.getContractId()));
        contractOrgConfig.setCollaborationOrgId(entId);
        QueryWrapper<ContractOrgConfig> queryWrapper = new QueryWrapper<>(contractOrgConfig);
        long contractOrgConfigCount = iContractOrgConfigService.count(queryWrapper);
        if(contractOrgConfigCount>0){
            ContractOrgConfig updatedContractOrgConfig = new ContractOrgConfig();
            updatedContractOrgConfig.setHidden(true);
            updatedContractOrgConfig.setHiddenOpTime(new Date());
            boolean update = iContractOrgConfigService.update(updatedContractOrgConfig, queryWrapper);
            if(!update){
                log.warn("更新ContractOrgConfig时出错，{}",updatedContractOrgConfig);
            }
        }else{
            contractOrgConfig.setHidden(true);
            contractOrgConfig.setHiddenOpTime(new Date());
            boolean save = iContractOrgConfigService.save(contractOrgConfig);
            if(!save){
                log.warn("保存ContractOrgConfig时出错，{}",contractOrgConfig);
            }
        }

        ContractOrgConfigVo contractOrgConfigVo = this.contractOrgConfigConvert.dtoToVo(contractOrgConfig);
        resultBean.setStatusEnum(StatusEnum.SUCCESS);
        resultBean.setData(contractOrgConfigVo);
        return ResponseEntity.ok(resultBean);
    }



    @PutMapping()
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<ResultBean<ContractOrgConfigVo>> updateContract(@Validated @RequestBody ContractOrgConfigUpdate update, @RequestHeader(name = "memberId")@NotBlank String entId) {
        ResultBean<ContractOrgConfigVo> resultBean = new ResultBean<>();

        //检查要隐藏的合约和当前企业是否有关联
        ContractOrgAlias contractOrgAlias = new ContractOrgAlias();
        contractOrgAlias.setContractId(Long.valueOf(update.getContractId()));
        contractOrgAlias.setCollaborationOrgId(entId);
        QueryWrapper<ContractOrgAlias> aliasQueryWrapper = new QueryWrapper<>(contractOrgAlias);
        long count = iContractOrgAliasService.count(aliasQueryWrapper);
        if(count==0){
            resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
            resultBean.setMessage("没有相关权限");
            return ResponseEntity.badRequest().body(resultBean);
        }
        ContractOrgConfig contractOrgConfig = new ContractOrgConfig();
        contractOrgConfig.setContractId(Long.valueOf(update.getContractId()));
        contractOrgConfig.setCollaborationOrgId(entId);
        QueryWrapper<ContractOrgConfig> configQueryWrapper = new QueryWrapper<>(contractOrgConfig);
        configQueryWrapper.select(ContractOrgConfig.CONTRACT_ID);
        long contractOrgConfigCount = iContractOrgConfigService.count(configQueryWrapper);
        if(contractOrgConfigCount==0){
            resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
            return ResponseEntity.badRequest().body(resultBean);
        }
        ContractOrgConfig updatedContractOrgConfig = new ContractOrgConfig();
        updatedContractOrgConfig.setHidden(update.getHidden());
        updatedContractOrgConfig.setHiddenOpTime(new Date());
        boolean isUpdate = iContractOrgConfigService.update(updatedContractOrgConfig, configQueryWrapper);
        if(!isUpdate){
            log.warn("更新ContractOrgConfig时出错，{}",updatedContractOrgConfig);
        }
        ContractOrgConfigVo contractOrgConfigVo = this.contractOrgConfigConvert.dtoToVo(contractOrgConfig);
        resultBean.setStatusEnum(StatusEnum.SUCCESS);
        resultBean.setData(contractOrgConfigVo);
        return ResponseEntity.ok(resultBean);
    }
}
