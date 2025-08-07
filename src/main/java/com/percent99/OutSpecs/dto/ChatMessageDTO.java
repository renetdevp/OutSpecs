package com.percent99.OutSpecs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageDTO {
  @NotNull private Long chatRoomId;

  @NotBlank private String content;
}
