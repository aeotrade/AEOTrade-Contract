package com.aeotrade.chain.contract.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;


@Data
public class ContractSave {


    @Size(max=18,message = "模板id{javax.validation.constraints.Size.message}")
    private String contractTplId;

    @NotBlank(message = "合约名称不能为空！")
    @Size(max=100,message = "合约名称{javax.validation.constraints.Size.message}")
    private String name;
    /**
     * 1-普通引擎	2-流程引擎	3-智能合约引擎	4-联邦学习引擎
     */
    @Pattern(regexp = "[1234]",message = "合约执行引擎类型不合法")
    private String type;

    private String processDefine;
    @Size(max=20,message = "版本号{javax.validation.constraints.Size.message}")
    private String versionNo;

    /**
     * 发布状态
     */
    @Pattern(regexp = "[12]",message = "发布状态不合法")
    @NotBlank(message = "发布状态不能为空！")
    private String releaseStatus;

    private List<OrgAliasVo> orgAlias;

    private String decription;

}
