package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.annotation.HasProfile;
import com.percent99.OutSpecs.entity.ChatRoom;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.repository.ChatMessageRepository;
import com.percent99.OutSpecs.repository.ChatRoomRepository;
import com.percent99.OutSpecs.repository.ProfileRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자들이 채팅을 주고받는 채팅방을 관리하는 service 객체.<br>
 */
@RequiredArgsConstructor
@Service
public class ChatRoomService {
  private final ChatRoomRepository chatRoomRepository;
  private final UserRepository userRepository;
  private final ProfileRepository profileRepository;
  private final ChatMessageRepository chatMessageRepository;

  @HasProfile
  @Transactional
  public ChatRoom createChatRoom(Long userId, Long targetId){
    User user1 = userRepository.findById(userId).orElse(null);
    User user2 = userRepository.findById(targetId).orElse(null);

    if (user1==null || user2==null) return null;

    if (!profileRepository.existsByUserId(targetId)) return null;

    if (chatRoomRepository.existsByUsersId(userId, targetId)){
      throw new IllegalStateException("ChatRoom already exists.");
    }

    ChatRoom chatRoom = new ChatRoom();

//    if (targetId.equals(CHATBOT_USER_ID)) chatRoom.setIsChatbot(true);
//    else chatRoom.setIsChatbot(false);
    chatRoom.setUser1(user1);
    chatRoom.setUser2(user2);
    chatRoom.setChatbot(false);
    chatRoom.setLastMessageId(null);

    return chatRoomRepository.save(chatRoom);
  }

  @Transactional
  public ChatRoom updateChatRoomById(ChatRoom chatRoom, Long userId){
    if (chatRoom==null || userId==null) return null;
    if (!isChatRoomParticipant(chatRoom.getId(), userId)) return null;

    return chatRoomRepository.save(chatRoom);
  }

  /**
   * chatRoomId와 userId를 parameter로 받아 해당 채팅방을 삭제하는 메소드. <br>
   * 사용자가 해당 채팅방에 참여하고 있지 않다면 삭제할 수 없음.
   * @param chatRoomId 삭제할 채팅방의 id 값
   * @param userId 채팅방을 삭제하려 하는 사용자의 id 값
   */
  @Transactional
  public void deleteChatRoomById(Long chatRoomId, Long userId){
    if (chatRoomId==null || userId==null) return;
    if (!isChatRoomParticipant(chatRoomId, userId)) return;

//    chatMessageRepository.deleteAllByChatRoomIdAndUserId(chatRoomId, userId);
    chatRoomRepository.deleteById(chatRoomId);
  }

  /**
   * 해당 사용자가 참여중인 모든 채팅방을 삭제하는 메소드
   * @param userId 채팅방을 삭제할 사용자의 id 값
   */
  @Transactional
  public void deleteAllChatRoomsByUserId(Long userId){
    if (userId == null) return;

    chatRoomRepository.deleteAllByUserId(userId);
  }

  /**
   * 사용자가 해당 채팅방에 참여중인지 여부를 반환하는 메소드
   * @param chatRoomId 사용자가 참여중인지 확인할 채팅방의 id 값
   * @param userId 채팅방에 참여중인지 확인할 사용자의 id 값
   * @return 사용자가 해당 채팅방에 참여중인지 여부
   */
  public boolean isChatRoomParticipant(Long chatRoomId, Long userId){
    return chatRoomRepository.existsByIdAndUserId(chatRoomId, userId);
  }
}
