package com.percent99.OutSpecs.dto;

import com.percent99.OutSpecs.entity.ParticipationStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * 팀 모집 참여 신청 상태를 변경하기 위한 데이터 전송 객체(DTO)
 * <ul>
 *     <li>status : 변경할 신청 상태 (PENDING, ACCEPTED, REJECTED)</li>
 * </ul>
 */
@Setter
@Getter
public class ParticipationDTO {

    private ParticipationStatus status;
}