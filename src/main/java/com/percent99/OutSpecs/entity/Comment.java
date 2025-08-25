package com.percent99.OutSpecs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 사용자가 남긴 댓글을 저장하기 위한 테이블.
 * Field: id, user, type, parentId, content, createdAt
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "comments")
public class Comment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private CommentType type;

  @Column(name = "parent_id", nullable = false)
  private Long parentId;

  @Column(name = "content", nullable = false, length = 255)
  private String content;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
