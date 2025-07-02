package com.aeotrade.chain.contract.mapstruct;

import com.aeotrade.chain.contract.po.ContractTemplate;
import com.aeotrade.chain.contract.vo.ContractTemplateListVo;
import com.aeotrade.chain.contract.vo.ContractTemplateVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContractTemplateConvert {

    @Mapping(target = "contractTplId", source = "contractTemplateId")
    @Mapping(target = "versionNo", source = "version")
    ContractTemplateListVo dtoToListVo(ContractTemplate contractTemplate);
    @Mapping(target = "contractTplId", source = "contractTemplateId")
    @Mapping(target = "versionNo", source = "version")
    @Mapping(target = "processDefine", source = "processDefintion")
    @Mapping(target = "solutiondescription", source = "solutionDescription")
    @Mapping(target ="orgAlias", ignore = true)
    ContractTemplateVo dtoToVo(ContractTemplate contractTemplate);
}
