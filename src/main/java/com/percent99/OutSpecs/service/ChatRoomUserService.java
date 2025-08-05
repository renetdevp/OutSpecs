package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.entity.ChatRoom;
import com.percent99.OutSpecs.entity.ChatRoomUser;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.repository.ChatRoomUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 어느 사용자들이 어느 채팅방에 들어가 있는지에 대한 정보를 관리하기 위한 service 객체.<br>
 */
@RequiredArgsConstructor
@Service
public class ChatRoomUserService {
  private final ChatRoomUserRepository chatRoomUserRepository;
  private final UserService userService;

  public void createChatRoomUser(ChatRoom chatRoom, Long userId){
    ChatRoomUser chatRoomUser = new ChatRoomUser();
    User user = userService.findById(userId).orElse(null);

    if (user == null) return;

    chatRoomUser.setChatRoom(chatRoom);
    chatRoomUser.setUser(user);
    chatRoomUser.setJoinedAt(LocalDateTime.now());

    chatRoomUserRepository.save(chatRoomUser);
  }

  public List<ChatRoomUser> findAllByChatRoomId(Long chatRoomId){
    return chatRoomUserRepository.findAllByChatRoomId(chatRoomId);
  }

  public List<ChatRoomUser> findAllByUserId(Long userId){
    return chatRoomUserRepository.findAllByUserId(userId);
  }

  public boolean existsByChatRoomIdAndUserId(Long chatRoomId, Long userId){
    return chatRoomUserRepository.existsByChatRoomIdAndUserId(chatRoomId, userId);
  }

  /**
   *
   * @param userId 현재 로그인한 사용자의 id 값
   * @param targetUserId 사용자가 새 채팅방을 만들고자 하는 상대 사용자의 id 값
   * @return 현재 로그인한 사용자와 상대 사용자가 함께 참여중인 채팅방이 존재하는지 여부를 반환
   */
  public boolean existsByUserIdAndTargetUserId(Long userId, Long targetUserId){
    return chatRoomUserRepository.existsChatRoomByUserIdAndTargetUserId(userId, targetUserId);
  }

  public void deleteChatRoomUser(Long chatRoomId, Long userId){
    if (!existsByChatRoomIdAndUserId(chatRoomId, userId)) return;

    chatRoomUserRepository.deleteByChatRoomIdAndUserId(chatRoomId, userId);
  }
}
