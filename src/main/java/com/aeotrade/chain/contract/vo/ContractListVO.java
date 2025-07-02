package com.aeotrade.chain.contract.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ContractListVO {
    private Long contractId;
    private String name;
    private Integer executedNum;
    private Integer remainingNum;
    private Date createDate;
    private String releaseStatus;
    private String executeStatus;
    private List<String> executeStatusMessage;
    private String organizationId;

    private List<OrgAliasVo> orgAlias;
}
