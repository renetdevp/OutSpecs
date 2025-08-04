package com.percent99.OutSpecs.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import javax.management.relation.Role;

@Getter
@Setter
public class UserDTO {

    @NotBlank(message = "이메일은 필수입니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    private String providerId;
    private Role role;
    private Integer aiRateLimit;
}
