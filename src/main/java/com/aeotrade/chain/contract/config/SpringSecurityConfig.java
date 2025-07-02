package com.aeotrade.chain.contract.config;

import com.aeotrade.chain.contract.controller.ContractTemplateController;
import com.aeotrade.chain.contract.controller.ContractTemplateKindController;
import com.aeotrade.chain.contract.feign.OAuth2JwtInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests().antMatchers(ContractTemplateController.API_PREFIX+"/**", ContractTemplateKindController.API_PREFIX).permitAll().anyRequest().authenticated().and().oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
        return http.build();
    }

    @Bean
    OAuth2JwtInterceptor oauth2JwtInterceptor(){
        return new OAuth2JwtInterceptor();
    }
}
