package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.dto.UserDTO;
import com.percent99.OutSpecs.repository.ProfileRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 *  User 정보 등록, 수정, 삭제, 조회 등을 위한 서비스
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    /**
     * 회원가입 정보 저장
     * @param userDTO
     * @return 저장된 user
     */
    public User registerUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new EntityExistsException("이미 존재하는 회원입니다.");
        }
        if(userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
           if(userDTO.getPassword().length() <= 2) {
               throw new IllegalArgumentException("비밀번호는 3자리 이상이어야 합니다.");
           }
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        user.setRole(userDTO.getRole());
        user.setAiRateLimit(userDTO.getAiRateLimit());

        if(userDTO.getProviderId() != null) {
            user.setProviderId(userDTO.getProviderId());
        }
        return userRepository.save(user);
    }

    /**
     * 유저정보 조회
     * @param username
     * @return username으로 찾은 유저정보
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 유저정보 수정
     * @param userDTO
     * @return 수정된 유저정보
     */
    public User updateUser(UserDTO userDTO) {
        User exiting = userRepository.findByUsername(userDTO.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("해당 유저는 존재하지 않습니다."));

        exiting.setRole(userDTO.getRole());
        if(exiting.getProviderId() == null &&
                userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            if(userDTO.getPassword().length() <= 2) {
                throw new IllegalArgumentException("비밀번호는 3자리 이상이어야 합니다.");
            }
            exiting.setPassword(userDTO.getPassword());
        }

        return userRepository.save(exiting);
    }

    /**
     * AI 사용횟수 감소
     * @param userId
     */
    public void decrementAiRateLimit(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저는 존재하지 않습니다."));
        user.setAiRateLimit(user.getAiRateLimit() - 1);
        userRepository.save(user);
    }

    /**
     * 유저정보 삭제
     * @param userId
     */
    public void deleteUserAndProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저는 존재하지 않습니다."));

        profileRepository.deleteByUserId(userId);
        userRepository.delete(user);
    }
}
