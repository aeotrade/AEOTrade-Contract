package com.aeotrade.chain.contract.controller;

import com.aeotrade.chain.contract.po.Contract;
import com.aeotrade.chain.contract.po.ContractOrgRelease;
import com.aeotrade.chain.contract.service.IContractOrgReleaseService;
import com.aeotrade.chain.contract.service.IContractService;
import com.aeotrade.chain.contract.vo.ContractOrgReleaseVo;
import com.aeotrade.chain.contract.vo.ResultBean;
import com.aeotrade.chain.contract.vo.StatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author chl
 * @since 2022-12-07
 */
@RestController
@RequestMapping("/cor")
public class ContractOrgReleaseController {

    @Autowired
    private IContractOrgReleaseService iContractOrgReleaseService;
    @Autowired
    private IContractService iContractService;

    @GetMapping()
    public ResponseEntity<ResultBean<List<ContractOrgReleaseVo>>> indexList(@RequestParam(name = "contractid") @NotBlank Long contractId, @RequestHeader(name = "memberId")@NotBlank String entId) {
        ResultBean<List<ContractOrgReleaseVo>> resultBean = new ResultBean<>();
        List<ContractOrgReleaseVo> orgReleaseVOS = new ArrayList<>();
        ContractOrgRelease contractOrgRelease = new ContractOrgRelease();
        contractOrgRelease.setContractId(contractId);
        QueryWrapper<ContractOrgRelease> queryWrapper = new QueryWrapper<>(contractOrgRelease);
        List<ContractOrgRelease> list = iContractOrgReleaseService.list(queryWrapper);
        if(list.isEmpty()){
            resultBean.setStatusEnum(StatusEnum.SUCCESS_NO_DATA);
            ResponseEntity.ok(resultBean);
        }
        boolean isInOrgRelease=false;
        for (ContractOrgRelease orgRelease : list) {
            ContractOrgReleaseVo contractOrgReleaseVO = new ContractOrgReleaseVo();
            contractOrgReleaseVO.setCollaborationOrgId(orgRelease.getCollaborationOrgId());
            contractOrgReleaseVO.setReleaseStatus(orgRelease.getReleaseStatus());
            contractOrgReleaseVO.setReleaseTime(orgRelease.getReleaseTime());
            if(StringUtils.equals(entId,orgRelease.getCollaborationOrgId())){
                isInOrgRelease=true;
            }
            orgReleaseVOS.add(contractOrgReleaseVO);
        }

        if(!isInOrgRelease){
            Contract contract=new Contract();
            contract.setContractId(contractId);
            contract.setOrganizationId(entId);
            QueryWrapper<Contract> contractQueryWrapper=new QueryWrapper<>(contract);
            contract=this.iContractService.getOne(contractQueryWrapper,false);
            if(contract==null){
                resultBean.setStatusEnum(StatusEnum.OPERATION_NOT_ALLOWED);
                return ResponseEntity.unprocessableEntity().body(resultBean);
            }
        }

        resultBean.setData(orgReleaseVOS);
        resultBean.setStatusEnum(StatusEnum.SUCCESS);
        return ResponseEntity.ok(resultBean);
    }

}
