package com.percent99.OutSpecs.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessageDTO {
  private Long senderId;
  @NotBlank private String content;
  private LocalDateTime createdAt;
  private Long chatRoomId;

  public ChatMessageDTO(Long senderId, String content, LocalDateTime createdAt, Long chatRoomId){
    this.senderId = senderId;
    this.content = content;
    this.createdAt = createdAt;
    this.chatRoomId = chatRoomId;
  }
}
