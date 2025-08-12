package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.*;
import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.repository.CommentRepository;
import com.percent99.OutSpecs.repository.PostRepository;
import com.percent99.OutSpecs.repository.ReactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 게시글(post) 관련 조회 기능을 제공하는 서비스 <br>
 * CQRS 패턴에 따라 조회(Query) 책임을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

    private final PostRepository postRepository;
    private final ReactionRepository reactionRepository;
    private final CommentRepository commentRepository;

    /**
     * ID로 게시글을 조회한다.
     * @param id 조회할 게시글의 ID
     * @return 조회된 Post 엔티티
     */
    public Post getPostById(Long id) {
        return postRepository.findWithDetailsById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시물은 존재하지않습니다."));
    }

    public PostDTO getPostDTOById(Long postId) {
        Post post = postRepository.findWithDetailsById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다."));

        // Post 엔티티를 클라이언트에 전달할 PostDTO로 변환합니다.
        return convertToDto(post);
    }

    /**
     * 특정 사용자가 작성한 모든 게시글을 조회한다.
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자가 작성한 게시글 목록
     */
    public List<Post> getPostsByUserId(Long userId) {
        return postRepository.findByUserId(userId);
    }
    /**
     * 특정 유형(type)의 게시글을 조회한다.
     * @param type 조회할 PostType
     * @return 지정한 유형의 게시글 목록
     */
    public List<Post> getPostsByType(PostType type) {
        return postRepository.findByType(type);
    }

    /**
     * 전체 게시글을 조회한다.
     * @return 전체 게시글 반환
     */
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    /**
     * 게시판 타입에 따라 최신글 limit개를 조회한다.
     * @param type 게시판 타입
     * @param limit 좋아요 가져올 개수
     * @return 최신글 목록
     */
    public List<Post> getRecentPosts(PostType type, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return postRepository.findByTypeOrderByCreatedAtDesc(type, pageable);
    }

    /**
     * 게시판 타입에 따라 조회수 높은 순 게시글 limit개를 조회한다.
     * @param type 게시판 타입
     * @param limit 좋아요 가져올 개수
     * @return 조회수 순 게시글 목록
     */
    public List<Post> getViewCountPosts(PostType type, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return postRepository.findByTypeOrderByViewCountDesc(type, pageable);
    }

    /**
     * 게시판 타입에 따라 좋아요 높은 순 게시글 limit개를 조회한다.
     * @param type 게시판 타입
     * @param limit 좋아요 가져올 개수
     * @return 좋아요 순 게시글 목록
     */
    public List<Post> getLikePosts(PostType type, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return postRepository.findByTypeOrderByLike(type, pageable);
    }

    /**
     * 채용공고 게시판의 기술스택 필터 조건에 맞는(하나라도 포함) 게시글을 모두 조회한다.
     * @param techs 기술스택 리스트
     * @return 스택 요구조건 별 게시글 목록
     */
    public List<Post> getTechPosts(List<String> techs) {
        return postRepository.findRecruitPostsByTechs(techs);
    }

    /**
     * QNA나 자유게시판에서 선택한 태그가 모두 들어있는 게시글을 조회한다.
     * @param tags 원하는 태그
     * @return 태그별 게시글 목록
     */
    public List<Post> getTagPosts(List<String> tags) {
        return postRepository.findBasePostsByTags(tags, tags.size());
    }

    /**
     * 나가서놀기 게시판에서 선택한 장소가 포함된 게시글을 조회한다.
     * @param place 원하는 장소
     * @return 해당 장소의 게시글 리스트
     */
    public List<Post> getPlacePosts(String place) {
        return postRepository.findHangoutPostsByPlace(place);
    }

    /**
     * 팀모집 게시판의 모집상태별 게시글을 조회한다.
     * @param postStatus 팀모집 상태(open, closed)
     * @return 해당 장소의 게시글 리스트
     */
    public List<Post> getTeamPosts(PostStatus postStatus) {
        return postRepository.findTeamPostsByStatus(postStatus);
    }

    public PostResponseDTO getPostReactionDetail(Long postId, User user) {
        int likesCount = (int)reactionRepository.countByTargetTypeAndTargetIdAndReactionType(TargetType.POST, postId, ReactionType.LIKE);
        int commentsCount = (int)commentRepository.countByTypeAndParentId(CommentType.COMMENT, postId);
        boolean isLiked = reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(user, TargetType.POST, postId, ReactionType.LIKE);
        boolean isBookmarked = reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(user, TargetType.POST, postId, ReactionType.BOOKMARK);

        return new PostResponseDTO(likesCount, commentsCount, isLiked, isBookmarked);
    }

    /**
     * Post 엔티티를 PostDTO로 변환합니다.
     * 가독성을 위해 private 메서드로 분리했습니다.
     *
     * @param post 변환할 Post 엔티티
     * @return 변환된 PostDTO
     */
    private PostDTO convertToDto(Post post) {
        PostDTO dto = new PostDTO();
        dto.setUserId(post.getUser().getId());
        dto.setType(post.getType());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());

        addTagsInfo(dto, post);
        addHangoutInfo(dto, post);
        addJobInfo(dto, post);
        addTeamInfo(dto, post);
        addQnAInfo(dto, post);

        return dto;
    }

    private void addTagsInfo(PostDTO dto, Post post) {
        if (post.getPostTags() != null && !post.getPostTags().isEmpty()) {
            PostTagsDTO tagsDTO = new PostTagsDTO();
            String tags = post.getPostTags().stream()
                    .map(PostTags::getTags) // tag 객체 자체가 아닌, 태그 이름을 가져옵니다.
                    .collect(Collectors.joining(","));
            tagsDTO.setTags(tags);
            dto.setTagsInfo(tagsDTO);
        }
    }

    private void addHangoutInfo(PostDTO dto, Post post) {
        if (post.getPostHangout() != null) {
            PostHangoutDTO hangoutDTO = new PostHangoutDTO();
            hangoutDTO.setPlaceName(post.getPostHangout().getPlaceName());
            dto.setHangoutInfo(hangoutDTO);
        }
    }

    private void addJobInfo(PostDTO dto, Post post) {
        if (post.getPostJob() != null) {
            PostJobDTO jobDTO = new PostJobDTO();
            jobDTO.setCareer(post.getPostJob().getCareer());
            List<String> techNames = post.getPostJob().getTechniques().stream()
                    .map(Techniques::getTech)
                    .collect(Collectors.toList());
            jobDTO.setTechniques(techNames);
            dto.setJobInfo(jobDTO);
        }
    }

    private void addTeamInfo(PostDTO dto, Post post) {
        if (post.getTeamInfo() != null) {
            PostTeamInformationDTO teamInfoDTO = new PostTeamInformationDTO();
            teamInfoDTO.setCapacity(post.getTeamInfo().getCapacity());
            teamInfoDTO.setStatus(post.getTeamInfo().getStatus());
            dto.setTeamInfo(teamInfoDTO);
        }
    }

    private void addQnAInfo(PostDTO dto, Post post) {
        if (post.getPostQnA() != null) {
            PostQnADTO qnaDTO = new PostQnADTO();
            qnaDTO.setAnswerComplete(post.getPostQnA().isAnswerComplete());
            dto.setQnaInfo(qnaDTO);
        }
    }
}