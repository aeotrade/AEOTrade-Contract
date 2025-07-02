package com.aeotrade.chain.contract.service.impl;

import com.aeotrade.chain.contract.dao.mybatis.ContractMapper;
import com.aeotrade.chain.contract.po.Contract;
import com.aeotrade.chain.contract.service.IContractService;
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
public class ContractServiceImpl extends ServiceImpl<ContractMapper, Contract> implements IContractService {

}
