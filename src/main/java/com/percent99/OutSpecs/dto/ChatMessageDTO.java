package com.percent99.OutSpecs.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessageDTO {
  private Long senderId;
  @NotBlank private String content;
  private LocalDateTime createdAt;
  private Long chatRoomId;
}
