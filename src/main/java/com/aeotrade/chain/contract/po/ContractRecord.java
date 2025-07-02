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
 * @since 2023-01-10
 */
@Getter
@Setter
@TableName("contract_record")
public class ContractRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "contract_record_id", type = IdType.ASSIGN_ID)
    private Long contractRecordId;

    @TableField("contract_id")
    private Long contractId;

    @TableField("name")
    private String name;

    @TableField("organization_id")
    private String organizationId;

    @TableField("org_contract_record_no")
    private String orgContractRecordNo;

    @TableField("process_instance_id")
    private String processInstanceId;

    @TableField("process_definition_id")
    private String processDefinitionId;

    @TableField("create_date")
    private Date createDate;

    public static final String CONTRACT_RECORD_ID = "contract_record_id";

    public static final String CONTRACT_ID = "contract_id";

    public static final String NAME = "name";

    public static final String ORGANIZATION_ID = "organization_id";

    public static final String ORG_CONTRACT_RECORD_NO = "org_contract_record_no";

    public static final String PROCESS_INSTANCE_ID = "process_instance_id";

    public static final String PROCESS_DEFINITION_ID = "process_definition_id";

    public static final String CREATE_DATE = "create_date";
}
