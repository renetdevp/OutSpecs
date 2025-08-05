package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Participation 엔티티에 대한 데이터 접근 기능을 제공하느 Repository 인터페이스
 * <p>
 *  JpaRepository를 상속받아 기본 CRUD 메서드를 제공하며,
 *  추가로 게시글별, 유저별로 신청현황을 조회한다.
 * </p>
 */
@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    /**
     * 특정 게시글에 등록된 모든 신청현황을 조회한다.
     * @param postId 조회할 게시글의 ID
     * @return 해당 게시글의 신청현황 리스트
     */
    List<Participation> findByPostId(Long postId);

    /**
     * 특정 유저가 등록한 모든 신청현황을 조회한다.
     * @param userId 조회할 유저의 ID
     * @return 해당 유저의 신청현황 리스트
     */
    List<Participation> findByUserId(Long userId);
}