package com.percent99.OutSpecs.dto;

import com.percent99.OutSpecs.entity.ChatMessage;
import com.percent99.OutSpecs.entity.ChatRoom;
import com.percent99.OutSpecs.entity.Profile;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChatRoomResponseDTO {
  private Long chatRoomId;
  private boolean chatRoomIsChatBot;
  private String chatRoomLastMessage;
  private LocalDateTime chatRoomLastMessageCreatedAt;

  private List<ChatMessageDTO> chatMessageDTOList;

  private Long user1Id;
  private String user1Nickname;
  private String user1ImageUrl;

  private Long user2Id;
  private String user2Nickname;
  private String user2ImageUrl;

  public ChatRoomResponseDTO(ChatRoom chatRoom, ChatMessage chatMessage, Profile profile1, Profile profile2){
    this.chatRoomId = chatRoom.getId();
    this.chatRoomIsChatBot = chatRoom.isChatbot();
    this.chatRoomLastMessage = chatMessage.getContent();
    this.chatRoomLastMessageCreatedAt = chatMessage.getCreatedAt();

    this.user1Id = profile1.getUserId();
    this.user1Nickname = profile1.getNickname();
    this.user1ImageUrl = profile1.getImageUrl();

    this.user2Id = profile2.getUserId();
    this.user2Nickname = profile2.getNickname();
    this.user2ImageUrl = profile2.getImageUrl();
  }
}
