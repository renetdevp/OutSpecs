package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.repository.CommentRepository;
import com.percent99.OutSpecs.repository.PostRepository;
import com.percent99.OutSpecs.repository.ReactionRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReactionServiceTest {

    @Mock
    ReactionRepository reactionRepository;
    @Mock
    PostRepository postRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    NotificationService notificationService;

    @InjectMocks
    ReactionService reactionService;

    User user;
    User targetUser;
    Long targetId = 1L;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(10L);
        targetUser = new User();
        targetUser.setId(20L);
    }

    /**
     * addReaction test
     */
    @Test
    @DisplayName("addReaction - 유저 없을시 예외")
    void addReactionNotUser() {
        // given
        given(userRepository.existsById(user.getId())).willReturn(false);

        // then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                reactionService.addReaction(user, TargetType.POST, targetId, ReactionType.LIKE)
        );
        assertEquals("해당 유저는 존재하지 않습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("addReaction - 이미 반응 존재 예외")
    void AlreadyExists() {
        // given
        given(userRepository.existsById(user.getId())).willReturn(true);
        given(reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(
                user, TargetType.POST, targetId, ReactionType.LIKE)).willReturn(true);

        // then
        EntityExistsException ex = assertThrows(EntityExistsException.class, () ->
                reactionService.addReaction(user, TargetType.POST, targetId, ReactionType.LIKE)
        );
        assertEquals("이미 반응이 존재합니다.", ex.getMessage());
    }

    @Test
    @DisplayName("addReaction - 게시글 없음 예외")
    void addReactionNotPost() {
        // given
        given(userRepository.existsById(user.getId())).willReturn(true);
        given(reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(
                user, TargetType.POST, targetId, ReactionType.LIKE)).willReturn(false);
        given(postRepository.existsById(targetId)).willReturn(false);

        // then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                reactionService.addReaction(user, TargetType.POST, targetId, ReactionType.LIKE)
        );
        assertEquals("해당 게시물은 존재하지 않습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("addReaction - 댓글 없음 예외")
    void addReactionNotComment() {
        // given
        given(userRepository.existsById(user.getId())).willReturn(true);
        given(reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(
                user, TargetType.COMMENT, targetId, ReactionType.LIKE)).willReturn(false);
        given(commentRepository.existsById(targetId)).willReturn(false);

        // then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                reactionService.addReaction(user, TargetType.COMMENT, targetId, ReactionType.LIKE)
        );
        assertEquals("해당 댓글은 존재하지 않습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("addReaction - 타겟 유저 없음 예외")
    void addReactionNotTargetUser() {
        // given
        given(userRepository.existsById(user.getId())).willReturn(true);
        given(reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(
                user, TargetType.USER, targetId, ReactionType.FOLLOW)).willReturn(false);
        given(userRepository.existsById(targetId)).willReturn(false);

        // then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                reactionService.addReaction(user, TargetType.USER, targetId, ReactionType.FOLLOW)
        );
        assertEquals("해당 유저는 존재하지 않습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("addReaction - 자신이나 자신글 제외 예외")
    void addReactionOwnReaction() {
        // given
        given(userRepository.existsById(user.getId())).willReturn(true);
        given(reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(
                user, TargetType.POST, targetId, ReactionType.LIKE)).willReturn(false);
        given(postRepository.existsById(targetId)).willReturn(true);

        Post post = new Post();
        post.setUser(user);
        given(postRepository.findById(targetId)).willReturn(Optional.of(post));

        // then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                reactionService.addReaction(user, TargetType.POST, targetId, ReactionType.LIKE)
        );
        assertEquals("자신이나 자신의 글에 반응할 수 없습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("addReaction - 팔로우 성공")
    void followSuccess() {
        // given
        given(userRepository.existsById(user.getId())).willReturn(true);
        given(reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(
                user, TargetType.USER, targetId, ReactionType.FOLLOW)).willReturn(false);
        given(userRepository.existsById(targetId)).willReturn(true);
        given(userRepository.findById(targetId)).willReturn(Optional.of(targetUser));

        // when
        reactionService.addReaction(user, TargetType.USER, targetId, ReactionType.FOLLOW);

        // then
        verify(reactionRepository).save(any(Reaction.class));
        verify(notificationService).sendNotification(user, targetUser, NotificationType.FOLLOW, targetId);
    }

    @Test
    @DisplayName("addReaction - 게시글 좋아요 성공")
    void postLikeSuccess() {
        // given
        given(userRepository.existsById(user.getId())).willReturn(true);
        given(reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(
                user, TargetType.POST, targetId, ReactionType.LIKE)).willReturn(false);
        given(postRepository.existsById(targetId)).willReturn(true);

        Post post = new Post();
        post.setUser(targetUser);
        given(postRepository.findById(targetId)).willReturn(Optional.of(post));

        // when
        reactionService.addReaction(user, TargetType.POST, targetId, ReactionType.LIKE);

        // then
        verify(reactionRepository).save(any(Reaction.class));
        verify(notificationService).sendNotification(user, targetUser, NotificationType.LIKE_POST, targetId);
    }

    @Test
    @DisplayName("addReaction - 댓글 좋아요 성공")
    void commentLikeSuccess() {
        // given
        given(userRepository.existsById(user.getId())).willReturn(true);
        given(reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(
                user, TargetType.COMMENT, targetId, ReactionType.LIKE)).willReturn(false);
        given(commentRepository.existsById(targetId)).willReturn(true);

        Comment comment = new Comment();
        comment.setUser(targetUser);
        given(commentRepository.findById(targetId)).willReturn(Optional.of(comment));

        // when
        reactionService.addReaction(user, TargetType.COMMENT, targetId, ReactionType.LIKE);

        // then
        verify(reactionRepository).save(any(Reaction.class));
        verify(notificationService).sendNotification(user, targetUser, NotificationType.LIKE_COMMENT, targetId);
    }

    /**
     * deleteReaction test
     */
    @Test
    @DisplayName("deleteReaction - 유저 없을 시 예외")
    void deleteReactionNotUser() {
        // given
        given(userRepository.existsById(user.getId())).willReturn(false);

        // then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                reactionService.deleteReaction(user, TargetType.POST, targetId, ReactionType.LIKE)
        );
        assertEquals("해당 유저는 존재하지 않습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("deleteReaction - 삭제 대상 리액션 없을 시 예외 발생")
    void deleteReactionNotFound() {
        // given
        given(userRepository.existsById(user.getId())).willReturn(true);
        given(reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(user, TargetType.POST, targetId, ReactionType.LIKE))
                .willReturn(false);

        // then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                reactionService.deleteReaction(user, TargetType.POST, targetId, ReactionType.LIKE)
        );
        assertEquals("삭제할 리액션이 존재하지 않습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("deleteReaction - 성공")
    void deleteReactionSuccess() {
        // given
        given(userRepository.existsById(user.getId())).willReturn(true);
        given(reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(user, TargetType.POST, targetId, ReactionType.LIKE))
                .willReturn(true);


        // when
        reactionService.deleteReaction(user, TargetType.POST, targetId, ReactionType.LIKE);

        // then
        verify(reactionRepository).deleteByUserAndTargetTypeAndTargetIdAndReactionType(
                user, TargetType.POST, targetId, ReactionType.LIKE);
    }

    /**
     * isReactionExists test
     */
    @Test
    @DisplayName("isReactionExists - 유저 없을 시 예외")
    void isReactionExistsNotUser() {
        // given
        given(userRepository.existsById(user.getId())).willReturn(false);

        // then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                reactionService.isReactionExists(user, TargetType.POST, targetId, ReactionType.LIKE)
        );
        assertEquals("해당 유저는 존재하지 않습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("isReactionExists - 존재할 시 true")
    void isReactionExistsTrue() {
        // given
        given(userRepository.existsById(user.getId())).willReturn(true);
        given(reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(
                user, TargetType.POST, targetId, ReactionType.LIKE)).willReturn(true);

        // when
        boolean exists = reactionService.isReactionExists(user, TargetType.POST, targetId, ReactionType.LIKE);

        // then
        assertTrue(exists);
    }

    @Test
    @DisplayName("isReactionExists - 존재안 할 시 false")
    void isReactionExistsFalse() {
        // given
        given(userRepository.existsById(user.getId())).willReturn(true);
        given(reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(
                user, TargetType.POST, targetId, ReactionType.LIKE)).willReturn(false);

        // when
        boolean exists = reactionService.isReactionExists(user, TargetType.POST, targetId, ReactionType.LIKE);

        // then
        assertFalse(exists);
    }
}