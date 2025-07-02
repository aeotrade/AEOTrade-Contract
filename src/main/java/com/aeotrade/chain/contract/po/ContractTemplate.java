package com.aeotrade.chain.contract.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;
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
@TableName("contract_template")
public class ContractTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "contract_template_id", type = IdType.ASSIGN_ID)
    private Long contractTemplateId;

    @TableField("custom_contract_template_id")
    private String customContractTemplateId;

    @TableField("icon")
    private String icon;

    @TableField("name")
    private String name;

    @TableField("decription")
    private String decription;

    /**
     * 1-普通引擎	2-流程引擎	3-智能合约引擎	4-联邦学习引擎
     */
    @TableField("type")
    private String type;

    @TableField("process_defintion")
    private String processDefintion;

    @TableField("version")
    private String version;

    /**
     * 0-否	1-是
     */
    @TableField("recommend")
    private String recommend;

    @TableField("solution_description")
    private String solutionDescription;

    @TableField("create_date")
    private Date createDate;

    @TableField("create_uid")
    private String createUid;

    @TableField("write_time")
    @Version
    private Timestamp writeTime;

    @TableField("write_uid")
    private String writeUid;

    public static final String CONTRACT_TEMPLATE_ID = "contract_template_id";

    public static final String CUSTOM_CONTRACT_TEMPLATE_ID = "custom_contract_template_id";

    public static final String ICON = "icon";

    public static final String NAME = "name";

    public static final String DECRIPTION = "decription";

    public static final String TYPE = "type";

    public static final String PROCESS_DEFINTION = "process_defintion";

    public static final String VERSION = "version";

    public static final String RECOMMEND = "recommend";

    public static final String SOLUTION_DESCRIPTION = "solution_description";

    public static final String CREATE_DATE = "create_date";

    public static final String CREATE_UID = "create_uid";

    public static final String WRITE_TIME = "write_time";

    public static final String WRITE_UID = "write_uid";
}
