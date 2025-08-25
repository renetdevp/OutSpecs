package com.percent99.OutSpecs.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = UsernameValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidUsername {
    String message() default "@를 포함한 유효한 이메일 주소를 입력 해주세요.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}