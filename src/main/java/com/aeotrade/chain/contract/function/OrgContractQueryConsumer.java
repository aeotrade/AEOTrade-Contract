package com.aeotrade.chain.contract.function;

import com.aeotrade.chain.contract.po.Contract;
import com.aeotrade.chain.contract.po.ContractOrgAlias;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.Consumer;

/**
 * 企业发起的和参与的所有合约查询条件构造器
 *
 * @author chenhailong
 * @date 2023/01/12
 */
@AllArgsConstructor
public class OrgContractQueryConsumer implements Consumer<QueryWrapper<Contract>> {
    private String entId;
    private List<ContractOrgAlias> contractOrgAliasList;
    @Override
    public void accept(QueryWrapper<Contract> contractQueryWrapper) {
        contractQueryWrapper=contractQueryWrapper.eq(Contract.ORGANIZATION_ID, entId);
        if(!CollectionUtils.isEmpty(contractOrgAliasList)){
            contractQueryWrapper.or(new JoinedContractQueryByContractIdConsumer(contractOrgAliasList));
        }
    }
}
