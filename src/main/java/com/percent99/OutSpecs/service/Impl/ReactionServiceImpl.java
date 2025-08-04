package com.percent99.OutSpecs.service.Impl;

import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.repository.ReactionRepository;
import com.percent99.OutSpecs.service.ReactionService;
import jakarta.persistence.EntityExistsException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReactionServiceImpl implements ReactionService {

    private final ReactionRepository reactionRepository;

    public ReactionServiceImpl(ReactionRepository reactionRepository) {
        this.reactionRepository = reactionRepository;
    }

    /**
     * 반응 추가 (좋아요, 북마크, 팔로우, 신고)
     * @param user
     * @param targetType
     * @param targetId
     * @param reactionType
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
    public int countReactions(TargetType targetType, Long targetId, ReactionType reactionType) {
        return (int)reactionRepository.countByTargetTypeAndTargetIdAndReactionType(targetType, targetId, reactionType);
    }

    @Override
    public List<Long> getFollowedUserIds(User user) {

    }

    @Override
    public List<Post> getBookMarkPosts(User user) {

    }
}
