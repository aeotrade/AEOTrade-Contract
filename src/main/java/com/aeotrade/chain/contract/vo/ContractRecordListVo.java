package com.aeotrade.chain.contract.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ContractRecordListVo {

    private String contractRecordId;
    private String contractId;
    private String contractName;
    private String activity;
    private Date activityTime;
    private Date createTime;
    private String entContractRecordNo;

}
