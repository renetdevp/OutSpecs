package com.percent99.OutSpecs.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatRoomResponseDTO {
  private Long chatRoomId;
  private boolean chatRoomIsChatBot;
//  private Long chatRoomLastMessageId;
  private String chatRoomLastMessage;
  private LocalDateTime chatRoomLastMessageCreatedAt;

  private Long user1Id;
  private String user1Nickname;
  private String user1ImageUrl;

  private Long user2Id;
  private String user2Nickname;
  private String user2ImageUrl;
}
