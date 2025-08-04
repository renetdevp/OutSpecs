package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.CommentDTO;
import com.percent99.OutSpecs.entity.Comment;

import java.util.List;

/**
 * 댓글(comment) 생성·조회·수정·삭제 기능을 제공하는 서비스 인터페이스
 */
public interface CommentService {
    /**
     * 새로운 댓글 생성합니다.
     * @param dto 댓글 생성에 필요한 데이터(dto)
     * @return 저장된 댓글 엔티티
     */
    Comment createComment(CommentDTO dto);

    /**
     * 지정한 ID의 댓글을 조회한다.
     * @param id 조회할 댓글의 ID
     * @return 조회된 댓글 엔티티
     */
    Comment getCommentById(Long id);

    /**
     * 지정한 ID의 댓글을 수정한다.
     * @param id 수정할 댓글의 ID
     * @param dto 수정할 내용이 담긴 DTO
     * @return 업데이트된 댓글 엔티티
     */
    Comment updateComment(Long id, CommentDTO dto);

    /**
     * 특정 게시물(post) 의 모든 댓글을 조회한다.
     * @param postId 조회할 게시글의 ID
     * @return 댓글 목록
     */
    List<Comment> getCommentsByPostId(Long postId);

    /**
     * 지정한 ID의 댓글을 삭제한다.
     * @param id 삭제할 댓글의 ID
     */
    void deletedComment(Long id);
}