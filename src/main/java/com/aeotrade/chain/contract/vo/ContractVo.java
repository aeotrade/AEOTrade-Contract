package com.aeotrade.chain.contract.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ContractVo {

    private Long contractId;
    private String memberId;
    private String icon;
    private Long contractTplId;
    private String name;
    private String decription;
    /**
     * 1-普通引擎	2-流程引擎	3-智能合约引擎	4-联邦学习引擎
     */
    private String type;
    private String processDefine;
    private String versionNo;
    private Date createDate;
    private Date writeTime;
    private String releaseStatus;
    private String executeStatus;
    private Integer executedNum;
    private Integer remainingNum;
    private String organizationId;

    private List<OrgAliasVo> orgAlias;
}
