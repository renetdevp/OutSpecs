package com.percent99.OutSpecs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {
  @Bean(name = "s3UploadExecutor")
  public Executor s3UploadExecutor(){
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(20);
    executor.setQueueCapacity(40);
    executor.setThreadNamePrefix("s3-upload-");
    executor.initialize();

    return executor;
  }
}
