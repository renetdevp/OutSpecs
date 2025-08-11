package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.Comment;
import com.percent99.OutSpecs.entity.CommentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Comment 엔티티에 대한 데이터 접근 기능을 제공하느 Repository 인터페이스
 * <p>
 *  JpaRepository를 상속받아 기본 CRUD 메서드를 제공하며,
 *  추가로 게시글별 댓글 조회 메서드를 정의한다.
 * </p>
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    /**
     * 부모객체에 등록된 모든 댓글을 조회한다.
     * @param parentId 조회할 부모의 ID
     * @return 해당 부모에 달린 댓글 목록 (댓글이 없으면 빈 리스트 반환)
     */
    List<Comment> findByParentId(Long parentId);

    /**
     * 해당 타입의 부모 객체에 등록된 모든 댓글을 조회한다.
     * @param type 해당 타입
     * @param parentId 조회할 부모의 ID
     * @return 해당 부모에 달린 댓글 목록
     */
    List<Comment> findByTypeAndParentId(CommentType type, Long parentId);

    /**
     * type에 따른 댓글의 갯수
     * @param type 해당 타입
     * @param parentId 조회할 부모의 ID
     * @return 해당 부모에 달린 댓글 갯수
     */
    long countByTypeAndParentId(CommentType type, Long parentId);
}