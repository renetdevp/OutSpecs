package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {
  private static final QPost qPost = QPost.post;
  private static final QReaction qReaction = QReaction.reaction;
  private final JPAQueryFactory queryFactory;

  public List<Post> searchLikeDesc(PostType postType, int limit){
    return queryFactory.selectFrom(qPost)
            .leftJoin(qReaction).on(
                    qReaction.targetId.eq(qPost.id),
                    isPostLike()
            )
            .where(qPost.type.eq(postType))
            .groupBy(qPost.id)
            .orderBy(qReaction.count().desc().nullsLast())
            .limit(limit)
            .fetch();
  }

  @Override
  public List<Long> searchRecruitByTech(List<String> techs) {
    final QPostJob qPostJob = new QPostJob("job");
    final QTechniques qTech = new QTechniques("tech");

    List<Post> result = queryFactory.selectFrom(qPost)
            .leftJoin(qPost.postJob, qPostJob).fetchJoin()
            .leftJoin(qPost.postJob.techniques, qTech).fetchJoin()
            .where(qPost.type.eq(PostType.RECRUIT),
                    qTech.tech.in(techs))
            .fetch();

    return result.stream()
            .map(Post::getId)
            .toList();
  }

  @Override
  public List<Long> searchHangoutByPlace(String place) {
    final QPostHangout qHangout = new QPostHangout("hangout");

    List<Post> result = queryFactory.selectFrom(qPost)
            .leftJoin(qPost.postHangout, qHangout).fetchJoin()
            .where(qPost.type.eq(PostType.PLAY),
                    qHangout.placeName.eq(place))
            .fetch();

    return result.stream()
            .map(Post::getId)
            .toList();
  }

  @Override
  public List<Post> searchTeamByStatus(PostStatus postStatus) {
    final QPostTeamInformation qTeamInfo = new QPostTeamInformation("teaminfo");

    return queryFactory.selectFrom(qPost)
            .leftJoin(qPost.teamInfo, qTeamInfo).fetchJoin()
            .where(qPost.type.eq(PostType.TEAM),
                    qTeamInfo.status.eq(postStatus))
            .fetch();
  }

  @Override
  public Post searchPostDetail(Long postId) {
    return queryFactory.selectFrom(qPost)
            .leftJoin(qPost.user).fetchJoin()
            .leftJoin(qPost.postHangout).fetchJoin()
            .leftJoin(qPost.postJob).fetchJoin()
            .leftJoin(qPost.postQnA).fetchJoin()
            .leftJoin(qPost.teamInfo).fetchJoin()
            .leftJoin(qPost.postTags).fetchJoin()
            .where(qPost.id.eq(postId))
            .fetchFirst();
  }

  private BooleanExpression isPostLike(){
    return qReaction.targetType.eq(TargetType.POST)
            .and(qReaction.reactionType.eq(ReactionType.LIKE));
  }

  @Override
  public Post searchPostWithTeamAndAuthorAndLock(Long postId) {
    return queryFactory.selectFrom(qPost)
            .leftJoin(qPost.teamInfo).fetchJoin()
            .leftJoin(qPost.user).fetchJoin()
            .where(qPost.id.eq(postId))
            .setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
            .fetchFirst();
  }
}
