package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.ChatRoom;
import com.percent99.OutSpecs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
  boolean existsByUser1AndUser2(User user1, User user2);

  @Query(value = "SELECT 1 FROM chat_rooms WHERE chatRoomId = :chatRoomId AND (user1_id = :userId OR user2_id = :userId) LIMIT 1", nativeQuery = true)
  boolean existsByIdAndUserId(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);
}
