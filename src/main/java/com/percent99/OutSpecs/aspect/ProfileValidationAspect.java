package com.percent99.OutSpecs.aspect;

import com.percent99.OutSpecs.annotation.HasProfile;
import com.percent99.OutSpecs.repository.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
public class ProfileValidationAspect {
  private final ProfileRepository profileRepository;

  @Before("@annotation(hasProfile)")
  public void checkProfile(JoinPoint joinPoint, HasProfile hasProfile){
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

    Long userId = (Long)argMap.get(hasProfile.userIdArg());

    boolean profileExists = profileRepository.existsByUserId(userId);

    if (!profileExists){
      throw new EntityNotFoundException("Profile not found.");
    }
  }
}
