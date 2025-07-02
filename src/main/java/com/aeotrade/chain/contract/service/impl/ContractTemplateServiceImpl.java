package com.aeotrade.chain.contract.service.impl;

import com.aeotrade.chain.contract.service.IContractTemplateService;
import com.aeotrade.chain.contract.dao.mybatis.ContractTemplateMapper;
import com.aeotrade.chain.contract.po.ContractTemplate;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author chl
 * @since 2022-10-21
 */
@Service
public class ContractTemplateServiceImpl extends ServiceImpl<ContractTemplateMapper, ContractTemplate> implements IContractTemplateService {

}
