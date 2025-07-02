package com.aeotrade.chain.contract.config;

import liquibase.integration.spring.SpringLiquibase;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = true)
public class ProcessEngineDatabaseMigrationsConfig {

    @Bean
    public ProcessEnginePlugin processDataMigrationsProcessEnginePlugin(SpringLiquibase springLiquibase){
        return new ProcessDataMigrationsProcessEnginePlugin(springLiquibase);
    }

}
