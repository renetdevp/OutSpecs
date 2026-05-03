package com.percent99.OutSpecs.aspect;

import com.percent99.OutSpecs.annotation.IsChatMessageSender;
import com.percent99.OutSpecs.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Aspect
@RequiredArgsConstructor
public class ChatMessageAuthorizationAspect {
  private final ChatMessageRepository chatMessageRepository;

  @Before("@annotation(isChatMessageSender)")
  public void checkSender(JoinPoint joinPoint, IsChatMessageSender isChatMessageSender){
    CodeSignature signature = (CodeSignature)joinPoint.getSignature();

    Object[] args = joinPoint.getArgs();
    String[] paramNames = signature.getParameterNames();

    if (paramNames == null){
      throw new IllegalStateException("Parameter names must be available for AOP.");
    }

    Map<String, Object> argMap = new HashMap<>();
    for (int i=0; i<paramNames.length; ++i){
      argMap.put(paramNames[i], args[i]);
    }

    Long chatMessageId = (Long)argMap.get(isChatMessageSender.chatMessageIdArg());
    Long userId = (Long)argMap.get(isChatMessageSender.userIdArg());

    boolean result = chatMessageRepository.existsByIdAndUserId(chatMessageId, userId);

    if (!result){
      throw new AccessDeniedException("User is not a sender of this chatMessage.");
    }
  }
}
