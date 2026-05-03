package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.QParticipation;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ParticipationRepositoryImpl implements ParticipationRepositoryCustom {
  private static final QParticipation qParticipation = QParticipation.participation;
  private final JPAQueryFactory queryFactory;

  @Override
  public ParticipationCountDTO getParticipationCount(long postId, long userId) {
    return queryFactory
            .select(
                    Projections.constructor(
                            ParticipationCountDTO.class,
                            qParticipation.count(),
                            getUserParticipationCount(userId)
                    )
            )
            .from(qParticipation)
            .where(qParticipation.post.id.eq(postId))
            .fetchOne();
  }

  private NumberExpression<Long> getUserParticipationCount(long userId){
    return new CaseBuilder()
            .when(qParticipation.user.id.eq(userId))
            .then(1)
            .otherwise((Integer)null)
            .count();
  }
}
