package com.aeotrade.chain.contract.service;

import com.aeotrade.chain.contract.po.ContractTemplateKindRel;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author chl
 * @since 2023-02-20
 */
public interface IContractTemplateKindRelService extends IService<ContractTemplateKindRel> {

    List<ContractTemplateKindRel> queryContractTemplateKindRelByCategoryCode(List<String> categoryCode);
}
