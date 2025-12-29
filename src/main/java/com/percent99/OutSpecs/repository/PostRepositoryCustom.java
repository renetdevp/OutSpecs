package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.PostStatus;
import com.percent99.OutSpecs.entity.PostType;

import java.util.List;

public interface PostRepositoryCustom {
  List<Post> searchLikeDesc(PostType postType, int limit);
  List<Long> searchRecruitByTech(List<String> techs);

  List<Long> searchHangoutByPlace(String place);
  List<Post> searchTeamByStatus(PostStatus postStatus);
  Post searchPostDetail(Long postId);
}
