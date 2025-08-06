package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.PostType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Post 엔티티에 대한 데이터 접근 기능을 제공하느 Repository 인터페이스
 * <p>
 *  JpaRepository를 상속받아 기본 CRUD 메서드를 제공하며,
 *  추가로 유저별, 게시글 타입별로 게시글을 조회한다.
 * </p>
 */
@Repository
public interface PostRepository extends JpaRepository<Post,Long> {

    /**
     * 특정 유저가 등록한 모든 게시글을 조회한다.
     * @param userId 조회할 유저의 ID
     * @return 해당 유저의 게시글 리스트
     */
    List<Post> findByUserId(Long userId);

    /**
     * 특정 유형(type)의 게시글을 조회한다.
     * @param type 조회할 게시글의 타입
     * @return 해당 유형의 게시글 리스트
     */
    List<Post> findByType(PostType type);

    /**
     * 게시판 타입에 따라 최신글을 조회한다.
     * @param type 조회할 게시글의 타입
     * @param pageable 조회할 게시글 개수
     * @return 해당 유형의 게시글 리스트
     */
    List<Post> findByTypeOrderByCreatedAtDesc(PostType type, Pageable pageable);

    /**
     * 게시판 타입에 따라 조회수 높은 순 조회한다.
     * @param type 조회할 게시글의 타입
     * @param pageable 조회할 게시글 개수
     * @return 해당 유형의 게시글 리스트
     */
    List<Post> findByTypeOrderByViewCountDesc(PostType type, Pageable pageable);

    /**
     * 게시판 타입에 따라 좋아요 수 높은 순으로 조회한다.
     * @param type 조회할 게시글의 타입
     * @param pageable 조회할 게시글 개수
     * @return 해당 유형의 좋아요 높은 게시글 리스트
     */
    @Query(value = "SELECT p FROM Post p, Reaction r "
            + "WHERE r.targetType = 'POST' AND r.reactionType = 'LIKE' AND r.targetId = p.id AND p.type = :type "
            + "GROUP BY p.id ORDER BY COUNT(r.id) DESC")
    List<Post> findByTypeOrderByLike(@Param("type") PostType type, Pageable pageable);
}