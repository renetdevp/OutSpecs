package com.percent99.OutSpecs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
  @Bean
  public RestTemplate restTemplate(){
    RestTemplate restTemplate = new RestTemplate();
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

    requestFactory.setConnectTimeout(10*1000);
    requestFactory.setReadTimeout(30*1000);

    restTemplate.setRequestFactory(requestFactory);

    return restTemplate;
  }
}
