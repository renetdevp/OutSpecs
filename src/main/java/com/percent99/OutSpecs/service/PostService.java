package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.PostDTO;
import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.PostType;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.handler.PostDetailHandler;
import com.percent99.OutSpecs.repository.PostRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 ** 게시글(post) 관련 생성·조회·수정·삭제 기능을 제공하는 서비스
 * <ul>
 *     <li>@Service으로 등록되어 DI 대상이된다</li>
 *     <li>모든 쓰기 메세드에 @Transactional 적용하고, 읽기 메서드에 @Transactional를 사용하지않는다.</li>
 *     <li>외부 API를 사용할경우 외부 API 부분을 제외한 부분에서 @Transactional를 사용한다.</li>
 *     <li>존재하지 않는 댓글 조회시 EntityNotFoundException를 던진다.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final List<PostDetailHandler> detailHandlers;

    /**
     * 새로운 게시글을 생성한다.
     * @param dto 게시글 생성에 필요한 데이터(dto)
     * @return 생성된 Post 엔티티
     */
    @Transactional
    public Post createPost(PostDTO dto) {

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("해당 유저는 존재하지 않습니다."));

        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setUser(user);
        post.setType(dto.getType());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setViewCount(0);
        
        detailHandlers.stream()
                .filter(h -> h.supports(dto.getType()))
                .forEach(h -> h.handle(post,dto));
        
        return postRepository.save(post);
    }

    /**
     * ID로 게시글을 조회한다.
     * @param id 조회할 게시글의 ID
     * @return 조회된 Post 엔티티
     */
    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시물은 존재하지않습니다."));
    }

    /**
     * ID로 게시글을 수정한다.
     * @param id 수정할 게시글의 ID
     * @param dto 수정할 필드가 담긴 DTO
     * @return 수정된 Post 엔티티
     */
    @Transactional
    public Post updatePost(Long id, PostDTO dto) {

        Post post = getPostById(id);
        post.setType(dto.getType());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setUpdatedAt(LocalDateTime.now());

        post.getPostBases().clear();
        post.setTeamInfo(null);
        post.setPostJob(null);
        post.setPostHangout(null);

        detailHandlers.stream()
                .filter(h -> h.supports(dto.getType()))
                .forEach(h -> h.handle(post,dto));

        return postRepository.save(post);
    }

    /**
     * 특정 사용자가 작성한 모든 게시글을 조회한다.
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자가 작성한 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<Post> getPostsByUserId(Long userId) {
        return postRepository.findByUserId(userId);
    }

    /**
     * 특정 유형(type)의 게시글을 조회한다.
     * @param type 조회할 PostType
     * @return 지정한 유형의 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<Post> getPostsByType(PostType type) {
        return postRepository.findByType(type);
    }

    /**
     * 전체 게시글을 조회한다.
     * @return 전체 게시글 반환
     */
    @Transactional(readOnly = true)
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    /**
     * 게시판 타입에 따라 최신글 20개를 조회한다
     * @param type 게시판 타입
     * @return 최신글 목록
     */
    @Transactional(readOnly = true)
    public List<Post> getRecentPosts(PostType type) {
        return postRepository.findTop20ByTypeOrderByCreatedAtDesc(type);
    }

    /**
     * 게시판 타입에 따라 조회수 높은 순 게시글 10개를 조회한다.
     * @param type 게시판 타입
     * @return 조회수 순 10개 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<Post> getViewCountPosts(PostType type) {
        return postRepository.findTop10ByTypeOrderByViewCountDesc(type);
    }

    /**
     * 게시판 타입에 따라 좋아요 높은 순 게시글 limit개를 조회한다.
     * @param type 게시판 타입
     * @param limit 좋아요 가져올 개수
     * @return 좋아요 순 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<Post> getLikePosts(PostType type, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return postRepository.findByTypeOrderByLike(type, pageable);
    }

    /**
     * ID로 게시글을 삭제한다.
     * @param id 삭제할 게시글의 ID
     */
    @Transactional
    public void deletedPost(Long id) {
        if(!postRepository.existsById(id)){
            throw new EntityNotFoundException("해당 게시물은 존재하지 않습니다.");
        }
        postRepository.deleteById(id);
    }
}