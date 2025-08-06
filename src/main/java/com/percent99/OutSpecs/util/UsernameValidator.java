package com.percent99.OutSpecs.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class UsernameValidator implements ConstraintValidator<ValidUsername, String> {

    /* 이메일 검증 @ 포함 */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null && EMAIL_PATTERN.matcher(value).matches();
    }
}