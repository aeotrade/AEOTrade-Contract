package com.aeotrade.chain.contract.vo;
import lombok.Data;

import java.util.List;


@Data
public class ContractTemplateVo {
    private Long contractTplId;
    private String icon;
    private String name;
    private String decription;
    /**
     * 1-普通引擎	2-流程引擎	3-智能合约引擎	4-联邦学习引擎
     */
    private String type;
    private String processDefine;
    private String versionNo;
    private String recommend;
    private String solutiondescription;
    List<ContractTemplateOrgAliasVo> orgAlias;
}
