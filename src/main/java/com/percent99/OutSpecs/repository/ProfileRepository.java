package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUserId(Long userId);
    void deleteByUserId(Long userId);

    /**
     * 닉네임 중복체크
     * @param nickname
     * @return
     */
    boolean existsByNickname(String nickname);

    /**
     * 프로필 존재여부 확인
     * @param userId
     * @return
     */
    boolean existsByUserId(Long userId);

    /**
     * 닉네임 중복체크(자기자신 제외)
     * @param nickname
     * @param userId
     * @return
     */
    boolean existsByNicknameAndUserIdNot(String nickname, Long userId);
}
