package com.aeotrade.chain.contract.vo;

import lombok.Data;

import java.util.List;

@Data
public class ContractProcessListVo {
    private String processDefine;
    private List<ContractProcessVo> contractProcessVos;
}
