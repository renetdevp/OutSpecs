package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.entity.Profile;
import com.percent99.OutSpecs.dto.ProfileDTO;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.repository.ProfileRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 사용자 오픈(profile) 관련 비지니스 로직을 처리하는 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ReactionService reactionService;
    private final S3Service s3Service;

    /**
     * 사용자 ID로 엔티티를 조회
     *
     * @param userid 조회할 사용자 ID
     * @return 조회된 user 엔티티
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userid){
        return userRepository.findById(userid)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저는 존재하지 않습니다."));
    }

    /**
     * 사용자 ID로 프로필 중복 여부를 확인하고, 이미 존재하면 예외처리
     *
     * @param userId 중복 체크할 사용자 ID
     */
    @Transactional(readOnly = true)
    public void ensureProfileNotExists(Long userId){
        if(profileRepository.existsByUserId(userId)){
            throw new EntityExistsException("이미 프로필이 존재합니다. userId = " + userId);
        }
    }

    /**
     * 사용자 ID로 Profile 엔티티를 조회
     *
     * @param userId 조회할 사용자 ID
     * @return 조회된 Profile 엔티티
     */
    @Transactional(readOnly = true)
    public Optional<Profile> getProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId);
    }

    /**
     * 사용자가 팔로우한 사용자들의 프로필 목록 조회
     *
     * @param user 조회할 사용자
     * @return 팔로우한 사용자들의 프로필 목록
     */
    @Transactional(readOnly = true)
    public List<ProfileDTO> getFollowedUserProfiles(User user){
        List<User> followedUsers = reactionService.getFollowedUsers(user);

        return followedUsers.stream()
                .map(followedUser -> getProfileByUserId(followedUser.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 프로필을 등록
     * <ol>
     *   <li>사용자 존재 여부 및 중복 프로필 검사</li>
     *   <li>닉네임 검증</li>
     *   <li>프로필 이미지 S3 업로드</li>
     *   <li>DB 저장 (트랜잭션 분리) 및 실패 시 업로드 롤백</li>
     * </ol>
     *
     * @param userId 프로필을 생성할 사용자 ID
     * @param dto     컨트롤러에서 검증된 프로필 DTO
     * @param file    업로드할 프로필 이미지 파일 (없으면 null 가능)
     * @return 생성된 Profile 엔티티
     */
    public Profile registerProfile(Long userId, ProfileDTO dto, MultipartFile file) throws IOException{

        User user = getUserById(userId);
        ensureProfileNotExists(userId);
        validateNickname(dto.getNickname(),null);

        String imageUrl = null;
        String s3Key = null;
        if(file != null && !file.isEmpty()){
            try {
                imageUrl = s3Service.uploadFile(file);
                s3Key = extractS3KeyFromUrl(imageUrl);
            }catch (IOException e){
                throw new IOException("프로필 이미지 업로드 실패 : ", e);
            }
        }

        try{
            return createProfileDB(user,dto, imageUrl, s3Key);
        } catch (Exception e){
            if(s3Key != null){
                try{
                    s3Service.deleteFile(s3Key);
                }catch (Exception ex){
                    log.error("롤백 중 S3 파일 삭제 실패 : key = " + s3Key, ex);
                }
            }
            throw  e;
        }
    }

    /**
     * DB에 실제로 Profile 엔티티를 생성하고 저장 <br>
     * 쓰기 전용 트랜잭션이 적용
     *
     * @param user   프로필 소유자 사용자
     * @param dto      프로필 데이터 DTO
     * @param imageUrl 프로필 이미지 URL (없으면  null)
     * @param s3Key    S3에 저장된 이미지의 키 (없으면 null)
     * @return 저장된 Profile 엔티티
     */
    @Transactional
    public Profile createProfileDB(User user,
                                   ProfileDTO dto,
                                   String imageUrl,
                                   String s3Key){
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setStacks(dto.getStacks());
        profile.setNickname(dto.getNickname());
        profile.setExperience(dto.getExperience());
        profile.setSelfInfo(dto.getSelfInfo());
        profile.setAllowCompanyAccess(dto.getAllowCompanyAccess());
        profile.setCreatedAt(LocalDateTime.now());
        profile.setImageUrl(imageUrl);
        profile.setS3Key(s3Key);

        return profileRepository.save(profile);
    }

    /**
     * 프로필 이미지 파일을 업데이트
     * <ol>
     *   <li>기존 프로필 조회</li>
     *   <li>새 이미지 S3 업로드</li>
     *   <li>DB 반영 (트랜잭션 분리) 및 실패 시 업로드 롤백</li>
     *   <li>기존 S3 파일 삭제</li>
     * </ol>
     *
     * @param userId 프로필 소유자 사용자 ID
     * @param file   새로 업로드할 이미지 파일
     */
    public void updateProfileImage(Long userId, MultipartFile file) throws IOException {

        if(file == null || file.isEmpty()){ return; }

        Profile profile = getProfileByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("프로필이 존재하지 않습니다."));

        String oldKey = profile.getS3Key();

        String newUrl;
        String newKey;

        try{
            newUrl = s3Service.uploadFile(file);
            newKey = extractS3KeyFromUrl(newUrl);
        }catch (Exception e){
            throw new IOException("프로필 이미지 업로드 실패 : ", e);
        }

        try{
            updateProfileImageDB(profile,newUrl,newKey);
        } catch (Exception e){
            try {
                s3Service.deleteFile(newKey);
            } catch (Exception ex){
                log.error("롤백 중 S3 파일 삭제 실패 : key = " + newKey, ex);
            }
            throw e;
        }

        if(oldKey != null){
            try {
                s3Service.deleteFile(oldKey);
            }catch (Exception ex){
                log.error("기존 S3 파일 삭제 실패 : key = " + oldKey, ex);
            }
        }
    }

    /**
     * DB에 프로필 이미지 URL과 S3 키를 반영 <br>
     * 쓰기 전용 트랜잭션이 적용됩니다.
     *
     * @param profile 프로필 엔티티 ID
     * @param imageUrl  새 이미지 URL
     * @param s3Key     새 이미지 S3 키
     */
    @Transactional
    public void updateProfileImageDB(Profile profile, String imageUrl, String s3Key){
        profile.setImageUrl(imageUrl);
        profile.setS3Key(s3Key);
        profileRepository.save(profile);
    }

    /**
     * 기본 프로필 정보를 수정
     *
     * @param userId  수정할 프로필 소유자 사용자 ID
     * @param dto     컨트롤러에서 검증된 프로필 DTO
     * @return 수정된  Profile 엔티티
     */
    @Transactional
    public Profile updateProfile(Long userId, ProfileDTO dto) {
        Profile profile = getProfileByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("프로필이 존재하지않습니다."));

        if(!profile.getNickname().equals(dto.getNickname())){
            validateNickname(dto.getNickname(), userId);
        }

        profile.setNickname(dto.getNickname());
        profile.setStacks(dto.getStacks());
        profile.setExperience(dto.getExperience());
        profile.setSelfInfo(dto.getSelfInfo());
        profile.setAllowCompanyAccess(dto.getAllowCompanyAccess());
        return profileRepository.save(profile);
    }

    /**
     * 사용자 프로필을 삭제
     * <ol>
     *   <li>프로필 조회</li>
     *   <li>DB 삭제 (트랜잭션 분리)</li>
     *   <li>S3 이미지 삭제</li>
     * </ol>
     *
     * @param userId 삭제할 프로필 소유자 사용자 ID
     */
    public void deleteProfileByUserId(Long userId) {

        Profile profile = getProfileByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("프로필이 존재하지않습니다."));

        String s3Key = profile.getS3Key();
        deleteProfileDB(profile.getUserId());

        if(s3Key != null){
            try {
                s3Service.deleteFile(s3Key);
            }catch (Exception e){
                log.error("프로필 이미지 S3 삭제 실패 : key = " + s3Key, e);
            }
        }
    }

    /**
     * DB에서 프로필 엔티티를 삭제
     * 쓰기 전용 트랜잭션이 적용
     *
     * @param profileId 삭제할 프로필 엔티티 ID
     */
    @Transactional
    public void deleteProfileDB(Long profileId){
        profileRepository.deleteById(profileId);
    }

    /**
     * S3에 저장된 이미지 URL에서 파일 키를 추출
     *
     * @param imageUrl 전체 S3 URL
     * @return 추출된 S3 키
     */
    private String extractS3KeyFromUrl(String imageUrl) {

        if(imageUrl == null || !imageUrl.contains("/")){
            throw new IllegalArgumentException("올바른 이미지 URL이 아닙니다.");
        }
        try{
            var uri = java.net.URI.create(imageUrl);
            String path = uri.getPath();
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (Exception e){
            throw new IllegalArgumentException("올바른 이미지 URL이 아닙니다.",e);
        }
    }

    /**
     * 닉네임 사용 가능 여부를 검증
     *
     * @param nickname       검사할 닉네임
     * @param userId  이미 존재하는 프로필을 업데이트할 때, 해당 사용자 ID는 제외하고 검사
     */
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

    private ProfileDTO convertToDTO(Profile profile){
        ProfileDTO dto = new ProfileDTO();
        dto.setUserId(profile.getUserId());
        dto.setNickname(profile.getNickname());
        dto.setExperience(profile.getExperience());
        dto.setSelfInfo(profile.getSelfInfo());
        dto.setStacks(profile.getStacks());
        dto.setAllowCompanyAccess(profile.getAllowCompanyAccess());
        dto.setImageUrl(profile.getImageUrl());

        return dto;
    }
}