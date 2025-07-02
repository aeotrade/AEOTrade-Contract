package com.aeotrade.chain.contract.vo;


import lombok.Data;

import java.util.Date;

@Data
public class ContractTemplateListVo {

    private Long contractTplId;
    private String icon;
    private String name;
    private String decription;
    private String versionNo;
    private Date createDate;
    private Date writeTime;
    private String recommend;
}
