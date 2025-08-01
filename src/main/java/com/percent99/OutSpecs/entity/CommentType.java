package com.percent99.OutSpecs.entity;

/**
 * Definition type of comment.
 * Enum values: COMMENT, ANSWER, REPLY.
 */
public enum CommentType {
  /**
   * 댓글, 일반적인 게시글에 달린 댓글
   */
  COMMENT,

  /**
   * 답글, Q&A 게시글에 달린 댓글
   */
  ANSWER,

  /**
   * 대댓글, 댓글에 달린 댓글
   */
  REPLY
}
