package com.aeotrade.chain.contract.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ContractOrgReleaseVo {

    private String collaborationOrgId;
    private String releaseStatus;
    private Date releaseTime;
}
