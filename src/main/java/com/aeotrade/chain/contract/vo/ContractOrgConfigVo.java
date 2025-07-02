package com.aeotrade.chain.contract.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ContractOrgConfigVo {
    private Long contractId;
    private String collaborationOrgId;
    private Boolean hidden;
    private Date hiddenOpTime;

}
