package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomRepositoryCustom {
  @Query("DELETE FROM ChatRoom cr WHERE cr.user1.id = :userId OR cr.user2.id = :userId")
  void deleteAllByUserId(@Param("userId") Long userId);
}
