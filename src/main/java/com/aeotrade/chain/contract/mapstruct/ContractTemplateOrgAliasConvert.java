package com.aeotrade.chain.contract.mapstruct;

import com.aeotrade.chain.contract.po.ContractTemplateOrgAlias;
import com.aeotrade.chain.contract.vo.ContractTemplateOrgAliasVo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContractTemplateOrgAliasConvert {


    ContractTemplateOrgAliasVo dtoToVo(ContractTemplateOrgAlias contract);
}
