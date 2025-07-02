package com.aeotrade.chain.contract.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author chl
 * @since 2023-06-26
 */
@Getter
@Setter
@TableName("contract_org_config")
public class ContractOrgConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "contract_org_config_seq", type = IdType.ASSIGN_ID)
    private Long contractOrgConfigSeq;

    @TableField("contract_id")
    private Long contractId;

    @TableField("collaboration_org_id")
    private String collaborationOrgId;

    @TableField("hidden")
    private Boolean hidden;

    @TableField("hidden_op_time")
    private Date hiddenOpTime;

    public static final String CONTRACT_ORG_CONFIG_SEQ = "contract_org_config_seq";

    public static final String CONTRACT_ID = "contract_id";

    public static final String COLLABORATION_ORG_ID = "collaboration_org_id";

    public static final String HIDDEN = "hidden";

    public static final String HIDDEN_OP_TIME = "hidden_op_time";
}
