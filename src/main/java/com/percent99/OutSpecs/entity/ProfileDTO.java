package com.percent99.OutSpecs.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileDTO {

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;

    @NotBlank(message = "기술 스택은 필수입니다.")
    private String stacks;

    @NotBlank(message = "경력 정보는 필수입니다.")
    private String experience;

    @NotBlank(message = "자기소개는 필수입니다.")
    private String selfInfo;

    @NotNull(message = "기업 공개 여부는 필수입니다.")
    private Boolean allowCompanyAccess;
}
