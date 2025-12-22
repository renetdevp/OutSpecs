package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.CommentType;

import java.util.Collection;
import java.util.Map;

public interface CommentRepositoryCustom {
  Map<Long, Long> countComments(Collection<Long> postIds, CommentType commentType);
}
