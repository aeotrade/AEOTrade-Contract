package com.aeotrade.chain.contract.mapstruct;

import com.aeotrade.chain.contract.po.Contract;
import com.aeotrade.chain.contract.vo.ContractListVO;
import com.aeotrade.chain.contract.vo.ContractVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContractConvert {

    @Mapping(target = "contractTplId", source = "contractTemplateId")
    @Mapping(target = "memberId", source = "organizationId")
    @Mapping(target = "processDefine",source = "processDefintion")
    @Mapping(target = "versionNo",source = "version")
    @Mapping(target ="executedNum", ignore = true)
    @Mapping(target ="orgAlias", ignore = true)
    ContractVo dtoToVo(Contract contract);


    @Mapping(target ="orgAlias", ignore = true)
    @Mapping(target = "executedNum",source = "doneQty")
    @Mapping(target ="executeStatusMessage", ignore = true)
    ContractListVO dtoToListVo(Contract contract);

}
