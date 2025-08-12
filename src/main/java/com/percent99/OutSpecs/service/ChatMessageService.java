package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.ChatMessageDTO;
import com.percent99.OutSpecs.entity.ChatMessage;
import com.percent99.OutSpecs.entity.ChatRoom;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.repository.ChatMessageRepository;
import com.percent99.OutSpecs.repository.ChatRoomRepository;
import com.percent99.OutSpecs.repository.ProfileRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
  private final ChatRoomService chatRoomService;
  private final ChatRoomRepository chatRoomRepository;
  private final UserRepository userRepository;
  private final ProfileRepository profileRepository;
  private final SimpMessageSendingOperations messagingTemplate;

  /**
   * 채팅 메시지를 생성하는 메소드
   * @param chatMessageDTO 사용자가 송신한 채팅 메시지 DTO 객체
   * @param userId 로그인한 사용자의 id 값
   */
  @Transactional
  public void createChatMessage(Long chatRoomId, ChatMessageDTO chatMessageDTO, Long userId){
    ChatRoom chatRoom = chatRoomService.findChatRoomById(chatRoomId).orElse(null);
    User user = userRepository.findById(userId).orElse(null);

    if (chatRoom==null || user==null) return;

    if (!profileRepository.existsByUserId(userId)) return;

    if (!chatRoomRepository.existsByIdAndUserId(chatRoom.getId(), userId)) return;

    ChatMessage chatMessage = new ChatMessage();

    chatMessage.setChatRoom(chatRoom);
    chatMessage.setContent(chatMessageDTO.getContent());
    chatMessage.setSender(user);
    chatMessage.setCreatedAt(LocalDateTime.now());

    chatMessage = chatMessageRepository.save(chatMessage);

    Long lastMessageId = chatMessage.getId();

    chatRoom.setLastMessageId(lastMessageId);

    chatRoomService.updateChatRoomById(chatRoom, userId);
  }

  /**
   * chatRoomId와 userId를 parameter로 받아 채팅방에 속한 모든 메시지를 가져오는 메소드.<br>
   * 사용자가 해당 chatRoom에 참가하고 있지 않다면 null을 반환.
   * @param chatRoomId 메시지를 가져오고자 하는 채팅방의 id 값
   * @param userId 메시지를 가져오려는 사용자의 id 값
   * @return 해당 채팅방의 모든 채팅 메시지를 반환
   */
  @Transactional(readOnly = true)
  public List<ChatMessage> findAllByChatRoomId(Long chatRoomId, Long userId){
    if (!chatRoomRepository.existsByIdAndUserId(chatRoomId, userId)) return null;

    return chatMessageRepository.findAllByChatRoomId(chatRoomId);
  }

  /**
   * chatRoomId와 userId, pageable을 parameter로 받아 채팅방에 속한 메시지의 일부를 가져오는 메소드.<br>
   * 사용자가 해당 chatRoom에 참가하고 있지 않다면 null을 반환.
   * @param chatRoomId 메시지를 가져오고자 하는 채팅방의 id 값
   * @param userId 메시지를 가져오려는 사용자의 id 값
   * @param pageable pagination을 위한 pageable 객체
   * @return pagination을 적용한 해당 채팅방의 채팅 메시지를 반환
   */
  @Transactional(readOnly = true)
  public Page<ChatMessage> findByChatRoomId(Long chatRoomId, Long userId, Pageable pageable){
    if (!chatRoomRepository.existsByIdAndUserId(chatRoomId, userId)) return null;

    return chatMessageRepository.findByChatRoomId(chatRoomId, pageable);
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
   * 해당 채팅방에서 사용자가 전송한 모든 메시지를 삭제하는 메소드.
   * @param chatRoomId 사용자의 채팅 메시지를 삭제할 채팅방의 id 값
   * @param userId 채팅 메시지를 삭제하려는 사용자의 id 값
   */
  @Transactional
  public void deleteAllChatMessages(Long chatRoomId, Long userId){
    if (!chatRoomRepository.existsByIdAndUserId(chatRoomId, userId)) return;

    chatMessageRepository.deleteAllByChatRoomIdAndUserId(chatRoomId, userId);
  }

  /**
   * Sender의 id 값과 userId 값이 일치하는 모든 chatMessage를 삭제하는 메소드
   * @param userId 모든 메시지를 삭제할 sender의 id 값
   */
  @Transactional
  public void deleteAllChatMessagesByUserId(Long userId){
    if (userId == null) return;

    chatMessageRepository.deleteAllByUserId(userId);
  }

  /**
   * id 값이 chatMessageId 값과 일치하고, Sender의 id 값이 userId 값과 일치하는 chatMessage를 삭제하는 메소드
   * @param userId 삭제하고자 하는 메시지의 Sender id 값
   * @param chatMessageId 삭제하고자 하는 메시지의 id 값
   */
  @Transactional
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
  @Transactional(readOnly = true)
  public boolean isChatMessageSender(Long chatMessageId, Long userId){
    ChatMessage chatMessage = chatMessageRepository.findById(chatMessageId).orElse(null);

    if (chatMessage == null) return false;

    return this.isChatMessageSender(chatMessage, userId);
  }

  /**
   * /queue/rooms/${chatRoomId} 채널에 메시지를 전송하는 메소드
   * @param chatRoomId 메시지를 전송할 채널
   * @param userId 메시지를 전송하고자 하는 사용자의 id 값
   * @param chatMessageDTO 전송하고자 하는 메시지
   */
  public void sendMessage(Long chatRoomId, Long userId, ChatMessageDTO chatMessageDTO){
    if (!chatRoomRepository.existsByIdAndUserId(chatRoomId, userId)) return;

    messagingTemplate.convertAndSend("/queue/rooms/"+chatRoomId, chatMessageDTO);
  }
}
