package com.aeotrade.chain.contract;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AeochaincontractApplication {

    public static void main(String[] args) {
        SpringApplication.run(AeochaincontractApplication.class, args);
    }

}
