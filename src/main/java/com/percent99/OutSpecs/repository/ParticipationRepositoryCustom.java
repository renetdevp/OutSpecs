package com.percent99.OutSpecs.repository;

public interface ParticipationRepositoryCustom {
  interface ParticipationCountDTO {
    long totalCount();
    long userCount();
  }

  ParticipationCountDTO getParticipationCount(long postId, long userId);
}
