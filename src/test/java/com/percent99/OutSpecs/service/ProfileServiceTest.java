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
import static org.mockito.Mockito.*;

/**
 * 오픈프로필 서비스 테스트 클래스
 */
@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock private ProfileRepository profileRepository;
    @Mock private UserRepository userRepository;
    @Mock private S3Service s3Service;

    @InjectMocks
    private ProfileService profileService;

    private ProfileDTO profileDTO;
    private User user;

    /**
     * 테스트 전에 공통으로 사용할 ProfileDTO 및 User 객체 초기화
     */
    @BeforeEach
    void setUp() {
        profileDTO = new ProfileDTO();
        profileDTO.setNickname("tester");
        profileDTO.setStacks("Java, Spring");
        profileDTO.setImageUrl("http://example.com/image.png");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
    }

    /**
     * <h5>프로필 등록 성공 테스트</h5>
     * <p>존재하는 유저와 중복 없는 닉네임 조건 하에 정상 등록 및 저장 호출 확인</p>
     */
    @Test
    @DisplayName("프로필 등록 성공")
    void registerProfile_success() throws IOException {

        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.existsByUserId(1L)).thenReturn(false);
        when(profileRepository.existsByNickname(profileDTO.getNickname())).thenReturn(false);

        Profile savedProfile = new Profile();
        savedProfile.setUser(user);
        savedProfile.setNickname(profileDTO.getNickname());
        when(profileRepository.save(any(Profile.class))).thenReturn(savedProfile);

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);
        // when
        Profile result = profileService.registerProfile(1L, profileDTO, mockFile);

        // then
        assertThat(result).isSameAs(savedProfile);
        verify(profileRepository).save(argThat(p ->
                p.getUser().equals(user) &&
                        p.getNickname().equals(profileDTO.getNickname())
        ));
    }

    /**
     * 프로필 등록 시 유저가 존재하지 않을 경우 예외 발생을 검증
     */
    @Test
    @DisplayName("프로필 등록 - 유저 없음 예외")
    void registerProfile_userNotFound() throws  IOException{

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        MultipartFile mockFile = mock(MultipartFile.class);

        // then
        assertThatThrownBy(() -> profileService.registerProfile(1L, profileDTO, mockFile))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 유저는 존재하지 않습니다.");
    }

    /**
     * 프로필 등록 시 이미 프로필이 존재하는 경우 예외 발생을 검증
     */
    @Test
    @DisplayName("프로필 등록 - 이미 프로필 존재 예외")
    void registerProfile_alreadyExists() throws IOException {

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.existsByUserId(1L)).thenReturn(true);

        MultipartFile mockFile = mock(MultipartFile.class);

        // then
        assertThatThrownBy(() -> profileService.registerProfile(1L, profileDTO, mockFile))
                .isInstanceOf(EntityExistsException.class)
                .hasMessage("이미 존재하는 프로필입니다.");
    }

    /**
     * 프로필 등록 시 닉네임이 중복될 경우 예외 발생을 검증
     */
    @Test
    @DisplayName("프로필 등록 - 닉네임 중복 예외")
    void registerProfile_duplicateNickname() {

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.existsByUserId(1L)).thenReturn(false);
        when(profileRepository.existsByNickname(profileDTO.getNickname())).thenReturn(true);

        MultipartFile mockFile = mock(MultipartFile.class);

        // then
        assertThatThrownBy(() -> profileService.registerProfile(1L, profileDTO, mockFile))
                .isInstanceOf(EntityExistsException.class)
                .hasMessage("이미 사용 중인 닉네임입니다.");
    }

    /**
     * <h5>프로필 이미지 업데이트 성공 테스트</h5>
     * <p>기존 프로필 조회 후 이미지 및 S3 키 변경, 저장 호출 확인</p>
     */
    @Test
    @DisplayName("프로필 이미지 업데이트 성공")
    void updateImage_success() throws IOException {

        // given
        Profile existingProfile = new Profile();
        existingProfile.setUser(user);
        existingProfile.setImageUrl("http://old-image.com/old.png");
        existingProfile.setS3Key("oldKey");

        MultipartFile mockFile = mock(MultipartFile.class);
        String newImageUrl = "http://new-image.com/new.png";
        String newS3Key = "new.png";

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existingProfile));
        when(s3Service.uploadFile(mockFile)).thenReturn(newImageUrl);
        doNothing().when(s3Service).deleteFile("oldKey");
        when(profileRepository.save(any(Profile.class))).thenReturn(existingProfile);

        profileService.updateProfileImage(1L, mockFile);

        // then
        assertThat(existingProfile.getImageUrl()).isEqualTo("http://new-image.com/new.png");
        assertThat(existingProfile.getS3Key()).isEqualTo(newS3Key);
        verify(profileRepository).save(existingProfile);
        verify(s3Service).deleteFile("oldKey");
    }

    /**
     * 프로필 이미지 업데이트 시 프로필이 존재하지 않으면 예외 발생을 검증
     */
    @Test
    @DisplayName("프로필 이미지 업데이트 - 프로필 없음 예외")
    void updateImage_profileNotFound() {

        // when
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        MultipartFile mockFile = mock(MultipartFile.class);

        // then
        assertThatThrownBy(() -> profileService.updateProfileImage(1L, mockFile))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 프로필은 존재하지 않습니다.");
    }

    /**
     * <h5>프로필 수정 성공 테스트</h5>
     * <p>기존 프로필 조회, 닉네임 중복 검사 통과 후 변경 및 저장 호출 확인</p>
     */
    @Test
    @DisplayName("프로필 수정 성공")
    void updateProfile_success() {

        // given
        Profile existingProfile = new Profile();
        existingProfile.setUser(user);
        existingProfile.setNickname("oldNick");

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existingProfile));
        when(profileRepository.existsByNicknameAndUserIdNot(profileDTO.getNickname(), 1L)).thenReturn(false);
        when(profileRepository.save(any(Profile.class))).thenReturn(existingProfile);

        // when
        Profile updated = profileService.updateProfile(1L, profileDTO);

        // then
        assertThat(updated.getNickname()).isEqualTo(profileDTO.getNickname());
        verify(profileRepository).save(any(Profile.class));
    }

    /**
     * 프로필 수정 시 프로필이 존재하지 않으면 예외 발생을 검증
     */
    @Test
    @DisplayName("프로필 수정 - 프로필 없음 예외")
    void updateProfile_notFound() {

        // when
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> profileService.updateProfile(1L, profileDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 프로필은 존재하지 않습니다.");
    }

    /**
     * 프로필 수정 시 닉네임이 중복되면 예외 발생을 검증
     */
    @Test
    @DisplayName("프로필 수정 - 닉네임 중복 예외")
    void updateProfile_duplicateNickname() {

        // given
        Profile profile = new Profile();
        profile.setUser(new User());
        profile.setNickname("ddddds");

        // when
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.existsByNicknameAndUserIdNot(profileDTO.getNickname(), 1L)).thenReturn(true);

        // then
        assertThatThrownBy(() -> profileService.updateProfile(1L, profileDTO))
                .isInstanceOf(EntityExistsException.class)
                .hasMessage("이미 사용 중인 닉네임입니다.");
    }

    /**
     * 프로필 수정 시 닉네임 길이가 짧으면 예외 발생
     */
    @Test
    @DisplayName("프로필 수정 - 닉네임 짧음 예외")
    void updateProfile_shortNickname() {
        // given
        profileDTO.setNickname("a"); // 2자 미만

        Profile profile = new Profile();
        profile.setUser(new User());
        profile.setNickname("ddddds");

        // when
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        // then
        assertThatThrownBy(() -> profileService.updateProfile(1L, profileDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임은 2자 이상이어야 합니다.");
    }

    /**
     * userId로 프로필 조회 시 프로필이 존재하면 반환되는지 검증
     */    @Test
    @DisplayName("userId로 프로필 조회 - 존재함")
    void getProfileByUserId_found() {

        // given
        Profile existingProfile = new Profile();
        existingProfile.setUser(user);

        // when
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existingProfile));

        // then
        Profile profile = profileService.getProfileByUserId(1L);
        assertThat(profile).isSameAs(existingProfile);
    }

    /**
     * userId로 프로필 조회 시 프로필이 없으면 null 반환되는지 검증
     */
    @Test
    @DisplayName("userId로 프로필 조회 - 없음")
    void getProfileByUserId_notFound() {

        // when
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // then
        Profile profile = profileService.getProfileByUserId(1L);
        assertThat(profile).isNull();
    }

    /**
     * <h5>userId로 프로필 삭제 성공 테스트</h5>
     * <p>기존 프로필 조회 후 삭제 호출 확인</p>
     */
    @Test
    @DisplayName("userId로 프로필 삭제 성공")
    void deleteProfileByUserId_success() {

        // given
        Profile profile = new Profile();
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        // then
        profileService.deleteProfileByUserId(1L);

        // then
        verify(profileRepository).delete(profile);
    }

    /**
     * userId로 프로필 삭제 시 프로필이 존재하지 않으면 예외 발생을 검증
     */
    @Test
    @DisplayName("userId로 프로필 삭제 - 존재하지 않아 예외")
    void deleteProfileByUserId_notFound() {

        // when
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> profileService.deleteProfileByUserId(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("삭제할 프로필이 없습니다.");
    }
}