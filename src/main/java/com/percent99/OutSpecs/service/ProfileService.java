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

import java.time.LocalDateTime;

/**
 * 오픈 프로필 등록, 수정, 삭제, 조회 등을 위한 서비스
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    /**
     * 프로필 등록
     * @param userId
     * @param profileDTO
     * @return 등록된 프로필
     */
    public Profile registerProfile(Long userId, ProfileDTO profileDTO) {
        User exiting = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저는 존재하지 않습니다."));

        if(profileRepository.existsByUserId(exiting.getId())) {
            throw new EntityExistsException("이미 존재하는 프로필입니다.");
        }
        if(profileRepository.existsByNickname(profileDTO.getNickname())) {
            throw new EntityExistsException("이미 존재하는 닉네임입니다.");
        }
        if (profileDTO.getNickname().length() < 2) {
            throw new IllegalArgumentException("닉네임은 2자 이상이어야 합니다.");
        }

        Profile profile = new Profile();
        profile.setUser(exiting);
        profile.setUserId(exiting.getId());
        profile.setStacks(profileDTO.getStacks());
        profile.setNickname(profileDTO.getNickname());
        profile.setExperience(profileDTO.getExperience());
        profile.setSelfInfo(profileDTO.getSelfInfo());
        profile.setAllowCompanyAccess(profileDTO.getAllowCompanyAccess());
        profile.setCreatedAt(LocalDateTime.now());

        return profileRepository.save(profile);
    }

    /**
     * 프로필 이미지 저장
     * @param userId
     * @param imageUrl
     * @param s3Key
     */
    public void updateImage(Long userId, String imageUrl, String s3Key) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 프로필은 존재하지 않습니다."));

        profile.setImageUrl(imageUrl);
        profile.setS3Key(s3Key);

        profileRepository.save(profile);
    }

    /**
     * 프로필 수정
     * @param userId
     * @param profileDTO
     * @return 수정된 프로필
     */
    public Profile updateProfile(Long userId, ProfileDTO profileDTO) {
        Profile exiting = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 프로필은 존재하지 않습니다."));

        if (profileRepository.existsByNicknameAndUserIdNot(profileDTO.getNickname(), userId)) {
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
     * 프로필 조회
     * @param userId
     * @return 프로필 정보
     */
    public Profile getProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId).orElse(null);
    }

    /**
     * 프로필 삭제
     * @param userId
     */
    public void deleteProfileByUserId(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 프로필이 없습니다."));
        profileRepository.delete(profile);
    }
}
