package com.aeotrade.chain.contract.util;

import com.aeotrade.chain.contract.constants.ContractConstants;
import com.aeotrade.chain.contract.po.Contract;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ContractUtil {

    /**
     * 是合同可刷新，判断合同是否需要刷新状态和统计数据
     *
     * @param contract 合同
     * @return boolean
     */
    public boolean isContractRefreshable(Contract contract){
        return ContractConstants.EXECUTE_STATUS_DOING.getCode().equalsIgnoreCase(contract.getExecuteStatus())
                || ContractConstants.EXECUTE_STATUS_EXCEPTION.getCode().equalsIgnoreCase(contract.getExecuteStatus())
                || ContractConstants.EXECUTE_STATUS_DOING_WARING.getCode().equalsIgnoreCase(contract.getExecuteStatus());
    }
}
