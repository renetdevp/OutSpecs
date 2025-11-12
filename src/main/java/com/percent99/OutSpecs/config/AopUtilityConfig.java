package com.percent99.OutSpecs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

@Configuration
public class AopUtilityConfig {
  @Bean
  public ExpressionParser expressionParser(){
    return new SpelExpressionParser();
  }

  @Bean
  public ParameterNameDiscoverer parameterNameDiscoverer(){
    return new DefaultParameterNameDiscoverer();
  }
}
