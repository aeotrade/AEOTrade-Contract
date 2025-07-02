package com.aeotrade.chain.contract.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class ContractUpDate {


    @Size(max=18,message = "合约id{javax.validation.constraints.Size.message}")
    @NotBlank(message = "合约id不能为空！")
    private String contractId;

    @NotBlank(message = "合约名称不能为空！")
    @Size(max=100,message = "合约名称{javax.validation.constraints.Size.message}")
    private String name;


    @Size(max=1024,message = "合约描述{javax.validation.constraints.Size.message}")
    private String decription;
    /**
     * 发布状态
     */
    @Pattern(regexp = "[123]",message = "发布状态不合法")
    @NotBlank(message = "发布状态不能为空！")
    private String releaseStatus;
    private String processDefine;

    private List<OrgAliasVo> orgAlias;
}
