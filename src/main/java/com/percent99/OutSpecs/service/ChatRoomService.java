package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.entity.ChatRoom;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.repository.ChatRoomRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 사용자들이 채팅을 주고받는 채팅방을 관리하는 service 객체.<br>
 */
@RequiredArgsConstructor
@Service
public class ChatRoomService {
  private final ChatRoomRepository chatRoomRepository;
  private final UserRepository userRepository;

  public void createChatRoom(Long userId, Long targetId){
    User user1 = userRepository.findById(userId).orElse(null);
    User user2 = userRepository.findById(targetId).orElse(null);

    if (user1==null || user2==null) return;

    if (chatRoomRepository.existsByUser1AndUser2(user1, user2)) return;
    if (chatRoomRepository.existsByUser1AndUser2(user2, user1)) return;

    ChatRoom chatRoom = new ChatRoom();

//    if (targetId.equals(CHATBOT_USER_ID)) chatRoom.setIsChatbot(true);
//    else chatRoom.setIsChatbot(false);
    chatRoom.setUser1(user1);
    chatRoom.setUser2(user2);
    chatRoom.setIsChatbot(false);
    chatRoom.setLastMessageId(null);

    chatRoomRepository.save(chatRoom);
  }

  public Optional<ChatRoom> findChatRoomById(Long chatRoomId){
    if (chatRoomId == null) return Optional.empty();

    return chatRoomRepository.findById(chatRoomId);
  }

  public ChatRoom updateChatRoomById(ChatRoom chatRoom, Long userId){
    if (chatRoom==null || userId==null) return null;
    if (!chatRoomRepository.existsByIdAndUserId(chatRoom.getId(), userId)) return null;

    return chatRoomRepository.save(chatRoom);
  }

  public void deleteChatRoomById(Long chatRoomId, Long userId){
    if (chatRoomId==null || userId==null) return;
    if (!chatRoomRepository.existsByIdAndUserId(chatRoomId, userId)) return;

    chatRoomRepository.deleteById(chatRoomId);
  }
}
