package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.ChatRoom;
import com.percent99.OutSpecs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
  @Query(value = "SELECT EXISTS (SELECT 1 FROM chat_rooms WHERE (user1_id = :user1Id AND user2_id = :user2Id) OR (user1_id = :user2Id AND user2_id = :user1Id))", nativeQuery = true)
  boolean existsByUser1IdAndUser2Id(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

  @Query(value = "SELECT EXISTS (SELECT 1 FROM chat_rooms WHERE id = :chatRoomId AND (user1_id = :userId OR user2_id = :userId))", nativeQuery = true)
  boolean existsByIdAndUserId(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);

  @Query("SELECT cr FROM ChatRoom cr WHERE cr.user1.id = :userId OR cr.user2.id = :userId")
  List<ChatRoom> findAllByUserId(@Param("userId") Long userId);

  @Query("SELECT cr FROM ChatRoom cr " +
          "WHERE (cr.user1.id = :userId AND cr.user2.id = :targetId) OR (cr.user1.id = :targetId AND cr.user2.id = :userId)")
  Optional<ChatRoom> findByUser1IdAndUser2Id(@Param("userId") Long userId, @Param("targetId") Long targetId);

  @Query("DELETE FROM ChatRoom cr WHERE cr.user1.id = :userId OR cr.user2.id = :userId")
  void deleteAllByUserId(@Param("userId") Long userId);
}
