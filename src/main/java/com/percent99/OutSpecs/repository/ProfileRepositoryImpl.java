package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.QProfile;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProfileRepositoryImpl implements ProfileRepositoryCustom {
  private final JPAQueryFactory queryFactory;
  private static final QProfile qProfile = QProfile.profile;

  @Override
  public boolean existsByUserId(Long userId) {
    return queryFactory.selectFrom(qProfile)
            .where(qProfile.userId.eq(userId))
            .fetchFirst() != null;
  }
}
