package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.repository.CommentRepository;
import com.percent99.OutSpecs.repository.PostRepository;
import com.percent99.OutSpecs.repository.ReactionRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    /**
     * 반응 추가 (좋아요, 북마크, 팔로우, 신고)
     * @param user
     * @param targetType
     * @param targetId
     * @param reactionType
     */
    public void addReaction(User user, TargetType targetType, Long targetId, ReactionType reactionType) {
        if(!userRepository.existsById(user.getId())) {
            throw new EntityNotFoundException("해당 유저는 존재하지 않습니다.");
        }
        if(reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(user, targetType, targetId, reactionType)) {
            throw new EntityExistsException("이미 반응이 존재합니다.");
        }
        if(targetType.equals(TargetType.POST) && !postRepository.existsById(targetId)) {
            throw new EntityNotFoundException("해당 게시물은 존재하지 않습니다.");
        } else if (targetType.equals(TargetType.COMMENT) && !commentRepository.existsById(targetId)) {
            throw new EntityNotFoundException("해당 댓글은 존재하지 않습니다.");
        } else if(targetType.equals(TargetType.USER) && !userRepository.existsById(targetId)) {
            throw new EntityNotFoundException("해당 유저는 존재하지 않습니다.");
        }
        User receiver = findTargetUser(targetType, targetId);
        if(Objects.equals(receiver.getId(), user.getId())) {
            throw new IllegalArgumentException("자신이나 자신의 글에 반응할 수 없습니다.");
        }

        Reaction reaction = new Reaction();
        reaction.setUser(user);
        reaction.setTargetType(targetType);
        reaction.setTargetId(targetId);
        reaction.setReactionType(reactionType);

        reactionRepository.save(reaction);

        // 알림 발송
        if(reaction.getReactionType().equals(ReactionType.FOLLOW)) {
            notificationService.sendNotification(user, receiver, NotificationType.FOLLOW, targetId);
        } else if(reaction.getReactionType().equals(ReactionType.LIKE) && reaction.getTargetType().equals(TargetType.POST)) {
            notificationService.sendNotification(user, receiver, NotificationType.LIKE_POST, targetId);
        } else if(reaction.getReactionType().equals(ReactionType.LIKE) && reaction.getTargetType().equals(TargetType.COMMENT)) {
            notificationService.sendNotification(user, receiver, NotificationType.LIKE_COMMENT, targetId);
        }

    }

    /**
     * target의 user를 찾는 메서드
     * @param type target type(post, user, comment)
     * @param targetId taregt id
     * @return target user
     */
    public User findTargetUser(TargetType type, Long targetId) {
        User user;

        if (type.equals(TargetType.USER)) {
            user = userRepository.findById(targetId)
                    .orElseThrow(() -> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));
        } else if (type.equals(TargetType.POST)) {
            Post post = postRepository.findById(targetId)
                    .orElseThrow(() -> new EntityNotFoundException("해당 게시글이 존재하지 않습니다."));
            user = Optional.ofNullable(post.getUser())
                    .orElseThrow(() -> new IllegalStateException("해당 게시글에 작성자가 없습니다."));
        } else {
            Comment comment = commentRepository.findById(targetId)
                    .orElseThrow(() -> new EntityNotFoundException("해당 댓글이 존재하지 않습니다."));
            user = Optional.ofNullable(comment.getUser())
                    .orElseThrow(() -> new IllegalStateException("해당 댓글의 작성자가 없습니다."));
        }

        return user;
    }

    /**
     * 반응 삭제
     * @param user
     * @param targetType
     * @param targetId
     * @param reactionType
     */
    @Transactional
    public void deleteReaction(User user, TargetType targetType, Long targetId, ReactionType reactionType) {
        if(!userRepository.existsById(user.getId())) {
            throw new EntityNotFoundException("해당 유저는 존재하지 않습니다.");
        }
        if(!reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(user, targetType, targetId, reactionType)) {
            throw new EntityNotFoundException("삭제할 리액션이 존재하지 않습니다.");
        }
        reactionRepository.deleteByUserAndTargetTypeAndTargetIdAndReactionType(user, targetType, targetId, reactionType);
    }

    /**
     * 이미 반응했는지 확인
     * @param user
     * @param targetType
     * @param targetId
     * @param reactionType
     * @return 반응했는지 true, false
     */
    public boolean isReactionExists(User user, TargetType targetType, Long targetId, ReactionType reactionType) {
        if(!userRepository.existsById(user.getId())) {
            throw new EntityNotFoundException("해당 유저는 존재하지 않습니다.");
        }
        return reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(user, targetType, targetId, reactionType);
    }

    /**
     * 해당 타겟에 해당 리액션의 수
     * @param targetType
     * @param targetId
     * @param reactionType
     * @return 반응수
     */
    public int countReactions(TargetType targetType, Long targetId, ReactionType reactionType) {
        return (int)reactionRepository.countByTargetTypeAndTargetIdAndReactionType(targetType, targetId, reactionType);
    }

    /**
     * User가 좋아요한 Post 목록 찾기
     * @param user 
     * @return user가 좋아요한 Post 목록
     */
    public List<Post> getLikedPosts(User user) {
        if(!userRepository.existsById(user.getId())){
            throw new EntityNotFoundException("해당 유저는 존재하지않습니다.");
        }
        List<Long> postIds = reactionRepository.findLikedPostIdsByUser(user);
        return postRepository.findAllById(postIds);
    }

    /**
     * User가 follow한 User 목록 찾기
     * @param user
     * @return User가 follow한 User 목록
     */
    public List<User> getFollowedUsers(User user) {
        if(!userRepository.existsById(user.getId())) {
            throw new EntityNotFoundException("해당 유저는 존재하지 않습니다.");
        }
        List<Long> userIds = reactionRepository.findFollowedUserIds(user);
        return userRepository.findAllById(userIds);
    }

    /**
     * User가 북마크한 Post 목록 찾기
     * @param user
     * @return User가 북마크한 Post 목록
     */
    public List<Post> getBookmarkedPosts(User user) {
        if(!userRepository.existsById(user.getId())) {
            throw new EntityNotFoundException("해당 유저는 존재하지 않습니다.");
        }
        List<Long> postIds = reactionRepository.findBookmarkedPostIdsByUser(user);
        return postRepository.findAllById(postIds);
    }

    /**
     * 신고 당한 모든 게시글 찾기
     * @return 신고당한 모든 게시글 + user정보
     */
    public List<Post> getReportPosts() {
        return reactionRepository.findReportedPostsWithUser(TargetType.POST, ReactionType.REPORT);
    }
}