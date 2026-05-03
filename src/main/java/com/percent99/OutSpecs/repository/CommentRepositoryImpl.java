package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.CommentType;
import com.percent99.OutSpecs.entity.QComment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;

import static com.querydsl.core.group.GroupBy.groupBy;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {
  private final JPAQueryFactory queryFactory;
  private static final QComment qComment = QComment.comment;

  @Override
  public Map<Long, Long> countComments(Collection<Long> postIds, CommentType commentType) {
    return queryFactory.from(qComment)
            .select(qComment.parentId, qComment.count())
            .where(qComment.type.eq(commentType),
                    qComment.parentId.in(postIds))
            .groupBy(qComment.parentId)
            .transform(groupBy(qComment.parentId).as(qComment.count()));
  }
}
