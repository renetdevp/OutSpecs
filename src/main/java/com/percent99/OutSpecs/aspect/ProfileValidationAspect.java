package com.percent99.OutSpecs.aspect;

import com.percent99.OutSpecs.annotation.HasProfile;
import com.percent99.OutSpecs.repository.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class ProfileValidationAspect {
  private final ProfileRepository profileRepository;

  private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
  private final ExpressionParser parser = new SpelExpressionParser();

  @Before("@annotation(hasProfile)")
  public void checkProfile(JoinPoint joinPoint, HasProfile hasProfile){
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

    Long userId = parser.parseExpression("#" + hasProfile.userIdArg())
            .getValue(context, Long.class);

    boolean profileExists = profileRepository.existsByUserId(userId);

    if (!profileExists){
      throw new EntityNotFoundException("Profile not found.");
    }
  }
}
