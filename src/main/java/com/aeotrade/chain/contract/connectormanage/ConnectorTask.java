package com.aeotrade.chain.contract.connectormanage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ConnectorTask {
    @JsonProperty("task_id")
    private String connectorTaskId;
    @JsonProperty("transfer_identity_id")
    private String dxpId;
    @JsonProperty("org_connector_id")
    private String orgConnectorId;
    @JsonProperty("activity_code")
    private String activityCode;
    @JsonProperty("contract_id")
    private String contractId;
    @JsonProperty("event_action_id")
    private String eventActionId;
    @JsonProperty("event_action_params")
    private Object eventActionParams;
    private String status;
    @JsonProperty("status_message")
    private String statusMessage;
    @JsonProperty("model_name")
    private String modelName;
    @JsonProperty("target_qty")
    private Integer targetQty;

}
