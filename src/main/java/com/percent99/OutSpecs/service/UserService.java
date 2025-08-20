package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.entity.Profile;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.dto.UserDTO;
import com.percent99.OutSpecs.entity.UserRoleType;
import com.percent99.OutSpecs.repository.ProfileRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 *  User 정보 등록, 수정, 삭제, 조회 등을 위한 서비스
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final PasswordEncoder passwordEncoder;
    private final ProfileRepository profileRepository;

    @Value("${chatbot.CHATBOT_USERNAME}")
    private String CHATBOT_USERNAME;

    @Value("${chatbot.CHATBOT_PASSWORD}")
    private String CHATBOT_PASSWORD;

    /**
     * 유저정보 조회
     * @param username
     * @return username으로 찾은 유저정보
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저는 존재하지 않습니다."));
    }

    /**
     * 회원가입 정보 저장
     * @param userDTO 검증은 컨트롤러 단계(@ValidUsername, @ValidPassword)에서 이미 수행됨
     * @return 저장된 user
     */
    @Transactional
    public User registerUser(UserDTO userDTO) {

        if(userDTO.getProviderId() != null && !userDTO.getProviderId().isBlank()) {
           throw new IllegalArgumentException("폼 회원가입에서는 구글 로그인을 사용할 수 없습니다.");
        }

        if (userDTO.getUsername().equals(CHATBOT_USERNAME)){
          throw new IllegalArgumentException("예약된 사용자 이름입니다.");
        }

        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new EntityExistsException("이미 존재하는 회원입니다.");
        }
        final int DEFAULT_AI_RATE_LIMIT = 10;

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(UserRoleType.USER);
        user.setAiRateLimit(DEFAULT_AI_RATE_LIMIT);

        return userRepository.save(user);
    }

    /**
     * 사용자가 자신의 비밀번호를 변경합니다.
     * @param username 사용자 아이디
     * @param newPassword 새로운 비밀번호
     */
    @Transactional
    public void changePassword(String username, String newPassword){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저는 존재하지 않습니다."));

        if(user.getProviderId() != null && !user.getProviderId().isBlank()){
            throw new IllegalArgumentException("소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("새로운 비밀번호를 입력해주세요.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
    }

    /**
     * AI 사용횟수 감소
     * @param userId
     */
    @Transactional
    public void decrementAiRateLimit(Long userId) {

        User user = getUserById(userId);

        int remaining = user.getAiRateLimit() - 1;
        user.setAiRateLimit(Math.max(remaining,0));
    }

    /**
     * 유저정보 삭제
     * @param userId
     */
    public void deleteUserAndProfile(Long userId) {

        String s3Key = deleteUserInTx(userId);

        if(s3Key != null && !s3Key.isBlank()){
            s3Service.deleteFile(s3Key);
        }
    }

    @Transactional
    public String deleteUserInTx(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저는 존재하지않습니다."));

        Profile profile = user.getProfile();
        String s3Key = profile != null ? profile.getS3Key() : null;
        userRepository.delete(user);
        return s3Key;
    }

    @Transactional
    private User createChatbotUser(){
      User user = new User();

      user.setUsername(CHATBOT_USERNAME);
      user.setPassword(passwordEncoder.encode(CHATBOT_PASSWORD));
      user.setRole(UserRoleType.CHATBOT);
      user.setAiRateLimit(0);
      user.setCreatedAt(LocalDateTime.now());

      return userRepository.save(user);
    }

    @Transactional
    private void createChatbotUserProfile(Long chatbotUserId){
        User chatbotUser = userRepository.findById(chatbotUserId).orElse(null);

        Profile chatbotUserProfile = new Profile();

        chatbotUserProfile.setUser(chatbotUser);
        chatbotUserProfile.setStacks("C");
        chatbotUserProfile.setExperience("CHATBOT");
        chatbotUserProfile.setSelfInfo("CHATBOT");
        chatbotUserProfile.setAllowCompanyAccess(false);
        chatbotUserProfile.setNickname("CHATBOT");
        chatbotUserProfile.setCreatedAt(LocalDateTime.now());

        profileRepository.save(chatbotUserProfile);
    }

  /**
   * CHATBOT 사용자가 이미 있다면 그 userId를, CHATBOT 사용자가 없다면 새 CHATBOT 사용자를 생성해 그 userId를 반환하는 메소드
   * @return
   */
  @Transactional
    public Long getOrCreateChatbotUserId(){
        User chatbotUser = userRepository.findByRole(UserRoleType.CHATBOT).orElse(null);

        if (chatbotUser == null){
            chatbotUser = this.createChatbotUser();
        }

        if (!profileRepository.existsByUserId(chatbotUser.getId())){
          this.createChatbotUserProfile(chatbotUser.getId());
        }

        return chatbotUser.getId();
    }
}