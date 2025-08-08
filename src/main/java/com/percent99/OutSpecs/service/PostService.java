package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.*;
import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.handler.PostDetailHandler;
import com.percent99.OutSpecs.repository.PostRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 ** 게시글(post) 관련 생성·조회·수정·삭제 기능을 제공하는 서비스
 * <ul>
 *     <li>@Service으로 등록되어 DI 대상이된다</li>
 *     <li>모든 쓰기 메세드에 @Transactional 적용하고, 읽기 메서드에 @Transactional를 사용하지않는다.</li>
 *     <li>외부 API를 사용할경우 외부 API 부분을 제외한 부분에서 @Transactional를 사용한다.</li>
 *     <li>존재하지 않는 글 조회시 EntityNotFoundException를 던진다.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final List<PostDetailHandler> detailHandlers;

    /**
     * 새로운 게시글을 생성한다.
     * @param dto 게시글 생성에 필요한 데이터(dto)
     * @return 생성된 Post 엔티티
     */
    @Transactional
    public Post createPost(PostDTO dto) {

        if (dto.getTitle() == null || dto.getContent() == null || dto.getType() == null) {
            throw new IllegalArgumentException("필수 항목이 누락되었습니다.");
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("해당 유저는 존재하지 않습니다."));

        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setUser(user);
        post.setType(dto.getType());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setViewCount(0);
        
        detailHandlers.stream()
                .filter(h -> h.supports(dto.getType()))
                .forEach(h -> h.handle(post,dto));

        if(post.getType().equals(PostType.AIPLAY)) {
            userService.decrementAiRateLimit(user.getId());
        }

        return postRepository.save(post);
    }

    /**
     * ID로 게시글을 조회한다.
     * @param id 조회할 게시글의 ID
     * @return 조회된 Post 엔티티
     */
    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시물은 존재하지않습니다."));
    }

    /**
     * ID로 게시글을 수정한다.
     * @param id 수정할 게시글의 ID
     * @param dto 수정할 필드가 담긴 DTO
     * @return 수정된 Post 엔티티
     */
    @Transactional
    public Post updatePost(Long id, PostDTO dto) {

        Post post = getPostById(id);
        post.setType(dto.getType());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setUpdatedAt(LocalDateTime.now());

        post.getPostTags().clear();
        post.setTeamInfo(null);
        post.setPostJob(null);
        post.setPostHangout(null);
        post.setPostQnA(null);

        detailHandlers.stream()
                .filter(h -> h.supports(dto.getType()))
                .forEach(h -> h.handle(post,dto));

        return postRepository.save(post);
    }

    public PostDTO getPostDTOById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다."));

        PostDTO dto = new PostDTO();
        dto.setUserId(post.getUser().getId());
        dto.setType(post.getType());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        if(post.getPostTags() != null) {
            PostTagsDTO postTagsDTO = new PostTagsDTO();
            StringBuilder tagsBuilder = new StringBuilder();
            for(PostTags tag : post.getPostTags()) {
                if (!tagsBuilder.isEmpty()) {
                    tagsBuilder.append(",");
                }
                tagsBuilder.append(tag);
            }
            postTagsDTO.setTags(tagsBuilder.toString());
            dto.setTagsInfo(postTagsDTO);
        }
        if(post.getPostHangout() != null) {
            PostHangoutDTO hangoutDTO = new PostHangoutDTO();
            hangoutDTO.setPlaceName(post.getPostHangout().getPlaceName());
            dto.setHangoutInfo(hangoutDTO);
        }
        if(post.getPostJob() != null) {
            PostJobDTO postJobDTO = new PostJobDTO();
            postJobDTO.setCareer(post.getPostJob().getCareer());
            List<String> techniqueNames = post.getPostJob().getTechniques().stream()
                    .map(Techniques::getTech)
                    .collect(Collectors.toList());
            postJobDTO.setTechniques(techniqueNames);
            dto.setJobInfo(postJobDTO);
        }
        if(post.getTeamInfo() != null) {
            PostTeamInformationDTO postTeamInfoDTO = new PostTeamInformationDTO();
            postTeamInfoDTO.setCapacity(post.getTeamInfo().getCapacity());
            postTeamInfoDTO.setStatus(post.getTeamInfo().getStatus());
            dto.setTeamInfo(postTeamInfoDTO);
        }
        if(post.getPostQnA() != null) {
            PostQnADTO postQnADTO = new PostQnADTO();
            postQnADTO.setAnswerComplete(post.getPostQnA().isAnswerComplete());
            dto.setQnaInfo(postQnADTO);
        }
        return dto;
    }

    /**
     * 특정 사용자가 작성한 모든 게시글을 조회한다.
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자가 작성한 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<Post> getPostsByUserId(Long userId) {
        return postRepository.findByUserId(userId);
    }

    /**
     * 특정 유형(type)의 게시글을 조회한다.
     * @param type 조회할 PostType
     * @return 지정한 유형의 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<Post> getPostsByType(PostType type) {
        return postRepository.findByType(type);
    }

    /**
     * 전체 게시글을 조회한다.
     * @return 전체 게시글 반환
     */
    @Transactional(readOnly = true)
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    /**
     * 게시판 타입에 따라 최신글 limit개를 조회한다.
     * @param type 게시판 타입
     * @param limit 좋아요 가져올 개수
     * @return 최신글 목록
     */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public List<Post> getLikePosts(PostType type, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return postRepository.findByTypeOrderByLike(type, pageable);
    }

    /**
     * 채용공고 게시판의 기술스택 필터 조건에 맞는(하나라도 포함) 게시글을 모두 조회한다.
     * @param techs 기술스택 리스트
     * @return 스택 요구조건 별 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<Post> getTechPosts(List<String> techs) {
        return postRepository.findRecruitPostsByTechs(techs);
    }

    /**
     * QNA나 자유게시판에서 선택한 태그가 모두 들어있는 게시글을 조회한다.
     * @param tags 원하는 태그
     * @return 태그별 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<Post> getTagPosts(List<String> tags) {
        return postRepository.findBasePostsByTags(tags, tags.size());
    }

    /**
     * 나가서놀기 게시판에서 선택한 장소가 포함된 게시글을 조회한다.
     * @param place 원하는 장소
     * @return 해당 장소의 게시글 리스트
     */
    @Transactional(readOnly = true)
    public List<Post> getPlacePosts(String place) {
        return postRepository.findHangoutPostsByPlace(place);
    }

    /**
     * 팀모집 게시판의 모집상태별 게시글을 조회한다.
     * @param postStatus 팀모집 상태(open, closed)
     * @return 해당 장소의 게시글 리스트
     */
    @Transactional(readOnly = true)
    public List<Post> getteamPosts(PostStatus postStatus) {
        return postRepository.findHangoutPostsByPlace(postStatus.name());
    }

    /**
     * ID로 게시글을 삭제한다. <br>
     * 질문 게시글은 관리자만 삭제 가능하며 관리자는 모든 게시글 삭제 가능하다. <br>
     * @param postId 삭제할 게시글의 ID
     * @param userId 로그인 유저 ID
     */
    @Transactional
    public void deletedPost(Long userId, Long postId) {

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("해당 유저는 존재하지 않습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new EntityNotFoundException("해당 게시물은 존재하지 않습니다."));

        if (user.getRole().equals(UserRoleType.ADMIN)) {
            postRepository.deleteById(postId);
        } else if(post.getType().equals(PostType.QNA)) {
            throw new IllegalArgumentException("QnA 게시글은 관리자만 삭제할 수 있습니다.");
        } else if(!userId.equals(post.getUser().getId())) {
            throw new IllegalArgumentException("게시글 작성자가 아닙니다.");
        } else { postRepository.deleteById(postId); }
    }

    /**
     * 질문 게시글의 답변상태가 true일 시 false로 false일 시 true로 업데이트한다.
     * @param userId 로그인 유저 ID
     * @param postId 질문게시글 ID
     */
    public void updateAnswerComplete(Long userId, Long postId) {

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("해당 유저는 존재하지 않습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new EntityNotFoundException("해당 게시물은 존재하지 않습니다."));

        if (!post.getType().equals(PostType.QNA)) {
            throw new IllegalArgumentException("QnA 게시글이 아닙니다.");
        }
        if(!userId.equals(post.getUser().getId())) {
            throw new IllegalArgumentException("게시글 작성자가 아닙니다.");
        }

        if (post.getPostQnA() == null) { return; }

        boolean currentStatus = post.getPostQnA().isAnswerComplete();
        post.getPostQnA().setAnswerComplete(!currentStatus);
    }
}