package com.aeotrade.chain.contract.config;

import org.springframework.amqp.support.converter.MarshallingMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class JaxbConfig {
    @Bean
    public MarshallingMessageConverter marshallingMessageConverter(){
        return new MarshallingMessageConverter(jaxb2Marshaller());
    }

    @Bean
    public Jaxb2Marshaller jaxb2Marshaller(){
        Jaxb2Marshaller jaxb2Marshaller=new Jaxb2Marshaller();
        jaxb2Marshaller.setPackagesToScan(com.aeotrade.chain.contract.message.ObjectFactory.class.getPackage().getName());
        return jaxb2Marshaller;
    }
}
