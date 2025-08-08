package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.ParticipationDTO;
import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.repository.ParticipationRepository;
import com.percent99.OutSpecs.repository.PostRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ParticipationServiceTest {

    @Mock
    private ParticipationRepository participationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ParticipationService participationService;

    private User user;
    private Post post;
    private PostTeamInformation teamInfo;
    private ParticipationDTO dto;
    private Participation participation;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        teamInfo = new PostTeamInformation();
        teamInfo.setCapacity(5);

        post = new Post();
        post.setId(100L);
        post.setUser(new User());
        post.setTeamInfo(teamInfo);

        dto = new ParticipationDTO();
        dto.setUserId(1L);
        dto.setPostId(100L);

        participation = new Participation();
        participation.setId(1L);
        participation.setUser(user);
        participation.setPost(post);
        participation.setStatus(ParticipationStatus.PENDING);
    }

    /**
     * createParticipation Test
     */
    @Test
    @DisplayName("createParticipation - 모집 신청 성공")
    void createParticipationSuccess() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(postRepository.findById(100L)).willReturn(Optional.of(post));
        given(participationRepository.countByPostId(100L)).willReturn(0L);
        given(participationRepository.findByUserIdAndPostId(1L, 100L))
                .willReturn(Optional.empty());
        given(participationRepository.save(any(Participation.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        Participation result = participationService.createParticipation(dto);

        // then
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getPost()).isEqualTo(post);
        assertThat(result.getStatus()).isEqualTo(ParticipationStatus.PENDING);
        assertThat(result.getAppliedAt()).isNotNull();

        verify(notificationService).sendNotification(
                eq(user), eq(post.getUser()), eq(NotificationType.APPLY), eq(post.getId())
        );
        verify(participationRepository).save(any(Participation.class));
    }

    @Test
    @DisplayName("createParticipation - 유저가 존재하지 않으면 예외 발생")
    void userNotFound() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> participationService.createParticipation(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("유저");
    }

    @Test
    @DisplayName("createParticipation - 게시글이 존재하지 않으면 예외 발생")
    void postNotFound() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(postRepository.findById(100L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> participationService.createParticipation(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("게시글");
    }

    @Test
    @DisplayName("createParticipation - 모집 인원 초과 시 예외 발생")
    void capacityExceeded() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(postRepository.findById(100L)).willReturn(Optional.of(post));
        given(participationRepository.countByPostId(100L)).willReturn(11L);

        // when & then
        assertThatThrownBy(() -> participationService.createParticipation(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("신청이 불가");
    }

    @Test
    @DisplayName("createParticipation - 중복 신청 시 예외 발생")
    void duplicate() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(postRepository.findById(100L)).willReturn(Optional.of(post));
        given(participationRepository.countByPostId(100L)).willReturn(0L);
        given(participationRepository.findByUserIdAndPostId(1L, 100L))
                .willReturn(Optional.of(new Participation()));

        // when & then
        assertThatThrownBy(() -> participationService.createParticipation(dto))
                .isInstanceOf(EntityExistsException.class)
                .hasMessageContaining("이미 신청");
    }

    /**
     * getParticipationById Test
     */
    @Test
    @DisplayName("getParticipationById - 존재하지 않는 참여 정보 조회 시 예외 발생")
    void getParticipationByIdNotFound() {
        // given
        given(participationRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> participationService.getParticipationById(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("발견되지않았습니다");
    }

    @Test
    @DisplayName("getParticipationById - 조회 성공")
    void getParticipationByIdSuccess() {
        // given
        given(participationRepository.findById(1L)).willReturn(Optional.of(participation));

        // when
        Participation result = participationService.getParticipationById(1L);

        // then
        assertThat(result).isEqualTo(participation);
    }

    /**
     *  updateParticipation Test
     */
    @Test
    @DisplayName("updateParticipation - ACCEPTED 상태로 변경 시 모집완료 처리 및 알림 발송")
    void updateParticipationAccepted() {
        // given
        given(participationRepository.findById(1L)).willReturn(Optional.of(participation));
        given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
        given(participationRepository.save(any(Participation.class))).willAnswer(invocation -> invocation.getArgument(0));

        // 모집완료를 위해 용량 다 채우기
        List<Participation> acceptedList = new ArrayList<>();
        for (int i = 0; i < post.getTeamInfo().getCapacity(); i++) {
            Participation p = new Participation();
            p.setStatus(ParticipationStatus.ACCEPTED);
            acceptedList.add(p);
        }
        given(participationRepository.findByPostId(post.getId())).willReturn(acceptedList);

        // when
        ParticipationDTO updateDto = new ParticipationDTO();
        updateDto.setStatus(ParticipationStatus.ACCEPTED);
        Participation updated = participationService.updateParticipation(1L, updateDto);

        // then
        assertThat(updated.getStatus()).isEqualTo(ParticipationStatus.ACCEPTED);
        assertThat(post.getTeamInfo().getStatus()).isEqualTo(PostStatus.CLOSED);
        then(notificationService).should().sendNotification(eq(post.getUser()), eq(user), eq(NotificationType.ACCEPTED), anyLong());
    }

    @Test
    @DisplayName("updateParticipation - REJECTED 상태로 변경 시 알림 발송")
    void updateParticipationRejected() {
        // given
        participation.setStatus(ParticipationStatus.PENDING);
        given(participationRepository.findById(1L)).willReturn(Optional.of(participation));
        given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
        given(participationRepository.save(any(Participation.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ParticipationDTO updateDto = new ParticipationDTO();
        updateDto.setStatus(ParticipationStatus.REJECTED);
        Participation updated = participationService.updateParticipation(1L, updateDto);

        // then
        assertThat(updated.getStatus()).isEqualTo(ParticipationStatus.REJECTED);
        then(notificationService).should().sendNotification(eq(post.getUser()), eq(user), eq(NotificationType.REJECTED), anyLong());
    }

    /**
     * deleteParticipation Test
     */
    @Test
    @DisplayName("deleteParticipation - 존재하지 않는 참여 정보 삭제 시 예외 발생")
    void deleteParticipationNotFound() {
        // given
        given(participationRepository.existsById(1L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> participationService.deleteParticipation(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("없습니다");
    }

    @Test
    @DisplayName("deleteParticipation - 삭제 성공")
    void deleteParticipationSuccess() {
        // given
        given(participationRepository.existsById(1L)).willReturn(true);
        willDoNothing().given(participationRepository).deleteById(1L);

        // when
        participationService.deleteParticipation(1L);

        // then
        then(participationRepository).should().deleteById(1L);
    }
}
