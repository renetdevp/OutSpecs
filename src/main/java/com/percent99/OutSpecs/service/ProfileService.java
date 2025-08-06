package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.entity.Profile;
import com.percent99.OutSpecs.dto.ProfileDTO;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.repository.ProfileRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 사용자 오픈(profile) 관련 비지니스 로직을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    /**
     * 사용자 ID로 프로필을 등록합니다.
     * @param userId 프로필을 생성할 사용자 ID    
     * @param profileDTO 컨트롤러에서 검증된 프로필 데이터
     * @return 생성된 Profile 엔티티
     */
    public Profile registerProfile(Long userId, ProfileDTO profileDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저는 존재하지 않습니다."));

        if(profileRepository.existsByUserId(userId)) {
            throw new EntityExistsException("이미 존재하는 프로필입니다.");
        }
        if(profileRepository.existsByNickname(profileDTO.getNickname())) {
            throw new EntityExistsException("이미 존재하는 닉네임입니다.");
        }
        if (profileDTO.getNickname().length() < 2) {
            throw new IllegalArgumentException("닉네임은 2자 이상이어야 합니다.");
        }

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setStacks(profileDTO.getStacks());
        profile.setNickname(profileDTO.getNickname());
        profile.setExperience(profileDTO.getExperience());
        profile.setSelfInfo(profileDTO.getSelfInfo());
        profile.setAllowCompanyAccess(profileDTO.getAllowCompanyAccess());

        return profileRepository.save(profile);
    }

    /**
     * 프로필 이미지 URL과 S3 키를 업데이트 합니다.
     * @param userId 프로필 소유자 사용자 ID
     * @param imageUrl 저장된 이미지 URL
     * @param s3Key AWS S3에 저장된 키
     */
    public void updateImage(Long userId, String imageUrl, String s3Key) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 프로필은 존재하지 않습니다."));

        profile.setImageUrl(imageUrl);
        profile.setS3Key(s3Key);
        profileRepository.save(profile);
    }

    /**
     * 기본 프로필을 수정합니다.
     * @param userId 수정할 프로필의 사용자 ID
     * @param profileDTO 컨트롤러에서 검증된 프로필 데이터
     * @return 수정된 프로필 수정된 프로필 엔티티
     */
    public Profile updateProfile(Long userId, ProfileDTO profileDTO) {
        Profile exiting = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 프로필은 존재하지 않습니다."));

        if (!exiting.getNickname().equals(profileDTO.getNickname())
                && profileRepository.existsByNicknameAndUserIdNot(profileDTO.getNickname(), userId)) {
            throw new EntityExistsException("이미 사용 중인 닉네임입니다.");
        }
        if (profileDTO.getNickname().length() < 2) {
            throw new IllegalArgumentException("닉네임은 2자 이상이어야 합니다.");
        }

        exiting.setNickname(profileDTO.getNickname());
        exiting.setStacks(profileDTO.getStacks());
        exiting.setExperience(profileDTO.getExperience());
        exiting.setSelfInfo(profileDTO.getSelfInfo());
        exiting.setAllowCompanyAccess(profileDTO.getAllowCompanyAccess());

        return profileRepository.save(exiting);
    }

    /**
     * 사용자 ID로 프로필 조회합니다.
     * @param userId 조회할 사용자 ID
     * @return Profile 엔티티 (없으면 null)
     */
    public Profile getProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId).orElse(null);
    }

    /**
     * 사용자 ID로 프로필을 삭제합니다.
     * @param userId 삭제할 프로필 소유자 ID
     */
    public void deleteProfileByUserId(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 프로필이 없습니다."));
        profileRepository.delete(profile);
    }
}