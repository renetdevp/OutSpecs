package com.percent99.OutSpecs.aspect;

import com.percent99.OutSpecs.annotation.IsChatMessageSender;
import com.percent99.OutSpecs.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@Aspect
@RequiredArgsConstructor
public class ChatMessageAuthorizationAspect {
  private final ChatMessageRepository chatMessageRepository;

  private final ParameterNameDiscoverer parameterNameDiscoverer;
  private final ExpressionParser parser;

  @Before("@annotation(isChatMessageSender)")
  public void checkSender(JoinPoint joinPoint, IsChatMessageSender isChatMessageSender){
    MethodSignature signature = (MethodSignature)joinPoint.getSignature();
    Method method = signature.getMethod();

    Object[] args = joinPoint.getArgs();
    String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);

    if (parameterNames == null){
      throw new IllegalStateException("Parameter names must be available for AOP.");
    }

    EvaluationContext context = new StandardEvaluationContext();
    for (int i=0; i<parameterNames.length; ++i){
      context.setVariable(parameterNames[i], args[i]);
    }

    Long chatMessageId = parser.parseExpression("#" + isChatMessageSender.chatMessageIdArg())
            .getValue(context, Long.class);
    Long userId = parser.parseExpression("#" + isChatMessageSender.userIdArg())
            .getValue(context, Long.class);

    boolean result = chatMessageRepository.existsByIdAndUserId(chatMessageId, userId);

    if (!result){
      throw new AccessDeniedException("User is not a sender of this chatMessage.");
    }
  }
}
