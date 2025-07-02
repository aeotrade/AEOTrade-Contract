package com.aeotrade.chain.contract.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ContractProcessVo {
    private String activityId;
    private String activityName;
    private Date activityTriggerTime;
    private String masterDataModelName;
    private String activityType;
    private boolean onTable;
}
