package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.entity.ChatRoom;
import com.percent99.OutSpecs.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 사용자들이 채팅을 주고받는 채팅방을 관리하는 service 객체.<br>
 */
@Service
public class ChatRoomService {
  private final ChatRoomRepository chatRoomRepository;
  private final ChatRoomUserService chatRoomUserService;

  public ChatRoomService(ChatRoomRepository chatRoomRepository,
                         ChatRoomUserService chatRoomUserService){
    this.chatRoomRepository = chatRoomRepository;
    this.chatRoomUserService = chatRoomUserService;
  }

  public void createChatRoom(Long userId, Long targetId){
    if (chatRoomUserService.existsByUserIdAndTargetUserId(userId, targetId)){
      return;
    }

    ChatRoom chatRoom = new ChatRoom();

//    if (targetId.equals(CHATBOT_USER_ID)) chatRoom.setIsChatbot(true);
//    else chatRoom.setIsChatbot(false);
    chatRoom.setIsChatbot(false);
    chatRoom.setLastMessageId(null);

    chatRoom = chatRoomRepository.save(chatRoom);
    chatRoomUserService.createChatRoomUser(chatRoom, userId);
    chatRoomUserService.createChatRoomUser(chatRoom, targetId);
  }

  public Optional<ChatRoom> findChatRoomById(Long chatRoomId){
    if (chatRoomId == null) return Optional.empty();

    return chatRoomRepository.findById(chatRoomId);
  }

  public ChatRoom updateChatRoomById(ChatRoom chatRoom, Long userId){
    if (chatRoom==null || userId==null) return null;
    if (!chatRoomUserService.existsByChatRoomIdAndUserId(chatRoom.getId(), userId)) return null;

    return chatRoomRepository.save(chatRoom);
  }

  public void deleteChatRoomById(Long chatRoomId, Long userId){
    if (chatRoomId==null || userId==null) return;
    if (!chatRoomUserService.existsByChatRoomIdAndUserId(chatRoomId, userId)){
      return;
    }

    chatRoomRepository.deleteById(chatRoomId);
  }
}
