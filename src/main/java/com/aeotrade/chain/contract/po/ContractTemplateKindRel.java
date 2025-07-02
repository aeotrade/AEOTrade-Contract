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
 * @since 2023-02-20
 */
@Getter
@Setter
@TableName("contract_template_kind_rel")
public class ContractTemplateKindRel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "rel_seq", type = IdType.ASSIGN_ID)
    private Long relSeq;

    @TableField("contract_template_id")
    private Long contractTemplateId;

    @TableField("kind_id")
    private String kindId;

    @TableField("create_date")
    private Date createDate;

    @TableField("create_uid")
    private String createUid;

    public static final String REL_SEQ = "rel_seq";

    public static final String CONTRACT_TEMPLATE_ID = "contract_template_id";

    public static final String KIND_ID = "kind_id";

    public static final String CREATE_DATE = "create_date";

    public static final String CREATE_UID = "create_uid";
}
