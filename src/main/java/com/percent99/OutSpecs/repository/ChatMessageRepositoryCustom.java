package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.dto.ChatMessageDTO;
import com.percent99.OutSpecs.entity.ChatMessage;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepositoryCustom {
  ChatMessage findByIdWithSender(Long chatMessageId);
  List<ChatMessage> findAllByChatRoomId(Long chatRoomId);
  boolean existsByIdAndUserId(Long chatMessageId, Long userId);
  List<ChatMessage> searchChatMessage(Long chatRoomId, Long userId, LocalDateTime cursor, Long limit);
  List<ChatMessageDTO> searchChatMessageAsDTO(Long chatRoomId, LocalDateTime cursor, Long limit);
  void deleteAllBySenderId(Long senderId);
}
