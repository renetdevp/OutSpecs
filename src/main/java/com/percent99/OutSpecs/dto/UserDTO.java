package com.percent99.OutSpecs.dto;

import com.percent99.OutSpecs.entity.UserRoleType;
import com.percent99.OutSpecs.util.ValidPassword;
import com.percent99.OutSpecs.util.ValidUsername;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    @NotBlank(message = "이메일은 필수입니다.")
    @ValidUsername
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @ValidPassword
    private String password;

    private String providerId;
    private UserRoleType role;
    private Integer aiRateLimit;
}