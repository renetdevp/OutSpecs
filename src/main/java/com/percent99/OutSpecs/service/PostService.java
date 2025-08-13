package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.*;
import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.handler.PostDetailHandler;
import com.percent99.OutSpecs.repository.PostRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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
 *     <li>존재하지 않는 글 조회시 EntityNotFoundException를 던진다.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostQueryService postQueryService;
    private final UserService userService;
    private final List<PostDetailHandler> detailHandlers;
    private final CommentService commentService;

    /**
     * 새로운 게시글을 생성한다.
     * @param dto 게시글 생성에 필요한 데이터(dto)
     * @return 생성된 Post 엔티티
     */
    @Transactional
    public Post createPost(PostDTO dto) {

        if (dto.getTitle() == null || dto.getContent() == null || dto.getType() == null) {
            throw new IllegalArgumentException("필수 항목이 누락되었습니다.");
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("해당 유저는 존재하지 않습니다."));

        if(dto.getType() == PostType.RECRUIT && !user.getRole().equals(UserRoleType.ENTUSER)){
            throw new IllegalArgumentException("채용 공고는 기업 회원만 작성할 수 있습니다.");
        }

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

        if(post.getType().equals(PostType.AIPLAY)) {
            userService.decrementAiRateLimit(user.getId());
        }

        return postRepository.save(post);
    }

    /**
     * ID로 게시글을 수정한다.
     * @param id 수정할 게시글의 ID
     * @param dto 수정할 필드가 담긴 DTO
     * @return 수정된 Post 엔티티
     */
    @Transactional
    public Post updatePost(Long id, PostDTO dto) {

        Post post = postQueryService.getPostById(id);
        if(!post.getUser().getId().equals(dto.getUserId())) {
            throw new IllegalArgumentException("게시글 작성자가 아닙니다.");
        }

        if(dto.getType() == PostType.RECRUIT && !post.getUser().getRole().equals(UserRoleType.ENTUSER)){
            throw new IllegalArgumentException("채용 공고는 기업 회원만 작성할 수 있습니다.");
        }

        post.setType(dto.getType());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setUpdatedAt(LocalDateTime.now());

        detailHandlers.stream()
                .filter(h -> h.supports(dto.getType()))
                .forEach(h -> h.handle(post,dto));

        return postRepository.save(post);
    }

    /**
     * ID로 게시글을 삭제한다. <br>
     * 질문 게시글은 관리자만 삭제 가능하며 관리자는 모든 게시글 삭제 가능하다. <br>
     * @param postId 삭제할 게시글의 ID
     * @param userId 로그인 유저 ID
     */
    @Transactional
    public void deletedPost(Long userId, Long postId) {

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("해당 유저는 존재하지 않습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new EntityNotFoundException("해당 게시물은 존재하지 않습니다."));

        boolean isAdmin = user.getRole().equals(UserRoleType.ADMIN);
        boolean isOwner = userId.equals(post.getUser().getId());

        // 관리자는 모든 게시글을 삭제할 수 있습니다.
        if (isAdmin) {
            deleteAllCommentsById(userId, postId);
            return;
        }

        // 관리자가 아닌 경우, Q&A 게시글은 삭제할 수 없습니다.
        if (post.getType().equals(PostType.QNA)) {
            throw new IllegalArgumentException("QnA 게시글은 관리자만 삭제할 수 있습니다.");
        }

        // 자신의 게시글이 아니면 삭제할 수 없습니다.
        if (!isOwner) {
            throw new IllegalArgumentException("게시글 작성자가 아닙니다.");
        }

        deleteAllCommentsById(userId, postId);
    }

    @Transactional
    public void deleteAllCommentsById(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new EntityNotFoundException("해당 게시물은 존재하지 않습니다."));

        List<Comment> comments;
        if(post.getType().equals(PostType.QNA)) {
            comments = commentService.getByTypeAndPostId(CommentType.ANSWER, postId);
        } else comments = commentService.getByTypeAndPostId(CommentType.COMMENT, postId);

        if(comments != null && !comments.isEmpty()) {
            for(Comment comment : comments) {
                commentService.deletedComment(userId, comment.getId());
            }
        }
        postRepository.deleteById(postId);
    }

    /**
     * 질문 게시글의 답변상태가 true일 시 false로 false일 시 true로 업데이트한다.
     * @param userId 로그인 유저 ID
     * @param postId 질문게시글 ID
     */
    @Transactional
    public void updateAnswerComplete(Long userId, Long postId) {

        if(!userRepository.existsById(userId)){
            throw new EntityNotFoundException("해당 유저는 존재하지 않습니다.");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new EntityNotFoundException("해당 게시물은 존재하지 않습니다."));

        if (!post.getType().equals(PostType.QNA)) {
            throw new IllegalArgumentException("QnA 게시글이 아닙니다.");
        }
        if(!userId.equals(post.getUser().getId())) {
            throw new IllegalArgumentException("게시글 작성자가 아닙니다.");
        }

        if (post.getPostQnA() == null) { return; }

        boolean currentStatus = post.getPostQnA().getAnswerComplete();
        post.getPostQnA().setAnswerComplete(!currentStatus);
    }
}