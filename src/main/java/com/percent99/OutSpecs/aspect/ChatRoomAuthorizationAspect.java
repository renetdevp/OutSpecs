package com.percent99.OutSpecs.aspect;

import com.percent99.OutSpecs.annotation.IsParticipant;
import com.percent99.OutSpecs.repository.ChatRoomRepository;
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
public class ChatRoomAuthorizationAspect {
  private final ChatRoomRepository chatRoomRepository;

  private final ParameterNameDiscoverer parameterNameDiscoverer;
  private final ExpressionParser parser;

  @Before("@annotation(isParticipant)")
  public void checkParticipant(JoinPoint joinPoint, IsParticipant isParticipant){
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

    Long chatRoomId = parser.parseExpression("#" + isParticipant.chatRoomIdArg())
            .getValue(context, Long.class);
    Long userId = parser.parseExpression("#" + isParticipant.userIdArg())
            .getValue(context, Long.class);

    boolean result = chatRoomRepository.existsByIdAndUserId(chatRoomId, userId);

    if (!result){
      throw new AccessDeniedException("User is not a participant in the chatroom.");
    }
  }
}
