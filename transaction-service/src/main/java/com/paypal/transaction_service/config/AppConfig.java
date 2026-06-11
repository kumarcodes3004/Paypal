package com.paypal.transaction_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    //this will be used when we call another microservice
    //this will help to make https calls to other services

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
