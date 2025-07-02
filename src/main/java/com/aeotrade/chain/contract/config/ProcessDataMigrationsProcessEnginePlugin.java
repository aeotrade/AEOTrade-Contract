package com.aeotrade.chain.contract.config;

import liquibase.integration.spring.SpringLiquibase;
import lombok.AllArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;

@AllArgsConstructor
public class ProcessDataMigrationsProcessEnginePlugin implements ProcessEnginePlugin {
    private SpringLiquibase liquibase;
    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    }

    @Override
    public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {

    }

    @Override
    public void postProcessEngineBuild(ProcessEngine processEngine) {

    }
}
