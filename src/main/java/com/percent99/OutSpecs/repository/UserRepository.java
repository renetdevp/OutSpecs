package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByUsername(String username);

    /**
     *  회원가입 중복체크
     */
    boolean existsByUsername(String username);

    /**
     * 소셜 로그인
     */
    Optional<User> findByProviderId(String providerId);
}
