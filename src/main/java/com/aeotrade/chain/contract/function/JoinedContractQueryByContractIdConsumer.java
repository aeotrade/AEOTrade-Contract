package com.aeotrade.chain.contract.function;

import com.aeotrade.chain.contract.constants.ContractConstants;
import com.aeotrade.chain.contract.po.Contract;
import com.aeotrade.chain.contract.po.ContractOrgAlias;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 企业参与的合约查询条件构造器
 *
 * @author chenhailong
 * @date 2023/01/12
 */
@AllArgsConstructor
public class JoinedContractQueryByContractIdConsumer implements Consumer<QueryWrapper<Contract>> {
    private List<ContractOrgAlias> contractOrgAliasList;
    @Override
    public void accept(QueryWrapper<Contract> contractQueryWrapper) {
        List<Long> ids = new ArrayList<>();
        for (ContractOrgAlias orgAlias : contractOrgAliasList) {
            ids.add(orgAlias.getContractId());
        }
        ids=ids.stream().distinct().collect(Collectors.toList());
        contractQueryWrapper.in(Contract.CONTRACT_ID, ids).in(Contract.RELEASE_STATUS, ContractConstants.RELEASE_STATUS_PART.getCode(),ContractConstants.RELEASE_STATUS_PUBLISHED.getCode());
    }
}
