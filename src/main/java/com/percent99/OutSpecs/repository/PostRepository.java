package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.PostType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Post 엔티티에 대한 데이터 접근 기능을 제공하느 Repository 인터페이스
 * <p>
 *  JpaRepository를 상속받아 기본 CRUD 메서드를 제공하며,
 *  추가로 유저별, 게시물 타입별로 게시물을 조회한다.
 * </p>
 */
public interface PostRepository extends JpaRepository<Post,Long> {

    /**
     * 특정 유저가 등록한 모든 게시물을 조회한다.
     * @param userId 조회할 유저의 ID
     * @return 해당 유저의 게시물 리스트
     */
    List<Post> findByUserId(Long userId);

    /**
     * 특정 유형(type)의 게시물을 조회한다.
     * @param type 조회할 게시물의 타입
     * @return 해당 유형의 게시물 리스트
     */
    List<Post> findByType(PostType type);
}