package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.PostDTO;
import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.handler.PostDetailHandler;
import com.percent99.OutSpecs.repository.PostRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {
    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private CommentService commentService;

    @Mock
    private PostQueryService postQueryService;

    @Mock
    private S3Service s3Service;

    @Mock
    private PostDetailHandler handler1;

    @Mock
    private PostDetailHandler handler2;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private PostService postService;

    private User user;
    private User entUser;
    private PostDTO postDTO;
    private Post post;
    private List<MultipartFile> files;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("test@abcd.efg");
        user.setRole(UserRoleType.USER);

        entUser = new User();
        entUser.setId(2L);
        entUser.setUsername("ent@abcd.efg");
        entUser.setRole(UserRoleType.ENTUSER);

        postDTO = new PostDTO();
        postDTO.setUserId(1L);
        postDTO.setType(PostType.FREE);
        postDTO.setTitle("test title");
        postDTO.setContent("test content");

        post = new Post();
        post.setUser(user);
        post.setId(1L);
        post.setTitle("old title");
        post.setContent("old content");
        post.setType(PostType.TEAM);
        post.setImages(new ArrayList<>());
        files = new ArrayList<>();
        files.add(multipartFile);
        post.setPostTags(new ArrayList<>());
        post.setPostHangout(new PostHangout());
        post.setPostJob(new PostJob());
        post.setPostQnA(new PostQnA());

        postService = new PostService(postRepository, userRepository, postQueryService, userService,
                List.of(handler1, handler2), commentService, s3Service);
    }

    /**
     *  createPost test
     */
    @Test
    @DisplayName("createPost - 게시글 등록 성공(파일 없음)")
    void createPostSuccessfullyWithoutFiles() throws IOException {
        // given
        given(userService.getUserById(1L)).willReturn(user);
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(handler1.supports(PostType.FREE)).willReturn(true);

        // when
        Post result = postService.createPost(postDTO, null);

        // then
        assertEquals("test title", result.getTitle());
        assertEquals(PostType.FREE, result.getType());
        verify(postRepository).save(any(Post.class));
        verify(handler1).handle(any(Post.class), eq(postDTO));
        verify(handler2, never()).handle(any(), any());
        verify(s3Service, never()).uploadFile(any());
    }

    @Test
    @DisplayName("createPost - 게시글 등록 성공 (파일 포함)")
    void createPostSuccessfullyWithFiles() throws IOException {
        // given
        given(userService.getUserById(1L)).willReturn(user);
        given(multipartFile.isEmpty()).willReturn(false);
        given(s3Service.uploadFile(multipartFile)).willReturn("https://bucket.s3.amazonaws.com/images/test.jpg");
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(handler1.supports(PostType.FREE)).willReturn(true);

        // when
        Post result = postService.createPost(postDTO, files);

        // then
        assertEquals("test title", result.getTitle());
        assertEquals(PostType.FREE, result.getType());
        verify(s3Service).uploadFile(multipartFile);
        verify(postRepository).save(any(Post.class));
        assertEquals(1, result.getImages().size());
        assertEquals("https://bucket.s3.amazonaws.com/images/test.jpg", result.getImages().get(0).getImageUrl());
    }

    @Test
    @DisplayName("createPost - 파일 업로드 실패시 롤백")
    void rollbackWhenFileUploadFails() throws IOException {
        // given
        given(userService.getUserById(1L)).willReturn(user);
        given(multipartFile.isEmpty()).willReturn(false);
        given(s3Service.uploadFile(multipartFile)).willThrow(new IOException("Upload failed"));

        // when & then
        assertThrows(IOException.class, () -> postService.createPost(postDTO, files));
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("createPost - 채용공고는 기업회원만 작성 가능")
    void recruitPostOnlyForEntUser() throws IOException {
        // given
        postDTO.setType(PostType.RECRUIT);
        given(userService.getUserById(1L)).willReturn(user); // 일반 사용자

        // when & then
        assertThrows(IllegalArgumentException.class, () -> postService.createPost(postDTO, null));
    }

    @Test
    @DisplayName("createPost - 기업회원은 채용공고 작성 가능")
    void entUserCanCreateRecruitPost() throws IOException {
        // given
        postDTO.setUserId(2L);
        postDTO.setType(PostType.RECRUIT);
        given(userService.getUserById(2L)).willReturn(entUser);
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Post result = postService.createPost(postDTO, null);

        // then
        assertEquals(PostType.RECRUIT, result.getType());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("createPost - 존재하지 않는 유저 예외")
    void throwExceptionWhenUserNotFound() {
        //given
        postDTO.setUserId(999L);
        given(userService.getUserById(999L))
                .willThrow(new EntityNotFoundException("해당 유저는 존재하지 않습니다."));

        // when & then
        assertThrows(EntityNotFoundException.class, () -> postService.createPost(postDTO, null));
    }

    @Test
    @DisplayName("createPost - AIPLAY시 rate limit 감소")
    void callDecrementRateLimitForAiPlay() throws IOException {
        //given
        postDTO.setType(PostType.AIPLAY);
        given(userService.getUserById(1L)).willReturn(user);
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        //when
        postService.createPost(postDTO, null);

        //then
        verify(userService).decrementAiRateLimit(1L);
    }

    @Test
    @DisplayName("createPost - AIPLAY 아닐 시 rate limit 감소하지 않음")
    void notCallRateLimitWhenNotAiPlay() throws IOException {
        //given
        postDTO.setType(PostType.PLAY);
        given(userService.getUserById(1L)).willReturn(user);
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        //when
        postService.createPost(postDTO, null);

        //then
        verify(userService, never()).decrementAiRateLimit(anyLong());

    }

    @Test
    @DisplayName("createPost - 지원하는 handler만 handle() 메서드 실행")
    void onlyCallSupportingHandlers() throws IOException {
        //given
        postDTO.setType(PostType.QNA);
        given(userService.getUserById(1L)).willReturn(user);
        given(handler1.supports(PostType.QNA)).willReturn(false);
        given(handler2.supports(PostType.QNA)).willReturn(true);

        //when
        postService.createPost(postDTO, null);

        //then
        verify(handler1, never()).handle(any(), any());
        verify(handler2).handle(any(Post.class), eq(postDTO));
    }

    @Test
    @DisplayName("createPost - 필수 값 누락 시 예외 발생")
    void throwExceptionWhenRequiredFieldsMissing() throws IOException {
        //given
        postDTO.setTitle(null);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> postService.createPost(postDTO, null));
    }

    @Test
    @DisplayName("createPost - 핸들러 실행 내용 post에 반영")
    void handlerModificationsInSavedPost() throws IOException {
        //given
        postDTO.setType(PostType.PLAY);
        given(userService.getUserById(1L)).willReturn(user);
        given(handler1.supports(PostType.PLAY)).willReturn(true);
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        doAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            PostDTO dto = invocation.getArgument(1);

            PostHangout hangout = new PostHangout();
            hangout.setPost(post);
            hangout.setPlaceName("test-place");

            post.setPostHangout(hangout);
            return null;
        }).when(handler1).handle(any(Post.class), eq(postDTO));

        //when
        Post result = postService.createPost(postDTO, null);

        //then
        assertNotNull(result.getPostHangout());
        assertEquals("test-place", result.getPostHangout().getPlaceName());
        verify(handler1).handle(any(Post.class), eq(postDTO));
    }


    /**
     * updatePost test
     */
    @Test
    @DisplayName("updatePost - 게시물 수정 성공")
    void updatePostTest() throws IOException {
        // given
        given(postQueryService.getPostById(1L)).willReturn(post);
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(handler1.supports(any())).willReturn(false);
        given(handler2.supports(any())).willReturn(false);

        // when
        Post updatedPost = postService.updatePost(1L, postDTO, Collections.emptyList());

        // then
        assertEquals("test title", updatedPost.getTitle());
        assertEquals("test content", updatedPost.getContent());
        assertEquals(PostType.FREE, updatedPost.getType());
        verify(postRepository).save(updatedPost);
    }

    @Test
    @DisplayName("updatePost - 작성자가 아닌 경우 예외")
    void throwExceptionWhenNotAuthor() {
        // given
        postDTO.setUserId(999L);
        given(postQueryService.getPostById(1L)).willReturn(post);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> postService.updatePost(1L, postDTO, null));
    }

    @Test
    @DisplayName("updatePost - 이미지가 있는 경우 기존 이미지 삭제 후 새 이미지 업로드")
    void updatePostWithNewImages() throws IOException {
        // given
        Image oldImage = new Image();
        oldImage.setS3Key("old/image.jpg");
        oldImage.setImageUrl("https://bucket.s3.amazonaws.com/old/image.jpg");
        post.setImages(new ArrayList<>(List.of(oldImage)));

        files = List.of(multipartFile);

        given(postQueryService.getPostById(1L)).willReturn(post);
        given(multipartFile.isEmpty()).willReturn(false);
        given(s3Service.uploadFile(multipartFile)).willReturn("images/new.jpg");
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Post result = postService.updatePost(1L, postDTO, files);

        // then
        verify(s3Service).deleteFile("old/image.jpg");
        verify(s3Service).uploadFile(multipartFile);
        assertEquals(1, result.getImages().size());
        assertEquals("images/new.jpg", result.getImages().get(0).getImageUrl());
    }

    @Test
    @DisplayName("updatePost - 지원하는 handler만 handle() 메서드 실행")
    void onlyCallSupportingHandlersUpdate() throws IOException {
        //given
        given(postQueryService.getPostById(1L)).willReturn(post);
        given(handler1.supports(PostType.FREE)).willReturn(false);
        given(handler2.supports(PostType.FREE)).willReturn(true);

        //when
        postService.updatePost(1L, postDTO, Collections.emptyList());

        //then
        verify(handler1, never()).handle(any(), any());
        verify(handler2).handle(post, postDTO);
    }

    @Test
    @DisplayName("updatePost - 핸들러 실행 내용 post에 반영")
    void handlerModificationsInUpdatedPost() throws IOException {
        // given
        postDTO.setType(PostType.PLAY);
        given(postQueryService.getPostById(1L)).willReturn(post);
        given(handler1.supports(PostType.PLAY)).willReturn(true);
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        doAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            PostDTO dto = invocation.getArgument(1);

            PostHangout hangout = new PostHangout();
            hangout.setPost(post);
            hangout.setPlaceName("updated-place");

            post.setPostHangout(hangout);
            return null;
        }).when(handler1).handle(any(Post.class), eq(postDTO));

        // when
        Post result = postService.updatePost(1L, postDTO, Collections.emptyList());

        // then
        assertNotNull(result.getPostHangout());
        assertEquals("updated-place", result.getPostHangout().getPlaceName());
        verify(handler1).handle(any(Post.class), eq(postDTO));
    }

    /**
     * deletePost test
     */
    @Test
    @DisplayName("deletePost- 삭제 성공")
    void verifyDeleteCallOnSuccess() {
        // given
        post.setUser(user);
        post.setType(PostType.FREE);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postQueryService.getPostById(1L)).willReturn(post);
        given(commentService.getByTypeAndPostId(CommentType.COMMENT, 1L)).willReturn(new ArrayList<>());

        // when
        postService.deletedPost(1L, 1L);

        // then
        verify(postRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deletePost- 존재하지 않는 유저 또는 게시글 예외 발생")
    void throwWhenUserOrPostNotFoundDelete() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class,
                () -> postService.deletedPost(1L, 1L));

        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(postRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class,
                () -> postService.deletedPost(1L, 1L));
    }

    @Test
    @DisplayName("deletePost- ADMIN 유저는 모든 게시글 삭제 가능")
    void adminCanDeleteAnyPost() {
        // given
        user.setRole(UserRoleType.ADMIN);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postQueryService.getPostById(1L)).willReturn(post);
        given(commentService.getByTypeAndPostId(CommentType.COMMENT, 1L)).willReturn(new ArrayList<>());

        // when
        postService.deletedPost(1L, 1L);

        // then
        verify(postRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deletePost- QNA 글은 관리자만 삭제 가능 작성자도 예외 발생")
    void qnaDeleteOnlyByAdmin() {
        //given
        post.setType(PostType.QNA);
        user.setId(1L);
        user.setRole(UserRoleType.USER);
        post.setUser(user);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));


        //when & then
        assertThrows(IllegalArgumentException.class, () -> postService.deletedPost(1L, 1L));
    }

    @Test
    @DisplayName("deletePost- QNA 글은 관리자면 삭제 가능")
    void qnaDeleteAllowedForAdmin() {
        // given
        post.setType(PostType.QNA);
        User author = new User();
        author.setId(2L);
        post.setUser(author);
        user.setId(1L);
        user.setRole(UserRoleType.ADMIN);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postQueryService.getPostById(1L)).willReturn(post);
        given(commentService.getByTypeAndPostId(CommentType.ANSWER, 1L)).willReturn(new ArrayList<>());

        // when
        postService.deletedPost(1L, 1L);

        // then
        verify(postRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deletePost - 작성자 본인이면 삭제 가능")
    void authorCanDeleteOwnPost() {
        // given
        post.setType(PostType.TEAM);
        post.setUser(user);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postQueryService.getPostById(1L)).willReturn(post);
        given(commentService.getByTypeAndPostId(CommentType.COMMENT, 1L)).willReturn(new ArrayList<>());

        // when
        postService.deletedPost(1L, 1L);

        // then
        verify(postRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deletePost - 작성자 본인아니면 예외 발생")
    void otherCanNotDeletePost() {
        // given
        post.setType(PostType.TEAM);
        User author = new User();
        author.setId(2L);
        post.setUser(author);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> postService.deletedPost(1L, 1L));
    }

    @Test
    @DisplayName("deletePost - 게시글 삭제 시 이미지도 S3에서 삭제")
    void deleteImagesWhenDeletingPost() {
        // given
        Image image1 = new Image();
        image1.setS3Key("images/image1.jpg");
        Image image2 = new Image();
        image2.setS3Key("images/image2.jpg");
        post.setImages(List.of(image1, image2));
        post.setUser(user);
        post.setType(PostType.FREE);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postQueryService.getPostById(1L)).willReturn(post);
        given(commentService.getByTypeAndPostId(CommentType.COMMENT, 1L)).willReturn(new ArrayList<>());

        // when
        postService.deletedPost(1L, 1L);

        // then
        verify(s3Service).deleteFile("images/image1.jpg");
        verify(s3Service).deleteFile("images/image2.jpg");
        verify(postRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deletePost - QNA 게시글 삭제 시 답변 댓글들도 삭제")
    void deleteAnswersWhenDeletingQnaPost() {
        // given
        post.setType(PostType.QNA);
        user.setRole(UserRoleType.ADMIN);
        Comment answer1 = new Comment();
        answer1.setId(1L);
        Comment answer2 = new Comment();
        answer2.setId(2L);
        List<Comment> answers = List.of(answer1, answer2);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postQueryService.getPostById(1L)).willReturn(post);
        given(commentService.getByTypeAndPostId(CommentType.ANSWER, 1L)).willReturn(answers);

        // when
        postService.deletedPost(1L, 1L);

        // then
        verify(commentService).deletedComment(1L, 1L);
        verify(commentService).deletedComment(1L, 2L);
        verify(postRepository).deleteById(1L);
    }


    /**
     * updateAnswerComplete test
     */
    @Test
    @DisplayName("updateAnswerComplete- 존재하지 않는 유저 또는 게시글 예외 발생")
    void throwWhenUserOrPostNotFoundAnswer() {
        // given
        given(userRepository.existsById(1L)).willReturn(false);

        // when & then
        assertThrows(EntityNotFoundException.class,
                () -> postService.updateAnswerComplete(1L, 1L));

        // given
        given(userRepository.existsById(1L)).willReturn(true);
        given(postRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class,
                () -> postService.updateAnswerComplete(1L, 1L));
    }

    @Test
    @DisplayName("updateAnswerComplete- QNA 타입이 아닌 경우 예외 발생")
    void throwWhenNotQnaType() {
        // given
        post.setType(PostType.FREE);
        post.setUser(user);
        given(userRepository.existsById(1L)).willReturn(true);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> postService.updateAnswerComplete(1L, 1L));
    }

    @Test
    @DisplayName("updateAnswerComplete- 작성자가 아닐 경우 예외 발생")
    void throwWhenNotAuthor() {
        // given
        post.setType(PostType.QNA);
        User author = new User();
        author.setId(2L);
        post.setUser(author);
        given(userRepository.existsById(1L)).willReturn(true);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> postService.updateAnswerComplete(1L, 1L));
    }

    @Test
    @DisplayName("updateAnswerComplete- answerComplete 토글 확인")
    void toggleIsAnswerComplete() {
        // given
        post.setType(PostType.QNA);
        post.setUser(user);
        PostQnA postQnA = new PostQnA();
        postQnA.setAnswerComplete(false);
        post.setPostQnA(postQnA);

        given(userRepository.existsById(1L)).willReturn(true);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when & then
        postService.updateAnswerComplete(1L, 1L);
        assertTrue(post.getPostQnA().getAnswerComplete());

        postService.updateAnswerComplete(1L, 1L);
        assertFalse(post.getPostQnA().getAnswerComplete());
    }

    @Test
    @DisplayName("updateAnswerComplete- PostQnA가 null이면 토글 안 함")
    void doNothingIfPostQnAIsNull() {
        // given
        post.setType(PostType.QNA);
        post.setUser(user);
        post.setPostQnA(null);
        given(userRepository.existsById(1L)).willReturn(true);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        postService.updateAnswerComplete(1L, 1L);

        // then
        assertNull(post.getPostQnA());
    }

    /**
     * Helper methods test
     */
    @Test
    @DisplayName("extractS3KeyFromUrl - URL에서 S3 키 추출 성공")
    void extractS3KeyFromUrlSuccess() throws Exception {
        // given
        String imageUrl = "https://bucket.s3.amazonaws.com/images/test.jpg";

        // when
        java.lang.reflect.Method method = PostService.class.getDeclaredMethod("extractS3KeyFromUrl", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(postService, imageUrl);

        // then
        assertEquals("images/test.jpg", result);
    }

    @Test
    @DisplayName("extractS3KeyFromUrl - 잘못된 URL 시 예외 발생")
    void extractS3KeyFromUrlFailure() throws Exception {
        // given
        String invalidUrl = "invalid-url";

        // when & then
        java.lang.reflect.Method method = PostService.class.getDeclaredMethod("extractS3KeyFromUrl", String.class);
        method.setAccessible(true);

        assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(postService, invalidUrl));
    }

    @Test
    @DisplayName("rollbackUploadedFiles - S3에서 파일 삭제")
    void rollbackUploadedFilesTest() throws Exception {
        // given
        List<String> s3Keys = List.of("image1.jpg", "image2.jpg");

        // when
        java.lang.reflect.Method method = PostService.class.getDeclaredMethod("rollbackUploadedFiles", List.class);
        method.setAccessible(true);
        method.invoke(postService, s3Keys);

        // then
        verify(s3Service).deleteFile("image1.jpg");
        verify(s3Service).deleteFile("image2.jpg");
    }
}
