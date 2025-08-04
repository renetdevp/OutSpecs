package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.entity.Profile;
import com.percent99.OutSpecs.entity.ProfileDTO;
import com.percent99.OutSpecs.entity.User;

/**
 * 오픈 프로필 등록, 수정, 삭제, 조회 등을 위한 서비스
 */
public interface ProfileService {
    Profile registerProfile(Long userId, ProfileDTO profileDTO);
    void updateImage(Long userId, String imageUrl, String s3Key);
    Profile updateProfile(Long userId, ProfileDTO profileDTO);
    Profile getProfileByUserId(Long userId);
    void deleteProfileByUserId(Long userId);
}
