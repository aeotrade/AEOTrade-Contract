package com.aeotrade.chain.contract.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author chl
 * @since 2023-01-10
 */
@Getter
@Setter
@TableName("contract_org_alias")
public class ContractOrgAlias implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "alias_seq", type = IdType.ASSIGN_ID)
    private Long aliasSeq;

    @TableField("alias_id")
    private Long aliasId;

    @TableField("contract_id")
    private Long contractId;

    @TableField("alias_name")
    private String aliasName;

    @TableField("collaboration_org_id")
    private String collaborationOrgId;

    public static final String ALIAS_SEQ = "alias_seq";

    public static final String ALIAS_ID = "alias_id";

    public static final String CONTRACT_ID = "contract_id";

    public static final String ALIAS_NAME = "alias_name";

    public static final String COLLABORATION_ORG_ID = "collaboration_org_id";
}
