package com.aeotrade.chain.contract.controller;

import com.aeotrade.chain.contract.po.ContractTemplateKind;
import com.aeotrade.chain.contract.service.IContractTemplateKindService;
import com.aeotrade.chain.contract.vo.ContractTemplateKindVo;
import com.aeotrade.chain.contract.vo.ResultBean;
import com.aeotrade.chain.contract.vo.StatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author chl
 * @since 2023-02-20
 */
@RestController
@RequestMapping(ContractTemplateKindController.API_PREFIX)
public class ContractTemplateKindController {
    public static final String API_PREFIX="/contracttplkind";

    @Autowired
    private IContractTemplateKindService iContractTemplateKindService;
    @GetMapping()
    public ResponseEntity<ResultBean<List<ContractTemplateKindVo>>> indexList() {
        ResultBean<List<ContractTemplateKindVo>> resultBean = new ResultBean<>();
        List<ContractTemplateKindVo> contractTemplateKindVos = new ArrayList<>();
        QueryWrapper<ContractTemplateKind> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc(ContractTemplateKind.SORT);
        List<ContractTemplateKind> list = iContractTemplateKindService.list(queryWrapper);
        for (ContractTemplateKind contractTemplateKind : list) {
            ContractTemplateKindVo contractTemplateKindVo = new ContractTemplateKindVo();
            contractTemplateKindVo.setCategoryName(contractTemplateKind.getCategoryName());
            contractTemplateKindVo.setCategoryCode(contractTemplateKind.getCategoryCode());
            contractTemplateKindVo.setDescription(contractTemplateKind.getDescription());
            contractTemplateKindVos.add(contractTemplateKindVo);
        }
        resultBean.setData(contractTemplateKindVos);
        resultBean.setStatusEnum(StatusEnum.SUCCESS);
        return ResponseEntity.ok(resultBean);
    }
}
