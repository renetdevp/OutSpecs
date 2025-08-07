package com.percent99.OutSpecs.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {


    /**
     * 비밀번호 8~20자, 영문 대/소문자·숫자·특수문자(!,@,(),$) 포함
     * <ul>
     *     <li>(?=.*[A-Za-z]) : 영문자 최소 1회</li>
     *     <li>(?=.*\\d)     : 숫자 최소 1회</li>
     *     <li>(?=.*[@$!%*#?&]) : 특수문자 최소 1회</li>
     *     <li>.{8,}         : 전체 최소길이 8자 이상</li>
     * </ul>
     */
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).{8,}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null && PASSWORD_PATTERN.matcher(value).matches();
    }
}