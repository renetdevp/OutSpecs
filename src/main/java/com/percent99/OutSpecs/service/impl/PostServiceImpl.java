package com.percent99.OutSpecs.service.impl;

import com.percent99.OutSpecs.dto.PostDTO;
import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.PostType;
import com.percent99.OutSpecs.handler.PostDetailHandler;
import com.percent99.OutSpecs.repository.PostRepository;
import com.percent99.OutSpecs.service.PostService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


/**
 ** PostService 인터페이스의 기본 구현체
 * <ul>
 *     <li>@Service으로 등록되어 DI 대상이된다</li>
 *     <li>모든 쓰기 메세드에 @Transactional, 읽기 메서드에 @Transactional(readOnly = true)를 적용한다</li>
 *     <li>존재하지 않는 댓글 조회시 EntityNotFoundException를 던진다.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final List<PostDetailHandler> detailHandlers;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Post createPost(PostDTO dto) {
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setUser(dto.getUser());
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
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시물은 존재하지않습니다."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Post updatePost(Long id, PostDTO dto) {

        Post post = getPostById(id);
        post.setType(dto.getType());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());

        detailHandlers.stream()
                .filter(h -> h.supports(dto.getType()))
                .forEach(h -> h.handle(post,dto));

        return postRepository.save(post);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Post> getPostsByUserId(Long userId) {
        return postRepository.findByUserId(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Post> getPostsByType(PostType type) {
        return postRepository.findByType(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deletedPost(Long id) {
        if(!postRepository.existsById(id)){
            throw new EntityNotFoundException("해당 게시물은 존재하지 않습니다.");
        }
        postRepository.deleteById(id);
    }
}