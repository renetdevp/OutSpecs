package com.percent99.OutSpecs.service.impl;

import com.percent99.OutSpecs.dto.ParticipationDTO;
import com.percent99.OutSpecs.entity.Participation;
import com.percent99.OutSpecs.entity.ParticipationStatus;
import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.repository.ParticipationRepository;
import com.percent99.OutSpecs.repository.PostRepository;
import com.percent99.OutSpecs.service.ParticipationService;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ParticipationService 인터페이스의 기본 구현체
 * <ul>
 *     <li>@Service으로 등록되어 DI 대상이된다</li>
 *     <li>모든 쓰기 메세드에 @Transactional, 읽기 메서드에 @Transactional(readOnly = true)를 적용한다</li>
 *     <li>존재하지 않는 댓글 조회시 EntityNotFoundException를 던진다.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ParticipationServiceImpl implements ParticipationService {

    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Participation getParticipationById(Long id) {
        return participationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 신청 정보가 발견되지않았습니다."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Participation updateParticipation(Long id, ParticipationDTO dto) {
        Participation participation = getParticipationById(id);
        participation.setStatus(dto.getStatus());

        return participationRepository.save(participation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Participation> getParticipationsByPostId(Long postId) {
        return participationRepository.findByPostId(postId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Participation> getParticipationsByUserId(Long userId) {
        return participationRepository.findByUserId(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteParticipation(Long id) {
        if(!participationRepository.existsById(id)){
            throw new EntityNotFoundException("해당 신청 정보가없습니다.");
        }
        participationRepository.deleteById(id);
    }
}