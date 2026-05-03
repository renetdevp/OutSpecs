package com.percent99.OutSpecs.repository;

public interface ProfileRepositoryCustom {
  boolean existsByUserId(Long userId);
  boolean existsByNickname(String nickname);
  boolean nicknameAlreadyExists(String nickname, Long userId);
}
