package com.aeotrade.chain.contract.blockchain;

import com.aeotrade.chain.contract.connectormanage.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient("aeotrade-server-chain")
public interface SmartContractClient {
    String API_PREFIX="/contract";

    @PostMapping(value=API_PREFIX+"/vote/create",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    CommonResult<String> vote(Smartcontract smartcontract, @RequestHeader("memberId") String memberId);
}
