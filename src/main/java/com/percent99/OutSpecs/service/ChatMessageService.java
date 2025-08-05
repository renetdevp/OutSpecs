package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.entity.ChatMessage;
import com.percent99.OutSpecs.entity.ChatRoom;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.repository.ChatMessageRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 사용자들이 주고받는 채팅 메시지를 관리하기 위한 service 객체.<br>
 */
@RequiredArgsConstructor
@Service
public class ChatMessageService {
  private final ChatMessageRepository chatMessageRepository;
  private final ChatRoomUserService chatRoomUserService;
  private final ChatRoomService chatRoomService;
  private final UserRepository userRepository;

  /**
   *
   * @param chatMessage 사용자가 송신한 채팅 메시지 객체
   * @param userId 로그인한 사용자의 id 값
   * @return 생성한 chatMessage 객체
   */
  public void createChatMessage(ChatMessage chatMessage, Long userId){
    ChatRoom chatRoom = chatRoomService.findChatRoomById(chatMessage.getChatRoom().getId()).orElse(null);
    User user = userRepository.findById(userId).orElse(null);

    if (chatRoom==null || user==null) return;

    chatMessage.setSender(user);

    Long lastMessageId = chatMessageRepository.save(chatMessage).getId();

    chatRoom.setLastMessageId(lastMessageId);
    chatRoomService.updateChatRoomById(chatRoom, userId);
  }

  public List<ChatMessage> findAllByChatRoomId(Long chatRoomId, Long userId){
    if (!chatRoomUserService.existsByChatRoomIdAndUserId(chatRoomId, userId)){
      return null;
    }

    return chatMessageRepository.findAllByChatRoomId(chatRoomId);
  }

  public ChatMessage updateChatMessage(ChatMessage chatMessage, Long userId){
    if (!isChatMessageSender(chatMessage, userId)) return null;

    return chatMessageRepository.save(chatMessage);
  }

  public void deleteAllChatMessages(Long chatRoomId, Long userId){
    if (!chatRoomUserService.existsByChatRoomIdAndUserId(chatRoomId, userId)){
      return;
    }

    chatMessageRepository.deleteAllByChatRoomIdAndUserId(chatRoomId, userId);
  }

  public void deleteChatMessage(Long userId, Long chatMessageId){
    if (!isChatMessageSender(userId, chatMessageId)) return;

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
   *
   * @param chatMessageId userId 값이 해당 송신자의 id 값과 일치하는지 검증할 ChatMessage 객체의 id 값
   * @param userId 현재 로그인한 사용자의 id 값
   * @return userId 값이 chatMessage 송신자의 id 값과 일치하는지 여부를 반환
   */
  public boolean isChatMessageSender(Long chatMessageId, Long userId){
    ChatMessage chatMessage = chatMessageRepository.findById(chatMessageId).orElse(null);

    if (chatMessage == null) return false;

    return this.isChatMessageSender(chatMessage, userId);
  }
}
