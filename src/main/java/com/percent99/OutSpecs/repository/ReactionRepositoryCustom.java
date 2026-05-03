package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.ReactionType;
import com.percent99.OutSpecs.entity.TargetType;

import java.util.Collection;
import java.util.Map;

public interface ReactionRepositoryCustom {
  Map<Long, Long> countReaction(Collection<Long> postIds, TargetType targetType, ReactionType reactionType);
}
