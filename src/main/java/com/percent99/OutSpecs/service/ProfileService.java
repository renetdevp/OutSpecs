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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

/**
 * 사용자 오픈(profile) 관련 비지니스 로직을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    /**
     * 사용자 ID로 프로필을 등록합니다.
     * @param userId 프로필을 생성할 사용자 ID    
     * @param profileDTO 컨트롤러에서 검증된 프로필 데이터
     * @return 생성된 Profile 엔티티
     */
    @Transactional
    public Profile registerProfile(Long userId, ProfileDTO profileDTO, MultipartFile file) throws IOException{
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저는 존재하지 않습니다."));
        if(profileRepository.existsByUserId(userId)){
            throw new EntityExistsException("이미 존재하는 프로필입니다.");
        }

        validateNickname(profileDTO.getNickname(),null);

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setStacks(profileDTO.getStacks());
        profile.setNickname(profileDTO.getNickname());
        profile.setExperience(profileDTO.getExperience());
        profile.setSelfInfo(profileDTO.getSelfInfo());
        profile.setAllowCompanyAccess(profileDTO.getAllowCompanyAccess());

        String imageUrl = null;
        String s3Key = null;
        if(file != null && !file.isEmpty()){
            imageUrl = s3Service.uploadFile(file);
            s3Key = extractS3KeyFromUrl(imageUrl);
        }

        profile.setImageUrl(imageUrl);
        profile.setS3Key(s3Key);

        return profileRepository.save(profile);
    }


    /**
     * 프로필 이미지 URL과 S3 키를 업데이트 합니다.
     *
     * @param userId 프로필 소유자 사용자 ID
     * @param file 프로필 이미지 파일(MultipartFile)
     * @throws IOException
     */
    public void updateProfileImage(Long userId, MultipartFile file) throws IOException {

        if(file == null || file.isEmpty()){ return; }

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 프로필은 존재하지 않습니다."));

        if(profile.getS3Key() != null){
            s3Service.deleteFile(profile.getS3Key());
        }

        String imageUrl = s3Service.uploadFile(file);
        String s3Key = extractS3KeyFromUrl(imageUrl);

        updateProfileImageDB(profile,imageUrl,s3Key);
    }

    private String extractS3KeyFromUrl(String imageUrl) {

        if(imageUrl == null || !imageUrl.contains("/")){
            throw new IllegalArgumentException("올바른 이미지 URL이 아닙니다.");
        }
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }

    @Transactional
    protected void updateProfileImageDB(Profile profile, String imageUrl, String s3key){
        profile.setImageUrl(imageUrl);
        profile.setS3Key(s3key);
        profileRepository.save(profile);
    }

    /**
     * 기본 프로필을 수정합니다.
     * @param userId 수정할 프로필의 사용자 ID
     * @param profileDTO 컨트롤러에서 검증된 프로필 데이터
     * @return 수정된 프로필 수정된 프로필 엔티티
     */
    @Transactional
    public Profile updateProfile(Long userId, ProfileDTO profileDTO) {
        Profile exiting = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 프로필은 존재하지 않습니다."));

        if(!Objects.equals(exiting.getNickname(), profileDTO.getNickname())){
            validateNickname(profileDTO.getNickname(), userId);
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
    @Transactional(readOnly = true)
    public Profile getProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId).orElse(null);
    }

    /**
     * 사용자 ID로 프로필을 삭제합니다.
     * @param userId 삭제할 프로필 소유자 ID
     */
    @Transactional
    public void deleteProfileByUserId(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 프로필이 없습니다."));
        profileRepository.delete(profile);
    }

    private void validateNickname(String nickname, Long userId){
        if(nickname == null || nickname.length() < 2){
            throw new IllegalArgumentException("닉네임은 2자 이상이어야 합니다.");
        }
        boolean exists;
        if(userId == null){
            exists = profileRepository.existsByNickname(nickname);
        }
        else{
            exists = profileRepository.existsByNicknameAndUserIdNot(nickname,userId);
        }
        if(exists){
            throw  new EntityExistsException("이미 사용 중인 닉네임입니다.");
        }
    }
}