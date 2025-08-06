package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.entity.UserRoleType;
import com.percent99.OutSpecs.repository.PostRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 관리자 서비스 테스트 클래스
 */
@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock UserRepository userRepository;
    @Mock PostRepository postRepository;
    @InjectMocks AdminService adminService;

    /**
     * <h5>사용자 권한 변경이 정상적으로 수행되는지 테스트</h5>
     * <p>
     *   특정 사용자 ID에 대해 권한을 변경한 후 변경된 권한이 적용되고 저장(save) 호출 여부를 확인
     * </p>
     */
    @Test
    @DisplayName("changeUserRole 성공")
    void changeUserRole_success() {

        // given
        User u = new User();
        u.setRole(UserRoleType.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        // when
        adminService.changeUserRole(1L, UserRoleType.ENTUSER);

        // then
        assertThat(u.getRole()).isEqualTo(UserRoleType.ENTUSER);
        verify(userRepository).save(u);
    }

    /**
     * 사용자 권한 변경 시 대상 사용자가 존재하지 않을 경우 예외가 발생하는지 검증
     */
    @Test
    @DisplayName("changeUserRole: 유저 없음 예외")
    void changeUserRole_notFound() {

        // when / then
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() ->
                adminService.changeUserRole(1L, UserRoleType.USER))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 유저를 찾을 수 없습니다.");
    }

    /**
     * 신고 건수가 일정 수 이상인 게시글 목록 조회가 정상적으로 수행되는지 검증
     */
    @Test
    @DisplayName("findReportedPosts 호출")
    void findReportedPosts() {

        // given
        List<Post> reported = List.of(new Post(), new Post());
        when(postRepository.findByReportCountGreaterThanEqual(5))
                .thenReturn(reported);

        // when
        List<Post> result = adminService.findReportedPosts();

        // then
        assertThat(result).isEqualTo(reported);
    }
}