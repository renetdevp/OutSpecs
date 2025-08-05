package com.percent99.OutSpecs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 어느 사용자가 어느 채팅방에 참여하고 있는지를 저장하기 위한 users와 chat_rooms의 중간 테이블. <br>
 * Field: chatRoom, user, joinedAt.<br>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "chat_room_user")
public class ChatRoomUser {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_room_id")
  private ChatRoom chatRoom;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "joined_at")
  private LocalDateTime joinedAt;
}
