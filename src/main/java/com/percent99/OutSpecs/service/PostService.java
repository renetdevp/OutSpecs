package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.PostDTO;
import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.PostType;

import java.util.List;

/**
 * 게시글(post) 관련 생성·조회·수정·삭제 기능을 제공하는 서비스 인터페이스
 */
public interface PostService {

    /**
     * 새로운 게시글을 생성한다.
     * @param dto 게시글 생성에 필요한 데이터(dto)
     * @return 생성된 Post 엔티티
     */
    Post createPost(PostDTO dto);

    /**
     * ID로 게시글을 조회한다.
     * @param id 조회할 게시글의 ID
     * @return 조회된 Post 엔티티
     */
    Post getPostById(Long id);

    /**
     * ID로 게시글을 수정한다.
     * @param id 수정할 게시글의 ID
     * @param dto 수정할 필드가 담긴 DTO
     * @return 수정된 Post 엔티티
     */
    Post updatePost(Long id, PostDTO dto);

    /**
     * 특정 사용자가 작성한 모든 게시글을 조회한다.
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자가 작성한 게시글 목록
     */
    List<Post> getPostsByUserId(Long userId);

    /**
     * 특정 유형(type)의 게시글을 조회한다.
     * @param type 조회할 PostType
     * @return 지정한 유형의 게시글 목록
     */
    List<Post> getPostsByType(PostType type);

    /**
     * 전체 게시글을 조회한다.
     * @return 전체 게시글 반환
     */
    List<Post> getAllPosts();

    /**
     * ID로 게시글을 삭제한다.
     * @param id 삭제할 게시글의 ID
     */
    void deletedPost(Long id);
}