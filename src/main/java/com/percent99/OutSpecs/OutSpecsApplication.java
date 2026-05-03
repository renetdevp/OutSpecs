package com.percent99.OutSpecs;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

import java.util.TimeZone;

@EnableRetry
@SpringBootApplication
public class OutSpecsApplication {

	public static void main(String[] args) {
		SpringApplication.run(OutSpecsApplication.class, args);
	}

  @PostConstruct
  public void init(){
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
  }
}
