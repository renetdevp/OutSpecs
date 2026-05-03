package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.ParticipationDTO;
import com.percent99.OutSpecs.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParticipateFacadeService {
  private final ParticipationService participationService;

  @Retryable(
          retryFor = ObjectOptimisticLockingFailureException.class,
          maxAttempts = 3,
          backoff = @Backoff(delay = 100)
  )
  public Participation retryableParticipate(ParticipationDTO dto, User user){
    return participationService.participate(dto, user);
  }

  @Recover
  public Participation recover(ObjectOptimisticLockingFailureException e, ParticipationDTO dto, User user){
    throw new IllegalStateException("팀 모집 참가 신청에 실패했습니다.");
  }
}
