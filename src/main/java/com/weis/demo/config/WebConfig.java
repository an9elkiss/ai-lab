package com.weis.demo.config;

import dev.langchain4j.http.client.HttpClientBuilderFactory;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebConfig {

    @Bean
    public HttpClientBuilderFactory httpClientBuilderFactory(){
        return new SpringRestClientBuilderFactory();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
