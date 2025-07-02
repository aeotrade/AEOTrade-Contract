package com.aeotrade.chain.contract.service.impl;

import com.aeotrade.chain.contract.dao.mybatis.ContractTemplateKindMapper;
import com.aeotrade.chain.contract.dao.mybatis.ContractTemplateKindRelMapper;
import com.aeotrade.chain.contract.po.ContractTemplateKind;
import com.aeotrade.chain.contract.po.ContractTemplateKindRel;
import com.aeotrade.chain.contract.exception.ApplicationException;
import com.aeotrade.chain.contract.service.IContractTemplateKindRelService;
import com.aeotrade.chain.contract.vo.StatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author chl
 * @since 2023-02-20
 */
@Service
public class ContractTemplateKindRelServiceImpl extends ServiceImpl<ContractTemplateKindRelMapper, ContractTemplateKindRel> implements IContractTemplateKindRelService {
    @Resource
    private ContractTemplateKindMapper contractTemplateKindMapper;
    @Override
    public List<ContractTemplateKindRel> queryContractTemplateKindRelByCategoryCode(List<String> categoryCode) {
        QueryWrapper<ContractTemplateKind> templateKindQueryWrapper = new QueryWrapper<>();
        templateKindQueryWrapper.in(ContractTemplateKind.CATEGORY_CODE,categoryCode);
        List<ContractTemplateKind> contractTemplateKinds = this.contractTemplateKindMapper.selectList(templateKindQueryWrapper);
        if(CollectionUtils.isEmpty(contractTemplateKinds)){
            throw new ApplicationException(HttpStatus.OK, StatusEnum.SUCCESS_NO_DATA);
        }
        List<String> ids = new ArrayList<>();
        for (ContractTemplateKind contractTemplateKind : contractTemplateKinds) {
            ids.add(String.valueOf(contractTemplateKind.getKindId()));
        }
        ids=ids.stream().distinct().collect(Collectors.toList());
        QueryWrapper<ContractTemplateKindRel> kindRelQueryWrapper = new QueryWrapper<>();
        kindRelQueryWrapper.in(ContractTemplateKindRel.KIND_ID,ids);
        List<ContractTemplateKindRel> list = this.baseMapper.selectList(kindRelQueryWrapper);
        if(CollectionUtils.isEmpty(list)){
            throw new ApplicationException(HttpStatus.OK,StatusEnum.SUCCESS_NO_DATA);
        }
        return list;
    }
}
