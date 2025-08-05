package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.ParticipationDTO;
import com.percent99.OutSpecs.entity.Participation;
import com.percent99.OutSpecs.entity.ParticipationStatus;
import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.repository.ParticipationRepository;
import com.percent99.OutSpecs.repository.PostRepository;
import com.percent99.OutSpecs.repository.UserRepository;
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

        Participation participation = new Participation();
        participation.setUser(user);
        participation.setPost(post);
        participation.setStatus(ParticipationStatus.PENDING);
        participation.setAppliedAt(LocalDateTime.now());

        return participationRepository.save(participation);
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
     * ID로 Participation 정보를 수정한다.
     * @param id 수정할 Participation의 ID
     * @param dto 수정할 내용이 담긴 DTO
     * @return 수정된 Participation 엔티티
     */
    @Transactional
    public Participation updateParticipation(Long id, ParticipationDTO dto) {
        Participation participation = getParticipationById(id);
        participation.setStatus(dto.getStatus());

        return participationRepository.save(participation);
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
     * 특정 사용자가 참여한 모든 Participation 정보를 조회한다.
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자의 Participation 리스트
     */

    @Transactional(readOnly = true)
    public List<Participation> getParticipationByUserId(Long userId) {
        return participationRepository.findByUserId(userId);
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