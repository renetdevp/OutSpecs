package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.QReaction;
import com.percent99.OutSpecs.entity.ReactionType;
import com.percent99.OutSpecs.entity.TargetType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;

import static com.querydsl.core.group.GroupBy.groupBy;

@Repository
@RequiredArgsConstructor
public class ReactionRepositoryImpl implements ReactionRepositoryCustom {
  private final JPAQueryFactory queryFactory;
  private static final QReaction qReaction = QReaction.reaction;

  @Override
  public Map<Long, Long> countReaction(Collection<Long> postIds, TargetType targetType, ReactionType reactionType) {
    return queryFactory.from(qReaction)
            .select(qReaction.targetId, qReaction.count())
            .where(
                    qReaction.targetType.eq(targetType),
                    idsIn(postIds),
                    qReaction.reactionType.eq(reactionType)
            )
            .groupBy(qReaction.targetId)
            .transform(groupBy(qReaction.targetId).as(qReaction.count()));
  }

  // postIds.size()가 1일 경우, hibernate에서 자동으로 WHERE a IN b 구문을 WHERE a = b.elem 쿼리로 변경해줌
  private BooleanExpression idsIn(Collection<Long> postIds){
    if (postIds.isEmpty()) return null;

    return qReaction.targetId.in(postIds);
  }
}
