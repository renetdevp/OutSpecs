package com.percent99.OutSpecs.aspect;

import com.percent99.OutSpecs.annotation.IsParticipant;
import com.percent99.OutSpecs.validator.ChatValidator;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Aspect
@RequiredArgsConstructor
public class ChatRoomAuthorizationAspect {
  private final ChatValidator chatValidator;

  @Before("@annotation(isParticipant)")
  public void checkParticipant(JoinPoint joinPoint, IsParticipant isParticipant){
    CodeSignature signature = (CodeSignature) joinPoint.getSignature();
    String[] paramNames = signature.getParameterNames();
    Object[] args = joinPoint.getArgs();

    if (paramNames == null){
      throw new IllegalStateException("Parameter names must be available for AOP.");
    }

//    EvaluationContext context = new StandardEvaluationContext();
//    for (int i=0; i<parameterNames.length; ++i){
//      context.setVariable(parameterNames[i], args[i]);
//    }

//    Long chatRoomId = parser.parseExpression("#" + isParticipant.chatRoomIdArg())
//            .getValue(context, Long.class);
//    Long userId = parser.parseExpression("#" + isParticipant.userIdArg())
//            .getValue(context, Long.class);

    Map<String, Object> argMap = new HashMap<>();
    for (int i=0; i<paramNames.length; ++i){
      argMap.put(paramNames[i], args[i]);
    }

    long chatroomId = (long)argMap.get(isParticipant.chatRoomIdArg());
    long userId = (long)argMap.get(isParticipant.userIdArg());

    chatValidator.validateParticipant(chatroomId, userId);
  }
}
