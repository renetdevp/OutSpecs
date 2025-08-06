package com.percent99.OutSpecs.dto;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * 유저 DTO 검증 테스트 클래스
 */
public class UserDTOValidationTest {

    private Validator validator;

    /**
     * Validator 인스턴스를 초기화합니다.
     * <p>
     * 각 테스트 전에 ValidatorFactory로부터 Validator를 생성하여 테스트에 사용합니다.
     * </p>
     */
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * 모든 필드가 유효한 UserDTO의 경우 제약 위반이 없음을 검증합니다.
     */
    @Test
    @DisplayName("유요한 UserDTO일 경우 제약 위반 X")
    void whenAllFieldValid_thenNoViolations(){

        // given
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("test@naver.com");
        userDTO.setPassword("AbcdeF1!");

        // when / then
        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
        assertThat(violations).isEmpty();
    }

    /**
     * 이메일 형식이 올바르지 않은 경우 username 필드에서 제약 위반이 발생함을 검증합니다.
     */
    @Test
    @DisplayName("이메일 형식이 올바르지 않을 때 username 필드 위반")
    void whenImValidEmail_thenNoViolationOnUsername(){

        // given
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("bad");
        userDTO.setPassword("AbcdeF1!");

        // when / then
        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("username");

    }

    /**
     * 비밀번호가 너무 짧거나 정해진 패턴에 맞지 않는 경우 password 필드에서 제약 위반이 발생함을 검증합니다.
     */
    @Test
    @DisplayName("비밀번호가 짧거나 패턴이 불일칠일 때 password 필드 위반")
    void whenInValidPassword_thenNoViolationOnPassword(){

        // given
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("test@naver.com");
        userDTO.setPassword("Abc");

        // when / then
        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("password");
    }
}