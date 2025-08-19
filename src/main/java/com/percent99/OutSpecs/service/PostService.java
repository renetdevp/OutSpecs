package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.*;
import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.handler.PostDetailHandler;
import com.percent99.OutSpecs.repository.PostRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 ** 게시글(post) 관련 생성·조회·수정·삭제 기능을 제공하는 서비스
 * <ul>
 *     <li>@Service으로 등록되어 DI 대상이된다</li>
 *     <li>모든 쓰기 메세드에 @Transactional 적용하고, 읽기 메서드에 @Transactional를 사용하지않는다.</li>
 *     <li>외부 API를 사용할경우 외부 API 부분을 제외한 부분에서 @Transactional를 사용한다.</li>
 *     <li>존재하지 않는 글 조회시 EntityNotFoundException를 던진다.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostQueryService postQueryService;
    private final UserService userService;
    private final List<PostDetailHandler> detailHandlers;
    private final CommentService commentService;
    private final S3Service s3Service;

    /**
     * 새로운 게시글을 생성한다.
     * @param dto 게시글 생성에 필요한 데이터(dto)
     * @return 생성된 Post 엔티티
     */
    public Post createPost(PostDTO dto, List<MultipartFile> files) throws IOException{

        if (dto.getTitle() == null || dto.getContent() == null || dto.getType() == null) {
            throw new IllegalArgumentException("필수 항목이 누락되었습니다.");
        }

        User user = userService.getUserById(dto.getUserId());

        if(dto.getType() == PostType.RECRUIT && !user.getRole().equals(UserRoleType.ENTUSER)){
            throw new IllegalArgumentException("채용 공고는 기업 회원만 작성할 수 있습니다.");
        }

        List<String> uploadedUrls = new ArrayList<>();
        List<String> s3Keys = new ArrayList<>();
        if(files != null && !files.isEmpty()){
            try{
                for(MultipartFile file : files){
                    if(!file.isEmpty()){
                       String imageUrl = s3Service.uploadFile(file);
                       String s3Key = extractS3KeyFromUrl(imageUrl);

                       uploadedUrls.add(imageUrl);
                       s3Keys.add(s3Key);
                    }
                }
            }catch (IOException e){
                rollbackUploadedFiles(s3Keys);
                throw new IOException("프로필 이미지 업로드 실패 : ", e);
            }
        }
        try{
            return createdPostDB(user,dto,uploadedUrls,s3Keys);
        }catch (Exception e){
            rollbackUploadedFiles(s3Keys);
            throw  e;
        }
    }

    /**
     * DB에 새로운 게시글을 저장한다.
     * @param user      작성자 엔티티
     * @param dto       게시글 DTO
     * @param imageUrls 업로드된 이미지 URL 리스트
     * @param s3Keys    업로드된 이미지 S3 키 리스트
     * @return 저장된 Post 엔티티
     */
    @Transactional
    public Post createdPostDB(User user, PostDTO dto,
                              List<String> imageUrls,
                              List<String> s3Keys){

        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setUser(user);
        post.setType(dto.getType());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setViewCount(0);

        List<Image> images = new ArrayList<>();
        if(imageUrls != null && !imageUrls.isEmpty()){
            for(int i = 0; i < imageUrls.size(); i++){
                Image image = new Image();
                image.setPost(post);
                image.setImageUrl(imageUrls.get(i));
                image.setS3Key(s3Keys.get(i));
                images.add(image);
            }
        }
        post.setImages(images);
        detailHandlers.stream()
                .filter(h -> h.supports(dto.getType()))
                .forEach(h -> h.handle(post,dto));

        if(post.getType().equals(PostType.AIPLAY)) {
            userService.decrementAiRateLimit(user.getId());
        }
        return postRepository.save(post);
    }

    /**
     * ID로 게시글을 수정한다.
     * <p>요청 사용자가 작성자가 아닌 경우 또는 권한 없는 타입인 경우 예외 발생</p>
     * @param id    수정할 게시글 ID
     * @param dto   수정할 내용이 담긴 DTO
     * @param files 새 이미지 파일 리스트 (nullable)
     * @return 수정된 Post 엔티티
     * @throws IOException 이미지 업로드 실패 시 발생
     * @throws IllegalArgumentException 권한 없는 경우
     */
    public Post updatePost(Long id, PostDTO dto,List<MultipartFile> files) throws IOException {

        Post post = postQueryService.getPostById(id);
        if(!post.getUser().getId().equals(dto.getUserId())) {
            throw new IllegalArgumentException("게시글 작성자가 아닙니다.");
        }

        if(dto.getType() == PostType.RECRUIT && !post.getUser().getRole().equals(UserRoleType.ENTUSER)){
            throw new IllegalArgumentException("채용 공고는 기업 회원만 작성할 수 있습니다.");
        }


        // 기본 이미지 정보 백업 (롤백용)
        List<String> oldS3Keys = new ArrayList<>();
        if(post.getImages() != null && !post.getImages().isEmpty()){
            oldS3Keys = post.getImages().stream()
                    .map(Image:: getS3Key)
                    .toList();
        }

        // 새 이미지 있을시 기존 이미지 삭제후 새 이미지 업로드
        List<String> newImageUrls = new ArrayList<>();
        List<String> newS3Keys = new ArrayList<>();

        if(files != null && !files.isEmpty()){
            for(String oldS3Key : oldS3Keys){
                try{
                    s3Service.deleteFile(oldS3Key);
                }catch (Exception e){
                    log.error("기존 이미지 삭제 실패: key = " + oldS3Key, e);
                }
            }
        }

        // 새 이미지 업로드
        try{
            for(MultipartFile file : files){
                if(!file.isEmpty()){
                    String imageUrl = s3Service.uploadFile(file);
                    String s3Key = extractS3KeyFromUrl(imageUrl);

                    newImageUrls.add(imageUrl);
                    newS3Keys.add(s3Key);
                }
            }
        }catch (IOException e){
            rollbackUploadedFiles(newS3Keys);
            throw new IOException("이미지 업로드 실패 : ", e);
        }

        // DB 업데이트
        try {
            return updatePostDB(post, dto, newImageUrls, newS3Keys);
        } catch (Exception e) {
            // DB 업데이트 실패 시 롤백
            if (!newS3Keys.isEmpty()) {
                rollbackUploadedFiles(newS3Keys);
            }
            throw e;
        }
    }

    /**
     * DB에 게시글을 실제 수정하고 저장한다.
     * <p>기존 이미지들을 orphanRemoval 처리로 삭제한 후,
     * 새 이미지 리스트를 추가하여 교체한다. </p>
     * @param post        수정할 게시글 엔티티
     * @param dto         게시글 DTO
     * @param newImageUrls 새 이미지 URL 리스트
     * @param newS3Keys    새 이미지 S3 키 리스트
     * @return 수정된 Post 엔티티
     */
    @Transactional
    public Post updatePostDB(Post post, PostDTO dto, List<String> newImageUrls, List<String> newS3Keys) {

        List<Image> images = post.getImages();
        if(images == null){
            images = new ArrayList<>();
            post.setImages(images);
        }

        for(Image img : new ArrayList<>(images)){
            img.setPost(null);
            images.remove(img);
        }

        if (!newImageUrls.isEmpty() && newImageUrls != null) {
            for (int i = 0; i < newImageUrls.size(); i++) {
                Image image = new Image();
                image.setPost(post);
                image.setImageUrl(newImageUrls.get(i));
                image.setS3Key(newS3Keys.get(i));
                images.add(image);
            }
        }
        post.setType(dto.getType());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setUpdatedAt(LocalDateTime.now());

        detailHandlers.stream()
                .filter(h -> h.supports(dto.getType()))
                .forEach(h -> h.handle(post, dto));
        return postRepository.save(post);
    }

    /**
     * 게시글을 삭제한다.
     * <p>권한 규칙 </p>
     * <ul>
     *   <li>관리자: 모든 게시글 삭제 가능</li>
     *   <li>QnA 게시글: 관리자만 삭제 가능</li>
     *   <li>일반 사용자: 자신의 게시글만 삭제 가능</li>
     * </ul>
     * @param userId 삭제 요청 사용자 ID
     * @param postId 삭제할 게시글 ID
     * @throws EntityNotFoundException 존재하지 않는 사용자 또는 게시글인 경우
     * @throws IllegalArgumentException 권한 없는 경우
     */
    public void deletedPost(Long userId, Long postId) {

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("해당 유저는 존재하지 않습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new EntityNotFoundException("해당 게시물은 존재하지 않습니다."));

        boolean isAdmin = user.getRole().equals(UserRoleType.ADMIN);
        boolean isOwner = userId.equals(post.getUser().getId());

        // 관리자는 모든 게시글을 삭제할 수 있습니다.
        if (isAdmin) {
            deleteAllCommentsById(userId, postId);
            return;
        }

        // 관리자가 아닌 경우, Q&A 게시글은 삭제할 수 없습니다.
        if (post.getType().equals(PostType.QNA)) {
            throw new IllegalArgumentException("QnA 게시글은 관리자만 삭제할 수 있습니다.");
        }

        // 자신의 게시글이 아니면 삭제할 수 없습니다.
        if (!isOwner) {
            throw new IllegalArgumentException("게시글 작성자가 아닙니다.");
        }

        deleteAllCommentsById(userId, postId);
    }

    /**
     * 특정 게시글의 모든 댓글과 이미지를 삭제한다.
     * @param userId 삭제 요청 사용자 ID
     * @param postId 게시글 ID
     */
    public void deleteAllCommentsById(Long userId, Long postId) {
        Post post = postQueryService.getPostById(postId);

        List<String> s3Keys = new ArrayList<>();
        if(post.getImages() != null && !post.getImages().isEmpty()){
            s3Keys = post.getImages().stream()
                    .map(Image::getS3Key)
                    .toList();
        }
        deletePostFromDB(userId, postId);
        deleteImagesFromS3(s3Keys);
    }

    /**
     * DB에서 게시글과 관련된 댓글을 삭제한다.
     * @param userId 삭제 요청 사용자 ID
     * @param postId 게시글 ID
     */
    @Transactional
    public void deletePostFromDB(Long userId, Long postId){
        Post post = postQueryService.getPostById(postId);

        List<Comment> comments;
        if(post.getType().equals(PostType.QNA)) {
            comments = commentService.getByTypeAndPostId(CommentType.ANSWER, postId);
        } else comments = commentService.getByTypeAndPostId(CommentType.COMMENT, postId);

        if(comments != null && !comments.isEmpty()) {
            for(Comment comment : comments) {
                commentService.deletedComment(userId, comment.getId());
            }
        }
        postRepository.deleteById(postId);
    }

    /**
     * S3에 저장된 이미지들을 삭제한다.
     * @param s3Keys 삭제할 S3 키 리스트
     */
    private void deleteImagesFromS3(List<String> s3Keys) {
        if (s3Keys == null || s3Keys.isEmpty()) {
            return;
        }
        for (String s3Key : s3Keys) {
            try {
                s3Service.deleteFile(s3Key);
                log.info("S3 이미지 삭제 성공: key = {}", s3Key);
            } catch (Exception e) {
                log.error("S3 이미지 삭제 실패: key = {}, 오류: {}", s3Key, e.getMessage());
            }
        }
    }

    /**
     * QnA 게시글의 답변 완료 상태를 토글한다.
     * @param userId 요청 사용자 ID
     * @param postId QnA 게시글 ID
     * @throws EntityNotFoundException 존재하지 않는 사용자 또는 게시글
     * @throws IllegalArgumentException QnA 게시글이 아니거나 작성자가 아닌 경우
     */
    @Transactional
    public void updateAnswerComplete(Long userId, Long postId) {

        if(!userRepository.existsById(userId)){
            throw new EntityNotFoundException("해당 유저는 존재하지 않습니다.");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new EntityNotFoundException("해당 게시물은 존재하지 않습니다."));

        if (!post.getType().equals(PostType.QNA)) {
            throw new IllegalArgumentException("QnA 게시글이 아닙니다.");
        }
        if(!userId.equals(post.getUser().getId())) {
            throw new IllegalArgumentException("게시글 작성자가 아닙니다.");
        }

        if (post.getPostQnA() == null) { return; }

        boolean currentStatus = post.getPostQnA().getAnswerComplete();
        post.getPostQnA().setAnswerComplete(!currentStatus);
    }

    /**
     * S3 URL에서 파일 키를 추출한다.
     * @param imageUrl 전체 S3 URL
     * @return 추출된 S3 키
     * @throws IllegalArgumentException 잘못된 URL일 경우
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
     * 업로드 실패 시 이미 업로드된 파일들을 S3에서 삭제한다.
     * @param s3Keys 삭제할 S3 키 리스트
     */
    private void  rollbackUploadedFiles(List<String> s3Keys){
        if(s3Keys != null && !s3Keys.isEmpty()){
            for (String s3key : s3Keys){
                try{
                    s3Service.deleteFile(s3key);
                }catch (Exception ex){
                    log.error("롤백 중 S3 파일 삭제 실패 : key = " + s3key, ex);
                }
            }
        }
    }
}