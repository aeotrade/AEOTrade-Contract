package com.aeotrade.chain.contract.function;

import com.aeotrade.chain.contract.po.Contract;
import com.aeotrade.chain.contract.po.ContractOrgConfig;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@AllArgsConstructor
public class JoinedContractQueryByHiddenConsumer implements Consumer<QueryWrapper<Contract>> {

    private List<ContractOrgConfig> list;

    @Override
    public void accept(QueryWrapper<Contract> contractQueryWrapper) {
        List<Long> ids = new ArrayList<>();
        for (ContractOrgConfig contractOrgConfig : list) {
            ids.add(contractOrgConfig.getContractId());
        }
        ids=ids.stream().distinct().collect(Collectors.toList());
        contractQueryWrapper.in(Contract.CONTRACT_ID,ids);
    }
}
