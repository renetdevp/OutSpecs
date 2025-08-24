package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.*;
import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.repository.CommentRepository;
import com.percent99.OutSpecs.repository.PostRepository;
import com.percent99.OutSpecs.repository.ReactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private final ParticipationService participationService;

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
     * 조회수 1 증가 후 게시글 조회
     * @param postId 조회할 게시글 ID
     * @return 조회된 post 엔티티
     */
    @Transactional
    public Post getPostAndIncreaseViewCount(Long postId) {
        postRepository.increaseViewCount(postId);
        return getPostById(postId);
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
     * @param size 좋아요 가져올 개수
     * @return 최신글 목록
     */
    public Slice<Post> getRecentPosts(User user, PostType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if(type.equals(PostType.AIPLAY)) {
            if(user == null) {
                throw new EntityNotFoundException("해당 유저가 존재하지 않습니다.");
            }
            pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            return postRepository.findByUserIdAndType(user.getId(), type, pageable);
        }
        else {
            return postRepository.findByTypeOrderByCreatedAtDesc(type, pageable);
        }
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
     * 특정 게시판 타입에서 선택한 태그가 들어있는 게시글을 조회한다.
     * @param postType 게시글 타입
     * @param tags 원하는 태그
     * @return 태그별 게시글 목록
     */
    public Slice<Post> getFilteredPosts(PostType postType, List<String> tags, int page, int size) {
        if (postType == null) {
            throw new IllegalArgumentException("PostType은 null일 수 없습니다.");
        }
        Pageable pageable = PageRequest.of(page, size,  Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Long> postIds = null;

        switch (postType) {
            case QNA :
            case FREE:
                if (tags == null || tags.isEmpty()) {
                    throw new IllegalArgumentException("태그가 없습니다.");
                }
                postIds = postRepository.findPostsByTypeAndTags(postType, tags, tags.size());
                break;
            case AIPLAY:
            case PLAY:
                if (tags == null || tags.isEmpty()) {
                    throw new IllegalArgumentException("장소가 없습니다.");
                }
                postIds = postRepository.findHangoutPostsByPlace(tags.get(0));
                break;
            case RECRUIT:
                if (tags == null || tags.isEmpty()) {
                    throw new IllegalArgumentException("태그가 없습니다.");
                }
                postIds = postRepository.findRecruitPostsByTechs(tags);
                break;
            default: throw new IllegalStateException("알 수 없는 PostType: " + postType);
        }

        if (postIds == null || postIds.isEmpty()) {
            return new SliceImpl<>(new ArrayList<>(), pageable, false); // 빈 Slice 반환
        }
        postIds = postIds.stream().distinct().collect(Collectors.toList());
        return postRepository.findByIdIn(postIds, pageable);
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
        int answersCount = (int)commentRepository.countByTypeAndParentId(CommentType.ANSWER, postId);
        boolean isLiked = reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(user, TargetType.POST, postId, ReactionType.LIKE);
        boolean isBookmarked = reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(user, TargetType.POST, postId, ReactionType.BOOKMARK);
        boolean isReported = reactionRepository.existsByUserAndTargetTypeAndTargetIdAndReactionType(user, TargetType.POST, postId, ReactionType.REPORT);
        boolean isParticipation = false;
        if(user != null) isParticipation = participationService.existParticipationByUserId(user.getId(),postId);
        int teamCount = participationService.countAcceptedParticipation(postId);

        return new PostResponseDTO(likesCount, commentsCount, answersCount, isLiked, isBookmarked, isReported, isParticipation, teamCount);
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
            qnaDTO.setAnswerComplete(post.getPostQnA().getAnswerComplete());
            dto.setQnaInfo(qnaDTO);
        }
    }

    public PostType resolvePostType(String category){
        if(category == null || category.isBlank()) return PostType.FREE;
        String v = category.trim();

        try{ return PostType.valueOf(v.toUpperCase());}
        catch (IllegalArgumentException ignore){}

        for (PostType t : PostType.values()){
            if(t.pathPrefix().equalsIgnoreCase(v))return t;
        }
        return PostType.FREE;
    }

    @Transactional(readOnly = true)
    public boolean existsByTypeAndTitleLike(PostType type, String title){
        return postRepository.existsByTypeAndTitleContainingIgnoreCase(type,title);
    }

    @Transactional(readOnly = true)
    public List<PostListViewDTO> search(PostType type, String q) {
        String query = (q == null) ? "" : q.trim();
        if (query.isEmpty()) return List.of();

        List<Post> posts = postRepository.searchByOptionalTypeAndTitle(type, query);

        if (posts.size() > 100) posts = posts.subList(0, 100);
        return toViews(posts,  true, false);
    }

    /** 공통 변환: 배치 집계로 카운트 모으고 뷰 DTO 채움 */
    public List<PostListViewDTO> toViews(List<Post> posts, boolean withCounts, boolean withImages) {
        if (posts.isEmpty()) return List.of();

        List<Long> ids = posts.stream().map(Post::getId).toList();

        final Map<Long, Long> likeMap = withCounts
                ? reactionRepository.countByPostIdsAndType(ids, TargetType.POST, ReactionType.LIKE)
                .stream()
                .collect(Collectors.toMap(ReactionRepository.CountByPostId::getPostId,
                        ReactionRepository.CountByPostId::getCnt))
                : Collections.emptyMap();

        final Map<Long, Long> bookmarkMap = withCounts
                ? reactionRepository.countByPostIdsAndType(ids, TargetType.POST, ReactionType.BOOKMARK)
                .stream()
                .collect(Collectors.toMap(ReactionRepository.CountByPostId::getPostId,
                        ReactionRepository.CountByPostId::getCnt))
                : Collections.emptyMap();

        final Map<Long, Long> commentMap = withCounts
                ? commentRepository.countCommentsInBatch(ids, CommentType.COMMENT)
                .stream()
                .collect(Collectors.toMap(CommentRepository.CountByPostId::getPostId,
                        CommentRepository.CountByPostId::getCnt))
                : Collections.emptyMap();

        final Map<Long, PostTeamInformationDTO> teamInfoMap =
                posts.stream()
                        .filter(p -> p.getType() == PostType.TEAM && p.getTeamInfo() != null)
                        .collect(Collectors.toMap(
                                Post::getId,
                                p -> toTeamInfoDto(p)
                        ));

        return posts.stream().map(p -> new PostListViewDTO(
                p.getId(),
                p.getTitle(),
                summarize(p.getContent()),
                p.getUser(),
                p.getType(),
                p.getCreatedAt(),
                teamInfoMap.get(p.getId()),
                p.getViewCount() == null ? 0L : p.getViewCount(),
                likeMap.getOrDefault(p.getId(), 0L),
                commentMap.getOrDefault(p.getId(), 0L),
                bookmarkMap.getOrDefault(p.getId(), 0L),
                withImages ? safeImages(p) : null
        )).toList();
    }
    private PostTeamInformationDTO toTeamInfoDto(Post p) {
        PostTeamInformationDTO dto = new PostTeamInformationDTO();
        dto.setCapacity(p.getTeamInfo().getCapacity());
        dto.setStatus(p.getTeamInfo().getStatus());
        return dto;
    }
    private String summarize(String s) {
        if (s == null) return null;
        return s.length() > 160 ? s.substring(0, 160) + "…" : s;
    }
    private List<Image> safeImages(Post p) {
        try { return p.getImages(); } catch (Exception e) { return null; }
    }
}