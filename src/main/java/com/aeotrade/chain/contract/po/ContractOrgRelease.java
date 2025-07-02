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
@TableName("contract_org_release")
public class ContractOrgRelease implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "contract_org_release_seq", type = IdType.ASSIGN_ID)
    private Long contractOrgReleaseSeq;

    @TableField("contract_id")
    private Long contractId;

    @TableField("collaboration_org_id")
    private String collaborationOrgId;

    @TableField("release_status")
    private String releaseStatus;

    @TableField("release_time")
    private Date releaseTime;

    public static final String CONTRACT_ORG_RELEASE_SEQ = "contract_org_release_seq";

    public static final String CONTRACT_ID = "contract_id";

    public static final String COLLABORATION_ORG_ID = "collaboration_org_id";

    public static final String RELEASE_STATUS = "release_status";

    public static final String RELEASE_TIME = "release_time";
}
