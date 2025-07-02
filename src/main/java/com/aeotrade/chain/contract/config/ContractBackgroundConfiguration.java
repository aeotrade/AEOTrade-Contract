package com.aeotrade.chain.contract.config;

import com.aeotrade.chain.contract.dealer.DxpMsgTransferComponet;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import static com.aeotrade.chain.contract.constants.ContractVariableConstants.DXP_SEND_COMPONET_BEAN_NAME;

@Configuration
public class ContractBackgroundConfiguration {
    @Value("${contract.exchange.id}")
    private String contractDxpid;


    @Bean(DXP_SEND_COMPONET_BEAN_NAME)
    public DxpMsgTransferComponet dxpMsgTransferComponet(@Autowired RabbitTemplate rabbitTemplate,@Autowired Jaxb2Marshaller jaxb2Marshaller) throws DatatypeConfigurationException {
        return new DxpMsgTransferComponet(this.contractDxpid,rabbitTemplate,jaxb2Marshaller, DatatypeFactory.newInstance());
    }

}
