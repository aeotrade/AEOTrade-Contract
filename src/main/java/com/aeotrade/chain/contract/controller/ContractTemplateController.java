package com.aeotrade.chain.contract.controller;

import com.aeotrade.chain.contract.po.*;
import com.aeotrade.chain.contract.mapstruct.ContractTemplateConvert;
import com.aeotrade.chain.contract.mapstruct.ContractTemplateOrgAliasConvert;
import com.aeotrade.chain.contract.service.IContractTemplateKindRelService;
import com.aeotrade.chain.contract.service.IContractTemplateKindService;
import com.aeotrade.chain.contract.service.IContractTemplateOrgAliasService;
import com.aeotrade.chain.contract.service.IContractTemplateService;
import com.aeotrade.chain.contract.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.aeotrade.chain.contract.controller.ContractTemplateController.API_PREFIX;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author chl
 * @since 2022-10-21
 */
@RestController
@RequestMapping(ContractTemplateController.API_PREFIX)
public class ContractTemplateController {
    public static final String API_PREFIX="/contracttpl";
    @Autowired
    private IContractTemplateService iContractTemplateService;
    @Autowired
    private IContractTemplateOrgAliasService iContractTemplateOrgAliasService;
    @Autowired
    private ContractTemplateOrgAliasConvert contractTemplateOrgAliasConvert;
    @Autowired
    private ContractTemplateConvert contractTemplateConvert;
    @Autowired
    private IContractTemplateKindService iContractTemplateKindService;
    @Autowired
    private IContractTemplateKindRelService iContractTemplateKindRelService;


    @GetMapping()
    public ResponseEntity<ResultBean<IPage<ContractTemplateListVo>>> indexList(@RequestParam(name = "tplname", required = false) String tplName,
                                                                               @RequestParam(name = "ccode", required = false) List<String> categoryCode,
                                                                               @RequestParam(name = "currentpage") int currentPage,
                                                                               @RequestParam(name = "pagesize") int pageSize) {
        ResultBean<IPage<ContractTemplateListVo>> resultBean = new ResultBean<>();
        IPage<ContractTemplateListVo> contractTemplateListVOIPage = new Page<>();
        List<ContractTemplateListVo> contractTemplateListVos = new ArrayList<>();

        List<Long> tplIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(categoryCode)) {
            ContractTemplateKind contractTemplateKind = new ContractTemplateKind();
            QueryWrapper<ContractTemplateKind> templateKindQueryWrapper = new QueryWrapper<>(contractTemplateKind);
            templateKindQueryWrapper.in(ContractTemplateKind.CATEGORY_CODE,categoryCode);
            templateKindQueryWrapper.select(ContractTemplateKind.KIND_ID);
            List<ContractTemplateKind> contractTemplateKinds = iContractTemplateKindService.list(templateKindQueryWrapper);
            if(CollectionUtils.isEmpty(contractTemplateKinds)){
                resultBean.setStatusEnum(StatusEnum.SUCCESS_NO_DATA);
                return ResponseEntity.ok().body(resultBean);
            }
            List<String> kindIdStrings = contractTemplateKinds.stream()
                    .map(ContractTemplateKind::getKindId)
                    .map(Object::toString)
                    .collect(Collectors.toList());
            ContractTemplateKindRel contractTemplateKindRel = new ContractTemplateKindRel();
            QueryWrapper<ContractTemplateKindRel> templateKindRelQueryWrapper = new QueryWrapper<>(contractTemplateKindRel);
            templateKindRelQueryWrapper.in(ContractTemplateKindRel.KIND_ID,kindIdStrings);
            templateKindRelQueryWrapper.select(ContractTemplateKindRel.CONTRACT_TEMPLATE_ID);
            List<ContractTemplateKindRel> list = iContractTemplateKindRelService.list(templateKindRelQueryWrapper);
            if(CollectionUtils.isEmpty(list)){
                resultBean.setStatusEnum(StatusEnum.SUCCESS_NO_DATA);
                return ResponseEntity.ok().body(resultBean);
            }
            tplIds = list.stream().map(ContractTemplateKindRel::getContractTemplateId).collect(Collectors.toList());
        }

        QueryWrapper<ContractTemplate> queryWrapper = new QueryWrapper<>();
        if(!CollectionUtils.isEmpty(tplIds)){
            queryWrapper.in(ContractTemplate.CONTRACT_TEMPLATE_ID,tplIds);
        }
        Page<ContractTemplate> page = new Page<>(currentPage, pageSize);
        if (StringUtils.isNotBlank(tplName)) {
            queryWrapper.like(ContractTemplate.NAME, tplName);
        }
        queryWrapper.orderByDesc(ContractTemplate.RECOMMEND, ContractTemplate.WRITE_TIME);
        Page<ContractTemplate> indexPage = this.iContractTemplateService.page(page, queryWrapper);

        List<ContractTemplate> records = indexPage.getRecords();
        for (ContractTemplate contractTemplate : records) {
            ContractTemplateListVo contractTemplateListVO = this.contractTemplateConvert.dtoToListVo(contractTemplate);
            contractTemplateListVos.add(contractTemplateListVO);
        }
        contractTemplateListVOIPage.setRecords(contractTemplateListVos);
        contractTemplateListVOIPage.setSize(records.size());
        contractTemplateListVOIPage.setCurrent(indexPage.getCurrent());
        contractTemplateListVOIPage.setPages(indexPage.getPages());
        contractTemplateListVOIPage.setTotal(indexPage.getTotal());
        resultBean.setData(contractTemplateListVOIPage);
        if (records.isEmpty()) {
            resultBean.setStatusEnum(StatusEnum.SUCCESS_NO_DATA);
        } else {
            resultBean.setStatusEnum(StatusEnum.SUCCESS);
        }
        return ResponseEntity.ok(resultBean);
    }

    @GetMapping("{id}")
    public ResponseEntity<ResultBean<ContractTemplateVo>> getContractTemplate(@PathVariable @NotNull Long id) {

        ResultBean<ContractTemplateVo> resultBean = new ResultBean<>();
        ContractTemplate byId = iContractTemplateService.getById(id);
        if (null == byId) {
            resultBean.setStatusEnum(StatusEnum.SUCCESS_NO_DATA);
            return ResponseEntity.ok(resultBean);
        }
        ContractTemplateVo contractTemplateVO = this.contractTemplateConvert.dtoToVo(byId);
        List<ContractTemplateOrgAliasVo> orgAliasVOS = new ArrayList<>();
        ContractTemplateOrgAlias contractTemplateOrgAlias = new ContractTemplateOrgAlias();
        contractTemplateOrgAlias.setContractTemplateId(byId.getContractTemplateId());
        QueryWrapper<ContractTemplateOrgAlias> queryWrapper = new QueryWrapper<>(contractTemplateOrgAlias);
        List<ContractTemplateOrgAlias> list = iContractTemplateOrgAliasService.list(queryWrapper);
        for (ContractTemplateOrgAlias templateOrgAlias : list) {
            ContractTemplateOrgAliasVo contractTemplateOrgAliasVO = contractTemplateOrgAliasConvert.dtoToVo(templateOrgAlias);
            orgAliasVOS.add(contractTemplateOrgAliasVO);
        }
        contractTemplateVO.setOrgAlias(orgAliasVOS);
        resultBean.setData(contractTemplateVO);
        resultBean.setStatusEnum(StatusEnum.SUCCESS);
        return ResponseEntity.ok(resultBean);
    }
}
