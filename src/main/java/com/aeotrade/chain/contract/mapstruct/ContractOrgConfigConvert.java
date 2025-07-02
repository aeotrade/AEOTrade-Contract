package com.aeotrade.chain.contract.mapstruct;

import com.aeotrade.chain.contract.po.ContractOrgConfig;
import com.aeotrade.chain.contract.vo.ContractOrgConfigVo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContractOrgConfigConvert {

    ContractOrgConfigVo dtoToVo(ContractOrgConfig contractOrgConfig);

}
