package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.CommentDTO;
import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.repository.CommentRepository;
import com.percent99.OutSpecs.repository.PostRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
     * 새로운 댓글 생성합니다.<br>
     * CommentType이 'COMMENT'나 'ANSWER'은 부모가 무조건 Post 이어야 하며,
     *  'REPLY'는 부모가 무조건 Comment이어야만 한다.
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
        } else if(dto.getType() == CommentType.REPLY){
            Comment parent = commentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

            if (parent.getType() == CommentType.REPLY) {
                throw new IllegalArgumentException("대댓글에 다시 대댓글을 달 수 없습니다.");
            }
        } else {
            throw new IllegalArgumentException("알 수 없는 댓글 타입입니다.");
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
        List<Comment> topComments = commentRepository.findByTypeAndParentId(CommentType.COMMENT, postId);
        List<Long> topCommentIds = topComments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        List<Comment> replies = new ArrayList<>();
        if (!topCommentIds.isEmpty()) {
            for (Long commentId : topCommentIds) {
                replies.addAll(commentRepository.findByTypeAndParentId(CommentType.REPLY, commentId));
            }
        }

        List<Comment> allComments = new ArrayList<>();
        allComments.addAll(topComments);
        allComments.addAll(replies);
        return allComments;
    }

    /**
     * 특정 게시물(post) 의 특정 타입의 comment를 모두 조회한다.
     * @param type 조회할 타입(answer, comment, reply)
     * @param postId 조회할 게시글의 ID
     * @return comment 목록
     */
    @Transactional(readOnly = true)
    public List<Comment> getByTypeAndPostId(CommentType type, Long postId) {
        return commentRepository.findByTypeAndParentId(type, postId);
    }

    /**
     * 지정한 ID의 댓글을 삭제한다.<br>
     * 답변은 관리자만 삭제 가능하다.
     * @param userId 로그인 유저 ID
     * @param commentId 삭제할 댓글의 ID
     */
    @Transactional
    public void deletedComment(Long userId,Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저는 존재하지 않습니다."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 댓글 내용이 발견되지않았습니다."));

        if(!user.getRole().equals(UserRoleType.ADMIN) && comment.getType().equals(CommentType.ANSWER)) {
            throw new IllegalArgumentException("질문의 답변은 관리자만 삭제할 수 있습니다.");
        } else if(!userId.equals(comment.getUser().getId())) {
            throw new IllegalArgumentException("댓글 작성자가 아닙니다.");
        } else {
            deleteCommentRecursively(commentId, comment.getType());
        }
    }

    /**
     * 재귀적으로 돌면서 자식 댓글이나 대댓글 다 삭제
     * @param commentId
     */
    private void deleteCommentRecursively(Long commentId, CommentType commentType) {
        CommentType childTypes;
        if(commentType.equals(CommentType.ANSWER)) childTypes = CommentType.COMMENT;
        else if(commentType.equals(CommentType.COMMENT)) childTypes = CommentType.REPLY;
        else childTypes = null;

        if (childTypes != null) {
            List<Comment> children = commentRepository.findByTypeAndParentId(childTypes, commentId);
            for (Comment child : children) {
                deleteCommentRecursively(child.getId(), child.getType());
            }
        }
        commentRepository.deleteById(commentId);
    }
}