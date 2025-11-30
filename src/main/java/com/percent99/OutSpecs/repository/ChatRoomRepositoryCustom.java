package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.dto.ChatRoomResponseDTO;
import com.percent99.OutSpecs.entity.ChatRoom;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepositoryCustom {
  Optional<ChatRoom> searchChatRoomWithUsers(Long chatRoomId, Long userId);
  boolean existsByIdAndUserId(Long chatRoomId, Long userId);
  boolean existsByUsersId(Long userId, Long targetId);
  ChatRoom findByUsersId(Long userId, Long targetId);
  List<ChatRoomResponseDTO> findAllChatRoomWithDetails(Long userId);
}
