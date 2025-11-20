package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long>, ProfileRepositoryCustom {

    Optional<Profile> findByUserId(Long userId);
    void deleteByUserId(Long userId);

    List<Profile> findByUserIdIn(Set<Long> userIds);
}
