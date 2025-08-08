package com.percent99.OutSpecs.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.ArgumentMatchers.any;

import com.percent99.OutSpecs.dto.CommentDTO;
import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.repository.CommentRepository;
import com.percent99.OutSpecs.repository.PostRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;

class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    private User user;
    private User admin;
    private User otherUser;
    private Comment comment;
    private Comment answerComment;
    private Comment replyComment;
    private CommentDTO dto;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setRole(UserRoleType.USER);

        admin = new User();
        admin.setId(2L);
        admin.setRole(UserRoleType.ADMIN);

        otherUser = new User();
        otherUser.setId(3L);
        otherUser.setRole(UserRoleType.USER);

        comment = new Comment();
        comment.setId(100L);
        comment.setContent("기존 내용");
        comment.setType(CommentType.COMMENT);
        comment.setUser(user);

        answerComment = new Comment();
        answerComment.setId(200L);
        answerComment.setType(CommentType.ANSWER);
        answerComment.setUser(user);

        replyComment = new Comment();
        replyComment.setId(300L);
        replyComment.setType(CommentType.REPLY);
        replyComment.setUser(user);

        dto = new CommentDTO();
        dto.setUserId(user.getId());
        dto.setType(CommentType.COMMENT);
        dto.setParentId(10L);
        dto.setContent("댓글 내용");
    }


    /**
     * createComment test
     */
    @Test
    @DisplayName("createComment - 정상적으로 댓글 생성")
    void createCommentSuccess() {
        // given
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(postRepository.findById(dto.getParentId())).willReturn(Optional.of(new Post()));
        given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Comment result = commentService.createComment(dto);

        // then
        assertNotNull(result);
        assertEquals(dto.getContent(), result.getContent());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("createComment - 유저가 존재하지 않아 예외 발생")
    void commentUserNotFound() {
        // given
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());

        // then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> {
            commentService.createComment(dto);
        });
        assertTrue(ex.getMessage().contains("해당 유저 정보가 발견되지않았습니다."));
    }

    @Test
    @DisplayName("createComment - 부모 게시글이 존재하지 않아 예외 발생")
    void commentPostNotFound() {
        // given
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(postRepository.findById(dto.getParentId())).willReturn(Optional.empty());

        // then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> {
            commentService.createComment(dto);
        });
        assertTrue(ex.getMessage().contains("게시글을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("createComment - 대댓글에 대댓글 방지 예외 발생")
    void replyParentIsReplyException() {
        // given
        CommentDTO replyDto = new CommentDTO();
        replyDto.setUserId(user.getId());
        replyDto.setType(CommentType.REPLY);
        replyDto.setParentId(replyComment.getId());

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(commentRepository.findById(replyComment.getId())).willReturn(Optional.of(replyComment));

        // then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(replyDto);
        });
        assertEquals("대댓글에 다시 대댓글을 달 수 없습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("createComment - 알 수 없는 댓글 타입 예외 발생")
    void invalidCommentType() {
        // given
        CommentDTO invalidDto = new CommentDTO();
        invalidDto.setUserId(user.getId());
        invalidDto.setType(null);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        // then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(invalidDto);
        });
        assertEquals("알 수 없는 댓글 타입입니다.", ex.getMessage());
    }

    /**
     * updateComment test
     */
    @Test
    @DisplayName("updateComment - 댓글 내용 정상 수정")
    void updateCommentSuccess() {
        // given
        CommentDTO updateDto = new CommentDTO();
        updateDto.setContent("수정된 내용");
        given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
        given(commentRepository.save(any(Comment.class))).willAnswer(i -> i.getArgument(0));

        // when
        Comment updated = commentService.updateComment(comment.getId(), updateDto);

        // then
        assertEquals("수정된 내용", updated.getContent());
    }

    @Test
    @DisplayName("updateComment - 수정 대상 댓글이 없을 때 예외 발생")
    void updateCommentNotFound() {
        // given
        CommentDTO updateDto = new CommentDTO();
        updateDto.setContent("수정된 내용");
        given(commentRepository.findById(comment.getId())).willReturn(Optional.empty());

        // then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> {
            commentService.updateComment(comment.getId(), updateDto);
        });
        assertTrue(ex.getMessage().contains("해당 댓글 내용이 발견되지않습니다."));
    }

    /**
     * deletedComment test
     */
    @Test
    @DisplayName("deletedComment - 관리자가 답변 삭제 가능")
    void adminDeleteAnswer() {
        // given
        given(userRepository.findById(admin.getId())).willReturn(Optional.of(admin));
        given(commentRepository.findById(answerComment.getId())).willReturn(Optional.of(answerComment));
        willDoNothing().given(commentRepository).deleteById(answerComment.getId());

        // then
        assertDoesNotThrow(() -> commentService.deletedComment(admin.getId(), answerComment.getId()));
        verify(commentRepository, times(1)).deleteById(answerComment.getId());
    }

    @Test
    @DisplayName("deletedComment - 작성자가 답변 삭제 시 예외 발생")
    void userDeleteAnswerException() {
        // given
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(commentRepository.findById(answerComment.getId())).willReturn(Optional.of(answerComment));

        // then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            commentService.deletedComment(user.getId(), answerComment.getId());
        });
        assertEquals("질문의 답변은 관리자만 삭제할 수 있습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("deletedComment - 작성자가 아닌 사용자가 댓글 삭제 시 예외 발생")
    void notAuthorException() {
        // given
        given(userRepository.findById(otherUser.getId())).willReturn(Optional.of(otherUser));
        given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

        // then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            commentService.deletedComment(otherUser.getId(), comment.getId());
        });
        assertEquals("댓글 작성자가 아닙니다.", ex.getMessage());
    }

    @Test
    @DisplayName("deletedComment - 작성자가 댓글 정상 삭제")
    void authorDelete() {
        //given
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

        // then
        assertDoesNotThrow(() -> commentService.deletedComment(user.getId(), comment.getId()));
        verify(commentRepository).deleteById(comment.getId());
    }

    @Test
    @DisplayName("deletedComment - 유저가 존재하지 않을 경우 예외 발생")
    void userNotFound() {
        // given
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());

        // then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> {
            commentService.deletedComment(user.getId(), comment.getId());
        });
        assertTrue(ex.getMessage().contains("해당 유저는 존재하지 않습니다."));
    }

    @Test
    @DisplayName("deletedComment - 댓글이 존재하지 않을 경우 예외 발생")
    void commentNotFound() {
        // given
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(commentRepository.findById(comment.getId())).willReturn(Optional.empty());

        // then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> {
            commentService.deletedComment(user.getId(), comment.getId());
        });
        assertTrue(ex.getMessage().contains("해당 댓글 내용이 발견되지않았습니다."));
    }
}
