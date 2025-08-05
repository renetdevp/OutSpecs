package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.repository.ReactionRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;

    /**
     * 반응 추가 (좋아요, 북마크, 팔로우, 신고)
     * @param user
     * @param targetType
     * @param targetId
     * @param reactionType
     */
    public void addReaction(User user, TargetType targetType, Long targetId, ReactionType reactionType) {
        if(reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(user, targetType, targetId, reactionType)) {
            throw new EntityExistsException("이미 반응이 존재합니다.");
        }
        Reaction reaction = new Reaction();
        reaction.setUser(user);
        reaction.setTargetType(targetType);
        reaction.setTargetId(targetId);
        reaction.setReactionType(reactionType);

        reactionRepository.save(reaction);
    }

    /**
     * 반응 삭제
     * @param user
     * @param targetType
     * @param targetId
     * @param reactionType
     */
    public void deleteReaction(User user, TargetType targetType, Long targetId, ReactionType reactionType) {
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
     * User가 follow한 targetId 목록 찾기
     * @param user
     * @return User가 follow한 userId 목록
     */
    public List<Long> getFollowedUserIds(User user) {
        return reactionRepository.findFollowedUserIds(user);
    }

    /**
     * User가 북마크한 Post 목록 찾기
     * @param user
     * @return User가 북마크한 Post 목록
     */
    public List<Post> getBookMarkPosts(User user) {
        List<Long> postIds = reactionRepository.findBookmarkedPostIdsByUser(user);
        return postRepository.findAllById(postIds);
    }
}
