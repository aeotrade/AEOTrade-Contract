package com.aeotrade.chain.contract.connectormanage;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("aeotrade-con-mng-sys")
public interface ConnectorManageClient {
    String ORG_CONNECOTR_TASK_API_PREFIX="/api/org-connectors-tasks";
    @PostMapping(ORG_CONNECOTR_TASK_API_PREFIX)
    ConnectorTaskResult createOrgConnectorTask(ConnectorTask connectorTask, @RequestHeader("memberId") String memberId);
    @DeleteMapping(ORG_CONNECOTR_TASK_API_PREFIX+ "/{orgConnectorTaskId}")
    void deleteOrgConnectorTask(@PathVariable String orgConnectorTaskId, @RequestHeader("memberId")String memberId);
    @PutMapping(ORG_CONNECOTR_TASK_API_PREFIX+ "/{orgConnectorTaskId}")
    ConnectorTaskResult updateOrgConnectorTask(@PathVariable String orgConnectorTaskId, ConnectorTask connectorTask, @RequestHeader("memberId") String memberId);
    @GetMapping(ORG_CONNECOTR_TASK_API_PREFIX+ "/{orgConnectorTaskId}")
    ConnectorTaskResult getOrgConnectorTask(@PathVariable String orgConnectorTaskId, @RequestHeader("memberId")String memberId);
    @PutMapping(ORG_CONNECOTR_TASK_API_PREFIX+ "/{orgConnectorTaskId}/start")
    ConnectorTaskResult startOrgConnectorTask(@PathVariable String orgConnectorTaskId, @RequestHeader("memberId")String memberId);
    @PutMapping(ORG_CONNECOTR_TASK_API_PREFIX+ "/{orgConnectorTaskId}/stop")
    ConnectorTaskResult stopOrgConnectorTask(@PathVariable String orgConnectorTaskId, @RequestHeader("memberId")String memberId);

    String ORG_CONNECTOR_API_PREFIX="/api/org-connectors";
    @GetMapping(ORG_CONNECTOR_API_PREFIX)
    OraConnectorListResult getOrgConnectors(@RequestParam("connector_id") String connectorId, @RequestHeader("memberId") String memberId);
    @GetMapping(ORG_CONNECTOR_API_PREFIX+"/{org_connector_id}")
    OrgConnectorResult getOrgConnector(@PathVariable("org_connector_id") String orgConnectorId, @RequestHeader("memberId") String memberId);

}
