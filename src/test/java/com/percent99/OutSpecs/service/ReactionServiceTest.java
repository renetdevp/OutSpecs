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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
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
    @DisplayName("addReaction - 이미 반응 존재시 삭제 호출")
    void AlreadyExists() {
        // given
        given(userRepository.existsById(user.getId())).willReturn(true);
        given(reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(
                user, TargetType.POST, targetId, ReactionType.LIKE)).willReturn(true);

        // when
        reactionService.addReaction(user, TargetType.POST, targetId, ReactionType.LIKE);

        // then - deleteReaction이 호출되어야 함
        verify(reactionRepository).deleteByUserAndTargetTypeAndTargetIdAndReactionType(
                user, TargetType.POST, targetId, ReactionType.LIKE);
        // save는 호출되지 않아야 함
        verify(reactionRepository, never()).save(any(Reaction.class));
        // 알림도 발송되지 않아야 함
        verify(notificationService, never()).sendNotification(any(), any(), any(), any());
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

    @Test
    @DisplayName("addReaction - 북마크는 알림 발송 안함")
    void bookmarkNoNotification() {
        // given
        given(userRepository.existsById(user.getId())).willReturn(true);
        given(reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(
                user, TargetType.POST, targetId, ReactionType.BOOKMARK)).willReturn(false);
        given(postRepository.existsById(targetId)).willReturn(true);

        Post post = new Post();
        post.setUser(targetUser);
        given(postRepository.findById(targetId)).willReturn(Optional.of(post));

        // when
        reactionService.addReaction(user, TargetType.POST, targetId, ReactionType.BOOKMARK);

        // then
        verify(reactionRepository).save(any(Reaction.class));
        verify(notificationService, never()).sendNotification(any(), any(), any(), any());
    }

    /**
     * findTargetUser test
     */
    @Test
    @DisplayName("findTargetUser - USER 타입 성공")
    void findTargetUserTypeUser() {
        // given
        given(userRepository.findById(targetId)).willReturn(Optional.of(targetUser));

        // when
        User result = reactionService.findTargetUser(TargetType.USER, targetId);

        // then
        assertEquals(targetUser, result);
    }

    @Test
    @DisplayName("findTargetUser - POST 타입 성공")
    void findTargetUserTypePost() {
        // given
        Post post = new Post();
        post.setUser(targetUser);
        given(postRepository.findById(targetId)).willReturn(Optional.of(post));

        // when
        User result = reactionService.findTargetUser(TargetType.POST, targetId);

        // then
        assertEquals(targetUser, result);
    }

    @Test
    @DisplayName("findTargetUser - COMMENT 타입 성공")
    void findTargetUserTypeComment() {
        // given
        Comment comment = new Comment();
        comment.setUser(targetUser);
        given(commentRepository.findById(targetId)).willReturn(Optional.of(comment));

        // when
        User result = reactionService.findTargetUser(TargetType.COMMENT, targetId);

        // then
        assertEquals(targetUser, result);
    }

    @Test
    @DisplayName("findTargetUser - POST의 작성자가 null일 때 예외")
    void findTargetUserPostUserNull() {
        // given
        Post post = new Post();
        post.setUser(null);
        given(postRepository.findById(targetId)).willReturn(Optional.of(post));

        // then
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                reactionService.findTargetUser(TargetType.POST, targetId)
        );
        assertEquals("해당 게시글에 작성자가 없습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("findTargetUser - COMMENT의 작성자가 null일 때 예외")
    void findTargetUserCommentUserNull() {
        // given
        Comment comment = new Comment();
        comment.setUser(null);
        given(commentRepository.findById(targetId)).willReturn(Optional.of(comment));

        // then
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                reactionService.findTargetUser(TargetType.COMMENT, targetId)
        );
        assertEquals("해당 댓글의 작성자가 없습니다.", ex.getMessage());
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

    /**
     * countReactions test
     */
    @Test
    @DisplayName("countReactions - 반응 수 조회 성공")
    void countReactions() {
        // given
        given(reactionRepository.countByTargetTypeAndTargetIdAndReactionType(
                TargetType.POST, targetId, ReactionType.LIKE)).willReturn(5L);

        // when
        int count = reactionService.countReactions(TargetType.POST, targetId, ReactionType.LIKE);

        // then
        assertEquals(5, count);
    }

    /**
     * getLikedPosts test
     */
    @Test
    @DisplayName("getLikedPosts - 유저 없을 시 예외")
    void getLikedPostsNotUser() {
        // given
        given(userRepository.existsById(user.getId())).willReturn(false);

        // then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                reactionService.getLikedPosts(user)
        );
        assertEquals("해당 유저는 존재하지않습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("getLikedPosts - 좋아요한 게시글 조회 성공")
    void getLikedPostsSuccess() {
        // given
        given(userRepository.existsById(user.getId())).willReturn(true);
        List<Long> postIds = Arrays.asList(1L, 2L, 3L);
        given(reactionRepository.findLikedPostIdsByUser(user)).willReturn(postIds);

        List<Post> posts = Arrays.asList(new Post(), new Post(), new Post());
        given(postRepository.findAllById(postIds)).willReturn(posts);

        // when
        List<Post> result = reactionService.getLikedPosts(user);

        // then
        assertEquals(3, result.size());
        verify(reactionRepository).findLikedPostIdsByUser(user);
        verify(postRepository).findAllById(postIds);
    }

    /**
     * isFollowing test
     */
    @Test
    @DisplayName("isFollowing - 유저가 null일 때 false")
    void isFollowingUserNull() {
        // when
        boolean result = reactionService.isFollowing(null, targetId);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("isFollowing - 자기 자신을 팔로우할 때 false")
    void isFollowingSelf() {
        // when
        boolean result = reactionService.isFollowing(user, user.getId());

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("isFollowing - 팔로우 중일 때 true")
    void isFollowingTrue() {
        // given
        given(reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(
                user, TargetType.USER, targetId, ReactionType.FOLLOW)).willReturn(true);

        // when
        boolean result = reactionService.isFollowing(user, targetId);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("isFollowing - 팔로우 안하고 있을 때 false")
    void isFollowingFalse() {
        // given
        given(reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(
                user, TargetType.USER, targetId, ReactionType.FOLLOW)).willReturn(false);

        // when
        boolean result = reactionService.isFollowing(user, targetId);

        // then
        assertFalse(result);
    }
}