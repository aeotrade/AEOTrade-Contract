package com.aeotrade.chain.contract.function;

import com.aeotrade.chain.contract.po.Contract;
import com.aeotrade.chain.contract.po.ContractTemplateKindRel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.function.Consumer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 模板id的合约查询条件构造器
 *
 * @author lzj
 * @date 2023/05/26
 */
@AllArgsConstructor
public class JoinedContractQueryByContractTplIdConsumer implements Consumer<QueryWrapper<Contract>> {
    private List<ContractTemplateKindRel> list;

    @Override
    public void accept(QueryWrapper<Contract> contractQueryWrapper) {
        List<Long> ids = new ArrayList<>();
        for (ContractTemplateKindRel contractTemplateKindRel : list) {
            ids.add(contractTemplateKindRel.getContractTemplateId());
        }
        ids=ids.stream().distinct().collect(Collectors.toList());
        contractQueryWrapper.in(Contract.CONTRACT_TEMPLATE_ID,ids);
    }
}
