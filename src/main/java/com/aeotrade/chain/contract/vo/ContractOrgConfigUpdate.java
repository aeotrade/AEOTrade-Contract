package com.aeotrade.chain.contract.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ContractOrgConfigUpdate {
    @Size(max=18,message = "合约id{javax.validation.constraints.Size.message}")
    @NotBlank(message = "合约id不能为空！")
    private String contractId;
    @NotNull(message = "是否隐藏不能为空！")
    private Boolean hidden;
}
