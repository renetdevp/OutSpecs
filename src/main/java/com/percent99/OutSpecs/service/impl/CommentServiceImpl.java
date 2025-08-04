package com.percent99.OutSpecs.service.impl;

import com.percent99.OutSpecs.dto.CommentDTO;
import com.percent99.OutSpecs.entity.Comment;
import com.percent99.OutSpecs.entity.CommentType;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.repository.CommentRepository;
import com.percent99.OutSpecs.repository.PostRepository;
import com.percent99.OutSpecs.service.CommentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 ** CommentService 인터페이스의 기본 구현체
 * <ul>
 *     <li>@Service으로 등록되어 DI 대상이된다</li>
 *     <li>모든 쓰기 메세드에 @Transactional, 읽기 메서드에 @Transactional(readOnly = true)를 적용한다</li>
 *     <li>존재하지 않는 댓글 조회시 EntityNotFoundException를 던진다.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Comment createComment(CommentDTO dto) {

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("해당 유저 정보가 발견되지않았습니다."));

        if(dto.getType() == CommentType.COMMENT || dto.getType() == CommentType.ANSWER){
            postRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        }else{
            commentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));
        }

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setType(dto.getType());
        comment.setParentId(dto.getParentId());
        comment.setContent(dto.getContent());
        comment.setCreatedAt(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 댓글 내용이 발견되지않습니다."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Comment updateComment(Long id, CommentDTO dto) {
        Comment comment = getCommentById(id);
        comment.setContent(dto.getContent());
        return commentRepository.save(comment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByParentId(postId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deletedComment(Long id) {
        if(!commentRepository.existsById(id)){
            throw new EntityNotFoundException("해당 댓글 내용이 발견되지않았습니다.");
        }
        commentRepository.deleteById(id);
    }
}