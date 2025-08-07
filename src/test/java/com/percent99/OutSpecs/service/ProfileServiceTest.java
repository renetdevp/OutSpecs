package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.ProfileDTO;
import com.percent99.OutSpecs.entity.Profile;
import com.percent99.OutSpecs.entity.User;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock private ProfileRepository profileRepository;
    @Mock private UserRepository    userRepository;
    @Mock private S3Service         s3Service;

    @InjectMocks
    private ProfileService profileService;

    private User user;
    private ProfileDTO dto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("tester");

        dto = new ProfileDTO();
        dto.setNickname("nick");
        dto.setStacks("Java,Spring");
        dto.setExperience("5년");
        dto.setSelfInfo("자기소개");
        dto.setAllowCompanyAccess(true);
    }

    @Test
    @DisplayName("getUserById 성공")
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User found = profileService.getUserById(1L);
        assertThat(found).isSameAs(user);
    }

    @Test
    @DisplayName("getUserById - 유저 없음 예외")
    void getUserById_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> profileService.getUserById(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 유저는 존재하지 않습니다.");
    }

    @Test
    @DisplayName("ensureProfileNotExists - 중복 프로필 예외")
    void ensureProfileNotExists_exists() {
        when(profileRepository.existsByUserId(1L)).thenReturn(true);
        assertThatThrownBy(() -> profileService.ensureProfileNotExists(1L))
                .isInstanceOf(EntityExistsException.class)
                .hasMessage("이미 프로필이 존재합니다. userId = 1");
    }

    @Test
    @DisplayName("getProfileByUserId 성공")
    void getProfileByUserId_success() {
        Profile p = new Profile();
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(p));
        Profile found = profileService.getProfileByUserId(1L);
        assertThat(found).isSameAs(p);
    }

    @Test
    @DisplayName("getProfileByUserId - 프로필 없음 예외")
    void getProfileByUserId_notFound() {
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> profileService.getProfileByUserId(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 프로필이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("registerProfile 성공 - 파일 없음")
    void registerProfile_success_noFile() throws IOException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.existsByUserId(1L)).thenReturn(false);
        when(profileRepository.existsByNickname("nick")).thenReturn(false);

        Profile saved = new Profile();
        saved.setUser(user);
        saved.setNickname("nick");
        when(profileRepository.save(any(Profile.class))).thenReturn(saved);

        // file 없이 호출
        Profile result = profileService.registerProfile(1L, dto, null);
        assertThat(result).isSameAs(saved);
    }

    @Test
    @DisplayName("registerProfile - 중복 프로필 예외")
    void registerProfile_exists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.existsByUserId(1L)).thenReturn(true);

        // file 없이 호출
        assertThatThrownBy(() -> profileService.registerProfile(1L, dto, null))
                .isInstanceOf(EntityExistsException.class)
                .hasMessage("이미 프로필이 존재합니다. userId = 1");
    }

    @Test
    @DisplayName("registerProfile - 닉네임 중복 예외")
    void registerProfile_duplicateNickname() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.existsByUserId(1L)).thenReturn(false);
        when(profileRepository.existsByNickname("nick")).thenReturn(true);

        // file 없이 호출
        assertThatThrownBy(() -> profileService.registerProfile(1L, dto, null))
                .isInstanceOf(EntityExistsException.class)
                .hasMessage("이미 사용 중인 닉네임입니다.");
    }

    @Test
    @DisplayName("registerProfile - 유저 없음 예외")
    void registerProfile_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // file 없이 호출
        assertThatThrownBy(() -> profileService.registerProfile(1L, dto, null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 유저는 존재하지 않습니다.");
    }

    @Test
    @DisplayName("updateProfileImage 성공")
    void updateProfileImage_success() throws IOException {
        Profile existing = new Profile();
        existing.setUser(user);
        existing.setImageUrl("old-url");
        existing.setS3Key("oldKey");

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        String newUrl = "http://new-url.png";
        String newKey = "new-url.png";

        when(profileRepository.findByUserId(1L))
                .thenReturn(Optional.of(existing));

        when(s3Service.uploadFile(file)).thenReturn(newUrl);
        when(profileRepository.save(any(Profile.class))).thenReturn(existing);
        doNothing().when(s3Service).deleteFile("oldKey");

        profileService.updateProfileImage(1L, file);

        assertThat(existing.getImageUrl()).isEqualTo(newUrl);
        assertThat(existing.getS3Key()).isEqualTo(newKey);
        verify(profileRepository).save(existing);
        verify(s3Service).deleteFile("oldKey");
    }

    @Test
    @DisplayName("updateProfileImage - 프로필 없음 예외")
    void updateProfileImage_notFound() {
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        assertThatThrownBy(() -> profileService.updateProfileImage(1L, file))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 프로필이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("updateProfile 성공")
    void updateProfile_success() {
        Profile existing = new Profile();
        existing.setUser(user);
        existing.setNickname("old");

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(profileRepository.existsByNicknameAndUserIdNot("nick", 1L)).thenReturn(false);
        when(profileRepository.save(any(Profile.class))).thenReturn(existing);

        Profile updated = profileService.updateProfile(1L, dto);
        assertThat(updated.getNickname()).isEqualTo("nick");
    }

    @Test
    @DisplayName("updateProfile - 프로필 없음 예외")
    void updateProfile_notFound() {
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.updateProfile(1L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 프로필이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("updateProfile - 닉네임 중복 예외")
    void updateProfile_duplicateNickname() {
        Profile existing = new Profile();
        existing.setUser(user);
        existing.setNickname("old");

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(profileRepository.existsByNicknameAndUserIdNot("nick", 1L)).thenReturn(true);

        assertThatThrownBy(() -> profileService.updateProfile(1L, dto))
                .isInstanceOf(EntityExistsException.class)
                .hasMessage("이미 사용 중인 닉네임입니다.");
    }

    @Test
    @DisplayName("updateProfile - 짧은 닉네임 예외")
    void updateProfile_shortNickname() {
        dto.setNickname("a");
        Profile existing = new Profile();
        existing.setUser(user);
        existing.setNickname("old");

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> profileService.updateProfile(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임은 2자 이상이어야 합니다.");
    }

    @Test
    @DisplayName("deleteProfileByUserId 성공")
    void deleteProfileByUserId_success() {
        Profile existing = new Profile();
        existing.setUserId(1L);
        existing.setUser(user);
        existing.setS3Key("key123");

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        doNothing().when(s3Service).deleteFile("key123");

        profileService.deleteProfileByUserId(1L);

        verify(profileRepository).deleteById(1L);
        verify(s3Service).deleteFile("key123");
    }

    @Test
    @DisplayName("deleteProfileByUserId - 프로필 없음 예외")
    void deleteProfileByUserId_notFound() {
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.deleteProfileByUserId(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 프로필이 존재하지 않습니다.");
    }
}