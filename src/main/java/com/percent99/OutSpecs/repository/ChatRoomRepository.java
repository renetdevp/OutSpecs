package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.ChatRoom;
import com.percent99.OutSpecs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
  @Query(value = "SELECT cr FROM ChatRoom cr WHERE cr.user1.username = :username OR cr.user2.username = :username")
  List<ChatRoom> findByUsername(@Param("username") String username);

  boolean existsByUser1AndUser2(User user1, User user2);

  @Query(value = "SELECT EXISTS (SELECT 1 FROM chat_rooms WHERE id = :chatRoomId AND (user1_id = :userId OR user2_id = :userId))", nativeQuery = true)
  boolean existsByIdAndUserId(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);
}
