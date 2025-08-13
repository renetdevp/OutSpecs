package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.ParticipationDTO;
import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.repository.ParticipationRepository;
import com.percent99.OutSpecs.repository.PostRepository;
import com.percent99.OutSpecs.repository.ProfileRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Participation 엔티티 관련 생성·조회·삭제·수정 기능을 제공하는 서비스
 * <ul>
 *     <li>@Service으로 등록되어 DI 대상이된다</li>
 *     <li>모든 쓰기 메세드에 @Transactional 적용하고, 읽기 메서드에 @Transactional를 사용하지않는다.</li>
 *     <li>외부 API를 사용할경우 외부 API 부분을 제외한 부분에서 @Transactional를 사용한다.</li>
 *     <li>존재하지 않는 댓글 조회시 EntityNotFoundException를 던진다.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    /**
     * 새로운 팀 모집 신청을 생성한다.
     * @param dto 모집신청 정보
     * @return 생성된 participation 엔티티
     */
    @Transactional
    public Participation createParticipation(ParticipationDTO dto) {
        User user  = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("해당 유저 정보가 발견되지않았습니다."));

        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("해당 게시글 정보가 발견되지않았습니다."));

        if(dto.getUserId().equals(post.getUser().getId())) {
            throw new IllegalStateException("자신의 팀모집에는 신청할 수 없습니다.");
        }
        if(post.getTeamInfo().getStatus().equals(PostStatus.CLOSED)) {
            throw new IllegalStateException("모집이 완료되었습니다.");
        }
        if(countParticipation(post.getId()) >= post.getTeamInfo().getCapacity() + 5) {
            throw new IllegalStateException("모집신청인원이 많아 신청이 불가합니다.");
        }
        if (getParticipationByUserId(user.getId(), post.getId()) != null) {
            throw new EntityExistsException("해당 게시글에 해당 유저가 이미 신청한 정보가 있습니다.");
        }

        Participation participation = new Participation();
        participation.setUser(user);
        participation.setPost(post);
        participation.setStatus(ParticipationStatus.PENDING);
        participation.setAppliedAt(LocalDateTime.now());
        Participation saved = participationRepository.save(participation);

        //알림 발송
        notificationService.sendNotification(user, post.getUser(), NotificationType.APPLY, post.getId());

        return saved;
    }

    /**
     * ID로 Participation 정보를 조회한다.
     * @param id 조회할 Participation의 ID
     * @return  조회된 participation 엔티티
     */
    @Transactional(readOnly = true)
    public Participation getParticipationById(Long id) {
        return participationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 신청 정보가 발견되지않았습니다."));
    }

    /**
     * ID로 Participation 정보를 수정한다. <br>
     * 팀 신청 수락 시, 모집인원 다 차면 모집완료로 상태 변경한다. <br>
     * @param id 수정할 Participation의 ID
     * @param dto 수정할 내용이 담긴 DTO
     * @return 수정된 Participation 엔티티
     */
    @Transactional
    public Participation updateParticipation(Long id, ParticipationDTO dto) {
        Participation participation = getParticipationById(id);
        Post post = postRepository.findById(participation.getPost().getId())
                .orElseThrow(() -> new EntityNotFoundException("해당 게시글 정보가 발견되지않았습니다."));
        participation.setStatus(dto.getStatus());
        Participation saved = participationRepository.save(participation);

        if(saved.getStatus().equals(ParticipationStatus.ACCEPTED)
                && (countAcceptedParticipation(post.getId()) == post.getTeamInfo().getCapacity())) {
            post.getTeamInfo().setStatus(PostStatus.CLOSED);
        }

        // 알림 발송
        if(saved.getStatus().equals(ParticipationStatus.REJECTED)) {
            notificationService.sendNotification(post.getUser(), participation.getUser(), NotificationType.REJECTED, participation.getId());
        } else if(saved.getStatus().equals(ParticipationStatus.ACCEPTED)) {
            notificationService.sendNotification(post.getUser(), participation.getUser(), NotificationType.ACCEPTED, participation.getId());
        }

        return saved;
    }

    /**
     * 특정 게시글의 모든 Participation 정보를 조회한다.
     * @param postId 조회할 게시글의 ID
     * @return 해당 게시글에 대한 Participation 리스트
     */
    @Transactional(readOnly = true)
    public List<Participation> getParticipationByPostId(Long postId) {
        return participationRepository.findByPostId(postId);
    }

    /**
     * 해당 팀 공고의 팀 허락된 인원 수
     * @param postId 해당 팀 공고
     * @return 현재 모집된 인원 수
     */
    public int countAcceptedParticipation(Long postId) {
        List<Participation> participations = getParticipationByPostId(postId);
        int count = 0;

        for(Participation partic : participations) {
            if(partic.getStatus().equals(ParticipationStatus.ACCEPTED)) count++;
        }
        return count;
    }

    /**
     * 해당 팀에서 허락 인원 상관 없이 모집신청한 인원 수
     * @param postId 해당 팀 공고
     * @return 모집신청 현황
     */
    public int countParticipation(Long postId) {
        return (int)participationRepository.countByPostId(postId);
    }


    /**
     * 특정 사용자가 참여한 모든 Participation 정보를 조회한다.
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자의 Participation 리스트
     */
    @Transactional(readOnly = true)
    public List<Participation> getAllParticipationByUserId(Long userId) {
        return participationRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Participation getParticipationByUserId(Long userId, Long postId) {
        return participationRepository.findByUserIdAndPostId(userId, postId).orElse(null);
    }

    /**
     * ID로 Participation 정보를 삭제한다.
     * @param id 삭제할 Participation의 ID
     */
    @Transactional
    public void deleteParticipation(Long id) {
        if(!participationRepository.existsById(id)){
            throw new EntityNotFoundException("해당 신청 정보가없습니다.");
        }
        participationRepository.deleteById(id);
    }
}