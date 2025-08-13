package com.percent99.OutSpecs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

//@Configuration
//@EnableWebSocketSecurity
//public class WebSocketSecurityConfig {
//  @Bean
//  AuthorizationManager<Message<?>> messageAuthorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages){
//    messages
//            .simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.DISCONNECT).permitAll()
//            .simpDestMatchers("/app/chats/**").authenticated()
//            .simpSubscribeDestMatchers("/queue/rooms/**").authenticated()
//            .anyMessage().denyAll();
//
//    return messages.build();
//  }
//}

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
  /**
   * client로부터 들어오는(inbound) 메시지를 어떻게 처리할지 설정하는 메소드.<br>
   * 1. 기본적인 연결/연결해제 설정을 위해 SimpMessageType.CONNECT|SimpMessageType.DISCONNECT는 모두 허용 <br>
   * 2. /app/chats/** 경로로 들어오는 메시지들은 인증된 사용자만 허용 <br>
   * 3. /queue/rooms/** 경로는 인증된 사용자만 구독할 수 있음 <br>
   * 4. 이외의 모든 메시지를 거부
   * @param messages MessageMatcher를 사용하여 security expression에 보안 제약 조건을 매핑하기 위한 객체
   */
  @Override
  protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
    messages
            .simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.DISCONNECT).permitAll()
            .simpDestMatchers("/app/chats/**").authenticated()
            .simpSubscribeDestMatchers("/queue/rooms/**").authenticated()
            .anyMessage().denyAll();
  }

  /**
   * CSRF 설정을 무효화할지 여부를 반환하는 메소드
   * @return CSRF 설정 무효화 여부
   */
  @Override
  protected boolean sameOriginDisabled() {
    return true;
  }
}