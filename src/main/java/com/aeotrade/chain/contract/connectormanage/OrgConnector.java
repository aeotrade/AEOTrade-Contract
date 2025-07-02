package com.aeotrade.chain.contract.connectormanage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrgConnector {
    @JsonProperty("org_connector_id")
    private String orgConnectorId;
    @JsonProperty("connector_name")
    private String connectorName;

}
