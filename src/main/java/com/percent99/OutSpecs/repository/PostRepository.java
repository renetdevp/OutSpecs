package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.dto.PostResponseDTO;
import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.PostType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Post 엔티티에 대한 데이터 접근 기능을 제공하느 Repository 인터페이스
 * <p>
 *  JpaRepository를 상속받아 기본 CRUD 메서드를 제공하며,
 *  추가로 유저별, 게시글 타입별로 게시글을 조회한다.
 * </p>
 */
@Repository
public interface PostRepository extends JpaRepository<Post,Long>, PostRepositoryCustom {

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
     * 특정 유저가 작성한 AIPLAY 게시글을 조회한다.
     * @param userId 조회할 유저 ID
     * @param pageable 페이징 정보
     * @return 해당 유저의 AIPLAY 게시글 Slice
     */
    Slice<Post> findByUserIdAndType(Long userId, PostType type, Pageable pageable);

    /**
     * 게시판 타입에 따라 최신글을 조회한다.
     * @param type 조회할 게시글의 타입
     * @param pageable 조회할 게시글 개수
     * @return 해당 유형의 게시글 리스트
     */
    Slice<Post> findByTypeOrderByCreatedAtDesc(PostType type, Pageable pageable);

    /**
     * 게시판 타입에 따라 조회수 높은 순 조회한다.
     * @param type 조회할 게시글의 타입
     * @param pageable 조회할 게시글 개수
     * @return 해당 유형의 게시글 리스트
     */
    List<Post> findByTypeOrderByViewCountDesc(PostType type, Pageable pageable);

    /**
     * 특정 게시판 타입에서 선택한 태그가 모두 들어있는 게시글을 조회한다.
     * @param postType 조회할 게시판 타입
     * @param tags 원하는 태그
     * @param tagCount 태그 개수
     * @return 원하는 태그가 모두 들어가 있는 게시글 id 리스트
     */
    @Query("SELECT p.id FROM Post p JOIN p.postTags pt "
            + "WHERE p.type = :postType AND pt.tags IN :tags "
            + "GROUP BY p.id HAVING COUNT(DISTINCT pt.tags) = :tagCount")
    List<Long> findPostsByTypeAndTags(@Param("postType") PostType postType, @Param("tags") List<String> tags, @Param("tagCount") long tagCount);

    /**
     * ID 리스트로 Pageable 적용
     * @param ids postId
     * @param pageable
     * @return slice post 값
     */
    Slice<Post> findByIdIn(List<Long> ids, Pageable pageable);

    @Modifying
    @Query("UPDATE Post p set p.viewCount = p.viewCount + 1 where p.id = :postId")
    int increaseViewCount(@Param("postId") Long postId);

    /**
     * 검색 결과 타입과 제목이 같은(부분 일치, 대소문자 무시) 게시물이 존재하는지 확인합니다.
     *
     * @param type   조회할 게시물 타입(예: FREE, QNA, TEAM, RECRUIT, PLAY, AIPLAY)
     * @param title  부분 일치로 검색할 제목 키워드(대소문자 무시). 공백만 있는 값은 사용하지 않는 것을 권장합니다.
     * @return       조건을 만족하는 게시물이 하나라도 존재하면 true, 없으면 false
     */
    boolean existsByTypeAndTitleContainingIgnoreCase(PostType type, String title);

    @EntityGraph(attributePaths = {"user"})
    @Query("""
      select p
      from Post p
      where (:type is null or p.type = :type)
        and lower(p.title) like lower(concat('%', :title, '%'))
      order by p.id desc
    """)
    List<Post> searchByOptionalTypeAndTitle(@Param("type") PostType type,
                                            @Param("title") String title);

  // 각 게시판별로 좋아요 갯수 상위 limit개 게시글을 가져오는 쿼리
  @Query(value = """
    SELECT id, title, created_at, post_type, likes
    FROM (
      SELECT p.id, p.title, p.created_at, p.type AS post_type,
        COUNT(r.id) AS likes,
        ROW_NUMBER() OVER (PARTITION BY p.type ORDER BY COUNT(r.id) DESC) AS rn
      FROM posts p
      LEFT JOIN reactions r
        ON p.id = r.target_id
        AND r.target_type = 'POST'
        AND r.reaction_type = 'LIKE'
      GROUP BY p.id
    ) AS post_with_likes
    WHERE rn <= :limit
    ORDER BY post_type, likes;
  """, nativeQuery = true)
  List<HomePostDTOProjection> getMostLikesPost(@Param("limit") long limit);

  interface HomePostDTOProjection {
    long getId();
    String getTitle();
    LocalDateTime getCreatedAt();
    PostType getPostType();
    long getLikes();
  }

  @Query(value = """
    WITH reaction_info AS (
      SELECT
        COALESCE(COUNT(*) FILTER (WHERE reaction_type = 'LIKE'), 0) AS likes,
        COALESCE(BOOL_OR(reaction_type = 'LIKE' AND user_id = :userId), FALSE) AS is_liked,
        COALESCE(BOOL_OR(reaction_type = 'BOOKMARK' AND user_id = :userId), FALSE) AS is_bookmarked,
        COALESCE(BOOL_OR(reaction_type = 'REPORT' AND user_id = :userId), FALSE) AS is_reported
      FROM reactions
      WHERE target_type = 'POST'
        AND target_id = :postId
    ),
    comment_info AS (
      SELECT
        COALESCE(COUNT(*) FILTER (WHERE type = 'COMMENT'), 0) AS comments,
        COALESCE(COUNT(*) FILTER (WHERE type = 'ANSWER'), 0) AS answers
      FROM comments
      WHERE parent_id = :postId
    ),
    participation_info AS (
      SELECT
        COALESCE(BOOL_OR(user_id = :userId), FALSE) AS is_participation,
        COALESCE(COUNT(*) FILTER (WHERE status = 'ACCEPTED'), 0) AS team_count
      FROM participations
      WHERE post_id = :postId
    )
    SELECT
      r.likes, c.comments, c.answers,
      r.is_liked, r.is_bookmarked, r.is_reported,
      p.is_participation, p.team_count
    FROM reaction_info r
    CROSS JOIN comment_info c
    CROSS JOIN participation_info p;
  """, nativeQuery = true)
  PostResponseDTO getPostReactionInfo(Long postId, Long userId);
}