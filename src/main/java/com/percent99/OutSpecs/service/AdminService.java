package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.entity.UserRoleType;
import com.percent99.OutSpecs.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 관리자(Admin) 전용 비즈니스 로직 처리하는 클래스
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ReactionService reactionService;

    /**
     * 특정 사용자 역할을 변경합니다.
     * @param userId 역할을 변경할 사용자 ID
     * @param newRole 설정할 새로운 역할 (USER,ENTUSER)
     */
    @Transactional
    public void changeUserRole(Long userId, UserRoleType newRole){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다."));
        user.setRole(newRole);
        userRepository.save(user);
    }

    /**
     * 신고횟수가 기본값(5) 이상인 게시물을 조회합니다.
     * @return 신고 많은 게시물 리스트
     */
    /*
    @Transactional(readOnly = true)
    public List<Post> findReportedPosts(){
        final int DEFAULT_THRESHOLD = 5;
        return "";
    }
     */
}