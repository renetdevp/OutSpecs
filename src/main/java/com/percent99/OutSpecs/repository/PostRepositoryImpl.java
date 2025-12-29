package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {
  private static final QPost qPost = QPost.post;
  private static final QReaction qReaction = QReaction.reaction;
  private final JPAQueryFactory queryFactory;

  @Override
  public List<Post> searchLikeDesc(PostType postType, int limit) {
    return queryFactory.selectFrom(qPost)
            .where(qPost.type.eq(postType))
            .leftJoin(qReaction).on(
                    qPost.id.eq(qReaction.targetId),
                    isPostLike())
            .fetchJoin()
            .groupBy(qPost)
            .orderBy(qReaction.count().desc())
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
}
