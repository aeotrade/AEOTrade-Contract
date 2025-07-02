package com.aeotrade.chain.contract.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContractVariableConstants {
    public static final String RECEIVERS_VARIABLE_NAME="receivers";
    public static final String SENDER_VARIABLE_NAME="sender";
    public static final String TRANSFER_DATA_VARIABLE_NAME="transdata";
    public static final String BIZ_DATA_VARIABLE_NAME="bizdata";
    public static final String BIZ_DATA_CHARSET_VARIABLE_NAME="bizdata_charset";
    public static final String RECEIVER_MAP_VARIABLE_NAME="recemap";
    public static final String MESSAGE_TYPE_VARIABLE_NAME="msgtype";

    public static final String DXP_SEND_COMPONET_BEAN_NAME="dxpTransferComponet";

    public static final String MEMBER_CONNECTOR_ID_NAME ="member_connector_id";
    public static final String MEMBER_CONNECTOR_TASK_ID_NAME ="member_connector_task_id";
    public static final String MEMBER_ID_NAME ="member_id";
    public static final String CONNECTOR_ID_NAME ="connector_id";
    public static final String EVENT_ACTION_ID_NAME="event_action_id";
    public static final String ALIAS_ID_NAME="alias_id";
    public static final String CONNECTOR_PARA_NAME="connector_task_para";
    public static final String MAIN_ACTIVITY_NAME="main_activity";
    public static final String CONNECTOR_STATUS_RUNNING ="running";
    public static final String CONNECTOR_STATUS_STOPPED ="stopped";
    public static final String CONNECTOR_STATUS_ERROR="error";
    public static final String CONNECTOR_STATUS_WARNING ="warning";

}
