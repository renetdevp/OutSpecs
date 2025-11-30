package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.annotation.HasProfile;
import com.percent99.OutSpecs.annotation.IsParticipant;
import com.percent99.OutSpecs.annotation.IsChatMessageSender;
import com.percent99.OutSpecs.dto.ChatMessageDTO;
import com.percent99.OutSpecs.entity.ChatMessage;
import com.percent99.OutSpecs.entity.ChatRoom;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자들이 주고받는 채팅 메시지를 관리하기 위한 service 객체.<br>
 */
@RequiredArgsConstructor
@Service
public class ChatMessageService {
  private final ChatMessageRepository chatMessageRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final SimpMessageSendingOperations messagingTemplate;

  /**
   * 채팅 메시지를 생성하는 메소드
   * @param chatRoomId 사용자가 채팅 메시지를 송신한 채팅방의 id 값
   * @param chatMessageDTO 사용자가 송신한 채팅 메시지 DTO 객체
   * @param userId 로그인한 사용자의 id 값
   */
  @IsParticipant
  @HasProfile
  @Transactional
  public void createChatMessage(Long chatRoomId, ChatMessageDTO chatMessageDTO, Long userId){
    if (chatRoomId == null){
      throw new IllegalArgumentException("chatRoomId must not be null.");
    }

    if (chatMessageDTO == null){
      throw new IllegalArgumentException("chatMessageDTO must not be null.");
    }

    if (userId == null){
      throw new IllegalArgumentException("userId must not be null.");
    }

    ChatRoom chatRoom = chatRoomRepository.searchChatRoomWithUsers(chatRoomId, userId).orElseThrow(
            () -> new EntityNotFoundException("ChatRoom not found.")
    );
    User sender = null;

    if (chatRoom.getUser1() != null && chatRoom.getUser1().getId().equals(userId)){
      sender = chatRoom.getUser1();
    }else if (chatRoom.getUser2() != null && chatRoom.getUser2().getId().equals(userId)){
      sender = chatRoom.getUser2();
    }

    if (sender == null){
      throw new IllegalStateException("Not a participant in the chatroom.");
    }

    ChatMessage chatMessage = new ChatMessage(chatRoom, chatMessageDTO.getContent(), sender);

    chatMessage = chatMessageRepository.saveAndFlush(chatMessage);

    Long lastMessageId = chatMessage.getId();

    chatRoom.setLastMessageId(lastMessageId);
  }

  @IsParticipant
  @Transactional(readOnly = true)
  public List<ChatMessageDTO> getRecentChatMessageDTO(Long chatRoomId, Long userId, LocalDateTime cursor, Long limit){
    if (chatRoomId == null){
      throw new IllegalArgumentException("chatRoomId must not be null.");
    }

    if (userId == null){
      throw new IllegalArgumentException("userId must not be null.");
    }

    if (cursor == null){
      throw new IllegalArgumentException("cursor must not be null.");
    }

    if (limit == null){
      throw new IllegalArgumentException("limit must not be null.");
    }

    return chatMessageRepository.searchChatMessageAsDTO(chatRoomId, cursor, limit);
  }

  /**
   * chatMessage와 userId를 parameter로 받아 해당 chatMessage를 업데이트하는 메소드. <br>
   * @param chatMessage 덮어쓸 채팅 메시지
   * @param userId 메시지를 업데이트하려는 사용자의 id 값
   * @return 업데이트된 채팅 메시지
   */
  @Transactional
  public ChatMessage updateChatMessage(ChatMessage chatMessage, Long userId){
    if (!isChatMessageSender(chatMessage, userId)) return null;

    return chatMessageRepository.save(chatMessage);
  }

  /**
   * id 값이 chatMessageId 값과 일치하고, Sender의 id 값이 userId 값과 일치하는 chatMessage를 삭제하는 메소드
   * @param userId 삭제하고자 하는 메시지의 Sender id 값
   * @param chatMessageId 삭제하고자 하는 메시지의 id 값
   */
  @IsChatMessageSender
  @Transactional
  public void deleteChatMessage(Long userId, Long chatMessageId){
    chatMessageRepository.deleteById(chatMessageId);
  }

  /**
   *
   * @param chatMessage userId 값이 해당 ChatMessage 송신자의 id 값과 일치하는지 검증할 ChatMessage 객체
   * @param userId 현재 로그인한 사용자의 id 값
   * @return userId 값이 chatMessage 송신자의 id 값과 일치하는지 여부를 반환
   */
  public boolean isChatMessageSender(ChatMessage chatMessage, Long userId){
    return chatMessage.getSender().getId().equals(userId);
  }

  /**
   * chatRoomId 채팅방에 속한 사용자에게 메시지를 전송하는 메소드
   * @param chatRoomId 메시지를 전송할 채팅방
   * @param userId 메시지를 전송하고자 하는 사용자의 id 값
   * @param chatMessageDTO 전송하고자 하는 메시지
   */
  @IsParticipant
  public void sendMessage(Long chatRoomId, Long userId, ChatMessageDTO chatMessageDTO){
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(
            () -> new IllegalStateException("ChatRoom not found.")
    );

    Long user1Id = chatRoom.getUser1().getId();
    Long user2Id = chatRoom.getUser2().getId();

    Long targetId = user1Id.equals(userId) ? user2Id : user1Id;

    chatMessageDTO.setSenderId(userId);
    chatMessageDTO.setCreatedAt(LocalDateTime.now());

    chatMessageDTO.setChatRoomId(chatRoomId);

    messagingTemplate.convertAndSend("/queue/users/"+targetId, chatMessageDTO);
    messagingTemplate.convertAndSend("/queue/users/"+userId, chatMessageDTO);
  }
}
