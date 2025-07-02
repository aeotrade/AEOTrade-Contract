package com.aeotrade.chain.contract.mapstruct;

import com.aeotrade.chain.contract.po.ContractOrgAlias;
import com.aeotrade.chain.contract.vo.OrgAliasVo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContractOrgAliasConvert {


    OrgAliasVo dto2vo(ContractOrgAlias contractOrgAlias);
}
