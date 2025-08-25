package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.PostStatus;
import com.percent99.OutSpecs.entity.PostType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
     * 게시판 타입에 따라 좋아요 수 높은 순으로 조회한다.
     * @param type 조회할 게시글의 타입
     * @param pageable 조회할 게시글 개수
     * @return 해당 유형의 좋아요 높은 게시글 id 리스트
     */
    @Query("SELECT p FROM Post p, Reaction r "
            + "WHERE r.targetType = 'POST' AND r.reactionType = 'LIKE' AND r.targetId = p.id AND p.type = :type "
            + "GROUP BY p.id ORDER BY COUNT(r.id) DESC")
    List<Post> findByTypeOrderByLike(@Param("type") PostType type, Pageable pageable);

    /**
     * 채용공고 게시판에서 기술 스택을 선택하여 하나라도 포함되어 있는 게시글을 모두 조회한다.
     * @param techs 원하는 기술스택
     * @return 기술스택을 하나라도 포함한 게시글 id 리스트
     */
    @Query("SELECT p.id FROM Post p JOIN p.postJob pj JOIN pj.techniques t "
            + "WHERE p.type = 'RECRUIT' AND t.tech IN :techs ")
    List<Long> findRecruitPostsByTechs(@Param("techs") List<String> techs);

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
     * 나가서놀기 게시판에서 선택한 장소가 포함된 게시글을 조회한다.
     * @param place 원하는 장소
     * @return 해당 장소의 게시글 리스트
     */
    @Query("SELECT p.id FROM Post p JOIN p.postHangout ph "
            + "WHERE p.type = 'PLAY' AND ph.placeName = :place")
    List<Long> findHangoutPostsByPlace(@Param("place") String place);

    /**
     * ID 리스트로 Pageable 적용
     * @param ids postId
     * @param pageable
     * @return slice post 값
     */
    Slice<Post> findByIdIn(List<Long> ids, Pageable pageable);


    /**
     * 팀모집 상태에 따른 게시글 조회
     * @param status 팀모집 상태
     * @return 팀모집 상태별 게시글
     */
    @Query("SELECT p FROM Post p JOIN p.teamInfo pt WHERE p.type = 'TEAM' AND pt.status = :status")
    List<Post> findTeamPostsByStatus(@Param("status") PostStatus status);

    /**
     * 게시물과 연관된 정보를 모두 조회합니다.
     * @param postId 조회할 게시물의 ID
     * @return 조회된 게시물
     */
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH p.postHangout " +
            "LEFT JOIN FETCH p.postJob " +
            "LEFT JOIN FETCH p.postQnA " +
            "LEFT JOIN FETCH p.teamInfo " +
            "LEFT JOIN FETCH p.postTags " +
            "WHERE p.id = :postId")
    Optional<Post> findWithDetailsById(@Param("postId") Long postId);

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
}