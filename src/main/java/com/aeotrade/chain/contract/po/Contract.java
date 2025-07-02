package com.aeotrade.chain.contract.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author chl
 * @since 2023-07-17
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("contract")
public class Contract implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "contract_id", type = IdType.ASSIGN_ID)
    private Long contractId;

    @TableField("customer_contract_id")
    private String customerContractId;

    @TableField("organization_id")
    private String organizationId;

    @TableField("icon")
    private String icon;

    @TableField("contract_template_id")
    private Long contractTemplateId;

    @TableField("name")
    private String name;

    @TableField("decription")
    private String decription;

    @TableField("type")
    private String type;

    @TableField("process_defintion")
    private String processDefintion;

    @TableField("process_definition_key")
    private String processDefinitionKey;

    @TableField("process_definition_id")
    private String processDefinitionId;

    @TableField("version")
    private String version;

    @TableField("create_date")
    private Date createDate;

    @TableField("create_uid")
    private String createUid;

    @TableField("write_time")
    @Version
    private Date writeTime;

    @TableField("write_uid")
    private String writeUid;

    @TableField("release_status")
    private String releaseStatus;

    @TableField("execute_status")
    private String executeStatus;

    @TableField("remaining_num")
    private Integer remainingNum;

    @TableField("done_qty")
    private Integer doneQty;

    @TableField("target_qty")
    private Integer targetQty;

    @TableField("task_error_msg")
    private String taskErrorMsg;

    public static final String CONTRACT_ID = "contract_id";

    public static final String CUSTOMER_CONTRACT_ID = "customer_contract_id";

    public static final String ORGANIZATION_ID = "organization_id";

    public static final String ICON = "icon";

    public static final String CONTRACT_TEMPLATE_ID = "contract_template_id";

    public static final String NAME = "name";

    public static final String DECRIPTION = "decription";

    public static final String TYPE = "type";

    public static final String PROCESS_DEFINTION = "process_defintion";

    public static final String PROCESS_DEFINITION_KEY = "process_definition_key";

    public static final String PROCESS_DEFINITION_ID = "process_definition_id";

    public static final String VERSION = "version";

    public static final String CREATE_DATE = "create_date";

    public static final String CREATE_UID = "create_uid";

    public static final String WRITE_TIME = "write_time";

    public static final String WRITE_UID = "write_uid";

    public static final String RELEASE_STATUS = "release_status";

    public static final String EXECUTE_STATUS = "execute_status";

    public static final String REMAINING_NUM = "remaining_num";

    public static final String DONE_QTY = "done_qty";

    public static final String TARGET_QTY = "target_qty";

    public static final String TASK_ERROR_MSG = "task_error_msg";
}
