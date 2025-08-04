package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.entity.UserDTO;

import java.util.Optional;

/**
 *  User 정보 등록, 수정, 삭제, 조회 등을 위한 서비스
 */
public interface UserService {

    User registerUser(UserDTO userDTO);
    Optional<User> findByUsername(String username);
    User updateUser(UserDTO userDTO);
    void decrementAiRateLimit(Long userId);
    void deleteUserAndProfile(Long userId);
}
