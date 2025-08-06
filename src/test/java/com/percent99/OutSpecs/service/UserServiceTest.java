package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.UserDTO;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.entity.UserRoleType;
import com.percent99.OutSpecs.repository.ProfileRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 유저 서비스 테스트 클래스
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ProfileRepository profileRepository;

    @InjectMocks private UserService userService;
    private UserDTO userDTO;

    /**
     * 테스트 전 공통 UserDTO를 초기화
     */
    @BeforeEach
    void setUp(){
        userDTO = new UserDTO();
        userDTO.setUsername("test@naver.com");
        userDTO.setPassword("test1234@");
        userDTO.setRole(UserRoleType.USER);
        userDTO.setAiRateLimit(10);
    }

    /**
     * registerUser 메서드가 정상 동작하여 사용자 저장 후 반환하는지 검증
     */
    @Test
    @DisplayName("registerUser 성공")
    void registerUser_success(){

        // when
        when(userRepository.existsByUsername(userDTO.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(userDTO.getPassword())).thenReturn("encrypted");
        User saved = new User();
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = userService.registerUser(userDTO);

        // then
        verify(userRepository).save(any(User.class));
        assertThat(result).isSameAs(saved);
    }

    /**
     * 중복된 username일 때 EntityExistsException을 던지는지 검증
     */
    @Test
    @DisplayName("폼 회원가입에서 구글 로그인 예외")
    void registerUser_withProviderId_throwIllegalArgument(){

        // given
        userDTO.setProviderId("google-123");

        // when / then
        assertThatThrownBy(() -> userService.registerUser(userDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("폼 회원가입에서는 구글 로그인을 사용할 수 없습니다.");
    }

    /**
     * findByUsername 호출 시 사용자가 있으면 Optional이 Present 상태인지 검증
     */
    @Test
    @DisplayName("중복 유저네임 예외")
    void registerUser_duplicateUsername() {

        // given
        when(userRepository.existsByUsername(userDTO.getUsername())).thenReturn(true);

        // when/ then
        assertThatThrownBy(() -> userService.registerUser(userDTO))
                .isInstanceOf(EntityExistsException.class)
                .hasMessage("이미 존재하는 회원입니다.");

        verify(userRepository).existsByUsername(userDTO.getUsername());
    }

    /**
     * findByUsername 호출 시 사용자가 없으면 Optional이 Empty 상태인지 검증
     */
    @Test
    @DisplayName("findByUsername: 존재O")
    void findByUsername_found() {

        // when
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(new User()));

        // then
        assertThat(userService.findByUsername("u")).isPresent();
        verify(userRepository).findByUsername("u");
    }

    @Test
    @DisplayName("findByUsername: 존재X")
    void findByUsername_notFound() {

        // when
        when(userRepository.findByUsername("u")).thenReturn(Optional.empty());

        // then
        assertThat(userService.findByUsername("u")).isEmpty();
        verify(userRepository).findByUsername("u");
    }

    /**
     * 유저 정보 업데이트 정상 흐름을 검증
     * <p>
     * 기존 사용자 조회 후 필드 변경 및 save 호출 확인
     */
    @Test
    @DisplayName("updateUser 성공")
    void updateUser_success() {

        // given
        userDTO.setUsername("u");
        userDTO.setRole(UserRoleType.ADMIN);
        userDTO.setAiRateLimit(10);

        User existing = new User();
        existing.setProviderId(null);

        when(userRepository.findByUsername("u")).thenReturn(Optional.of(existing));

        when(userRepository.save(any(User.class))).thenReturn(existing);

        // when
        User result = userService.updateUser(userDTO);

        // then
        assertThat(result.getRole()).isEqualTo(UserRoleType.ADMIN);
        verify(userRepository).save(any(User.class));
    }

    /**
     * 업데이트 할 유저가 없을 경우 EntityNotFoundException을 던지는지 검증
     */
    @Test
    @DisplayName("updateUser: 유저 없음 예외")
    void updateUser_userNotFound() {

        // when /then
        when(userRepository.findByUsername("test@naver.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.updateUser(userDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 유저는 존재하지 않습니다.");
    }

    /**
     * 업데이트 중 비밀번호가 짧을 경우 IllegalArgumentException을 던지는지 검증
     */
    /*
    @Test
    @DisplayName("updateUser: 비밀번호 짧음 예외")
    void updateUser_shortPassword() {

        // given
        userDTO.setUsername("u");
        userDTO.setPassword("12");
        userDTO.setAiRateLimit(null);

        User existing = new User();
        existing.setProviderId(null);

        // when
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(existing));

        // then
        assertThatThrownBy(() -> userService.updateUser(userDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호는 3자리 이상이어야 합니다.");
    }
     */

    /**
     * 앨런 AI 횟수 차감 정상 흐름을 검증
     * <p>
     * 기존 aiRateLimit에서 1 감소 및 save 호출 확인
     */
    @Test
    @DisplayName("decrementAiRateLimit 성공")
    void decrementAiRateLimit_success() {

        // given
        User u = new User();
        u.setAiRateLimit(5);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        // when
        userService.decrementAiRateLimit(1L);

        // then
        assertThat(u.getAiRateLimit()).isEqualTo(4);
        verify(userRepository).save(any(User.class));
    }

    /**
     * 앨런 AI 횟수 차감 중 사용자가 없을 경우 EntityNotFoundException을 던지는지 검증
     */
    @Test
    @DisplayName("decrementAiRateLimit: 유저 없음 예외")
    void decrementAiRateLimit_userNotFound() {

        // when / then
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.decrementAiRateLimit(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 유저는 존재하지 않습니다.");
    }

    /**
     * 오픈프로필 삭제 정상 흐름을 검증
     * <p>
     * profileRepository.deleteByUserId 및 userRepository.delete 호출 확인
     */
    @Test
    @DisplayName("deleteUserAndProfile 성공")
    void deleteUserAndProfile_success() {

        // given
        User u = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        // when
        userService.deleteUserAndProfile(1L);

        // then
        verify(profileRepository).deleteByUserId(1L);
        verify(userRepository).delete(any(User.class));
    }

    /**
     * 오픈프로필 삭제 중 대상 사용자가 없을 경우 EntityNotFoundException을 던지는지 검증
     */
    @Test
    @DisplayName("deleteUserAndProfile: 유저 없음 예외")
    void deleteUserAndProfile_userNotFound() {

        // when / then
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.deleteUserAndProfile(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 유저는 존재하지 않습니다.");
    }
}