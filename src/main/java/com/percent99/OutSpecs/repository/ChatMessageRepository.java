package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
  List<ChatMessage> findAllByChatRoomId(Long chatRoomId);

  @Query("DELETE FROM chat_messages WHERE chat_room_id=:chatRoomId AND user_id=:userId")
  void deleteAllByChatRoomIdAndUserId(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);
}
