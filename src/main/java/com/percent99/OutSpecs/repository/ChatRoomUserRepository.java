package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.ChatRoom;
import com.percent99.OutSpecs.entity.ChatRoomUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {
  @Query("SELECT 1 FROM ChatRoomUser crt " +
          "WHERE cru.chatRoom.id = :chatRoomId AND cru.user.id = :userId LIMIT 1")
  boolean existsByChatRoomIdAndUserId(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);

  List<ChatRoomUser> findAllByChatRoomId(Long chatRoomId);
  List<ChatRoomUser> findAllByUserId(Long userId);

  @Query("DELETE FROM ChatRoomUser cru WHERE cru.chatRoom.id = :chatRoomId AND cru.user.id = :userId")
  void deleteByChatRoomIdAndUserId(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);

  @Query("SELECT cru.chatRoom FROM ChatRoomUser cru WHERE cru.chatRoom IN " +
          "(SELECT cru2.chatRoom FROM ChatRoomUser cru2 WHERE cru2.user.id = :userId) " +
          "AND cru.user.id != :userId")
  List<ChatRoom> findChatRoomsByUserId(@Param("userId") Long userId);

  @Query("SELECT 1 " +
  "FROM ChatRoomUser cru1 " +
  "JOIN ChatRoomUser cru2 ON cru1.chatRoom = cru2.chatRoom " +
  "WHERE cru1.user.id = :userId " +
  "AND cru2.user.id = :targetUserId " +
  "LIMIT 1;")
  boolean existsChatRoomByUserIdAndTargetUserId(@Param("userId") Long userId, @Param("targetUserId") Long targetUserId);
}
