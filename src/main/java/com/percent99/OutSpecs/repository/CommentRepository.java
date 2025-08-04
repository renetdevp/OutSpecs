package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Comment 엔티티에 대한 데이터 접근 기능을 제공하느 Repository 인터페이스
 * <p>
 *  JpaRepository를 상속받아 기본 CRUD 메서드를 제공하며,
 *  추가로 게시글별 댓글 조회 메서드를 정의한다.
 * </p>
 */
public interface CommentRepository extends JpaRepository<Comment,Long> {
    /**
     * 특정 게시글에 등록된 모든 댓글을 조회한다.
     * @param postId 조회할 게시글의 ID
     * @return 해당 게시글에 달린 댓글 목록 (댓글이 없으면 빈 리스트 반환)
     */
    List<Comment> findByPostId(Long postId);
}