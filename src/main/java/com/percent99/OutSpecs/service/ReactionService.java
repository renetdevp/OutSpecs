package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.ReactionType;
import com.percent99.OutSpecs.entity.TargetType;
import com.percent99.OutSpecs.entity.User;

import java.util.List;

public interface ReactionService {
    void addReaction(User user, TargetType targetType, Long targetId, ReactionType reactionType);
    void deleteReaction(User user, TargetType targetType, Long targetId, ReactionType reactionType);
    boolean isReactionExists(User user, TargetType targetType, Long targetId, ReactionType reactionType);
    int countReactions(TargetType targetType, Long targetId, ReactionType reactionType);
    List<Long> getFollowedUserIds(User user);
    List<Post> getBookMarkPosts(User user);
}
