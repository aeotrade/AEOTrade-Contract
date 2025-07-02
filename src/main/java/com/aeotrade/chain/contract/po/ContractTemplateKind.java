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
 * @since 2023-07-14
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("contract_template_kind")
public class ContractTemplateKind implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "kind_id", type = IdType.ASSIGN_ID)
    private Long kindId;

    @TableField("sort")
    private Integer sort;

    @TableField("category_name")
    private String categoryName;

    @TableField("category_code")
    private String categoryCode;

    @TableField("note")
    private String note;

    @TableField("create_date")
    private Date createDate;

    @TableField("create_uid")
    private String createUid;

    @TableField("write_time")
    @Version
    private Date writeTime;

    @TableField("write_uid")
    private String writeUid;

    @TableField("description")
    private String description;

    public static final String KIND_ID = "kind_id";

    public static final String SORT = "sort";

    public static final String CATEGORY_NAME = "category_name";

    public static final String CATEGORY_CODE = "category_code";

    public static final String NOTE = "note";

    public static final String CREATE_DATE = "create_date";

    public static final String CREATE_UID = "create_uid";

    public static final String WRITE_TIME = "write_time";

    public static final String WRITE_UID = "write_uid";

    public static final String DESCRIPTION = "description";
}
