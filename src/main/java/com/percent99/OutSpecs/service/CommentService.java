package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.CommentDTO;
import com.percent99.OutSpecs.entity.Comment;
import com.percent99.OutSpecs.entity.CommentType;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.repository.CommentRepository;
import com.percent99.OutSpecs.repository.PostRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 ** 댓글(comment) 생성·조회·수정·삭제 기능을 제공하는 서비스
 * <ul>
 *     <li>@Service으로 등록되어 DI 대상이된다</li>
 *     <li>모든 쓰기 메세드에 @Transactional 적용하고, 읽기 메서드에 @Transactional를 사용하지않는다.</li>
 *     <li>외부 API를 사용할경우 외부 API 부분을 제외한 부분에서 @Transactional를 사용한다.</li>
 *     <li>존재하지 않는 댓글 조회시 EntityNotFoundException를 던진다.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    /**
     * 새로운 댓글 생성합니다.
     * @param dto 댓글 생성에 필요한 데이터(dto)
     * @return 저장된 댓글 엔티티
     */
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
     * 지정한 ID의 댓글을 조회한다.
     * @param id 조회할 댓글의 ID
     * @return 조회된 댓글 엔티티
     */
    @Transactional(readOnly = true)
    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 댓글 내용이 발견되지않습니다."));
    }

    /**
     * 지정한 ID의 댓글을 수정한다.
     * @param id 수정할 댓글의 ID
     * @param dto 수정할 내용이 담긴 DTO
     * @return 업데이트된 댓글 엔티티
     */
    @Transactional
    public Comment updateComment(Long id, CommentDTO dto) {
        Comment comment = getCommentById(id);
        comment.setContent(dto.getContent());
        return commentRepository.save(comment);
    }

    /**
     * 특정 게시물(post) 의 모든 댓글을 조회한다.
     * @param postId 조회할 게시글의 ID
     * @return 댓글 목록
     */
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByParentId(postId);
    }

    /**
     * 지정한 ID의 댓글을 삭제한다.
     * @param id 삭제할 댓글의 ID
     */
    @Transactional
    public void deletedComment(Long id) {
        if(!commentRepository.existsById(id)){
            throw new EntityNotFoundException("해당 댓글 내용이 발견되지않았습니다.");
        }
        commentRepository.deleteById(id);
    }
}