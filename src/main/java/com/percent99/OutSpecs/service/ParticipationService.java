package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.ParticipationDTO;
import com.percent99.OutSpecs.entity.Participation;

import java.util.List;

/**
 * Participation 엔티티 관련 생성·조회·삭제·수정 기능을 제공하는 서비스 인터페이스
 */
public interface ParticipationService {

    /**
     * 새로운 팀 모집 신청을 생성한다.
     * @param userId 참여할 사용자의 ID
     * @param postId 참여할 게시글의 ID
     * @return 생성된 participation 엔티티
     */
    Participation createParticipation(Long userId, Long postId);

    /**
     * ID로 Participation 정보를 조회한다.
     * @param id 조회할 Participation의 ID
     * @return  조회된 participation 엔티티
     */
    Participation getParticipationById(Long id);

    /**
     * ID로 Participation 정보를 수정한다.
     * @param id 수정할 Participation의 ID
     * @param dto 수정할 내용이 담긴 DTO
     * @return 수정된 Participation 엔티티
     */
    Participation updateParticipation(Long id, ParticipationDTO dto);

    /**
     * 특정 게시글의 모든 Participation 정보를 조회한다.
     * @param postId 조회할 게시글의 ID
     * @return 해당 게시글에 대한 Participation 리스트
     */
    List<Participation> getParticipationsByPostId(Long postId);

    /**
     * 특정 사용자가 참여한 모든 Participation 정보를 조회한다.
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자의 Participation 리스트
     */
    List<Participation> getParticipationsByUserId(Long userId);

    /**
     * ID로 Participation 정보를 삭제한다.
     * @param id 삭제할 Participation의 ID
     */
    void deleteParticipation(Long id);
}