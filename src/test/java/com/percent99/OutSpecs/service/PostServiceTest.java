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

import java.util.ArrayList;
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
    private PostDetailHandler handler1;

    @Mock
    private PostDetailHandler handler2;

    @InjectMocks
    private PostService postService;

    private User user;
    private PostDTO postDTO;
    private Post post;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("test@abcd.efg");
        user.setRole(UserRoleType.USER);

        postDTO = new PostDTO();
        postDTO.setUserId(1L);
        postDTO.setType(PostType.FREE);
        postDTO.setTitle("test title");
        postDTO.setContent("test content");

        post = new Post();
        post.setId(1L);
        post.setTitle("old title");
        post.setContent("old content");
        post.setType(PostType.TEAM);
        post.setPostTags(new ArrayList<>());
        post.setPostHangout(new PostHangout());
        post.setPostJob(new PostJob());
        post.setPostQnA(new PostQnA());

        postService = new PostService(postRepository, userRepository, userService, List.of(handler1, handler2));

    }

    /**
     *  createPost test
     */
    @Test
    @DisplayName("createPost - 게시글 등록 성공")
    void createPostSuccessfully() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(handler1.supports(PostType.FREE)).willReturn(true);

        // when
        Post result = postService.createPost(postDTO);

        // then
        assertEquals("test title", result.getTitle());
        assertEquals(PostType.FREE, result.getType());
        verify(postRepository).save(any(Post.class));
        verify(handler1).handle(any(Post.class), eq(postDTO));
        verify(handler2, never()).handle(any(), any());
    }

    @Test
    @DisplayName("createPost - 존재하지 않는 유저 예외")
    void throwExceptionWhenUserNotFound() {
        //given
        postDTO.setUserId(999L);
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> postService.createPost(postDTO));
    }

    @Test
    @DisplayName("createPost - AIPLAY시 rate limit 감소")
    void callDecrementRateLimitForAiPlay() {
        //given
        postDTO.setType(PostType.AIPLAY);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        //when
        postService.createPost(postDTO);

        //then
        verify(userService).decrementAiRateLimit(1L);
    }

    @Test
    @DisplayName("createPost - AIPLAY 아닐 시 rate limit 감소하지 않음")
    void notCallRateLimitWhenNotAiPlay() {
        //given
        postDTO.setType(PostType.PLAY);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        //when
        postService.createPost(postDTO);

        //then
        verify(userService, never()).decrementAiRateLimit(anyLong());

    }

    @Test
    @DisplayName("createPost - 지원하는 handler만 handle() 메서드 실행")
    void onlyCallSupportingHandlers() {
        //given
        postDTO.setType(PostType.QNA);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(handler1.supports(PostType.QNA)).willReturn(false);
        given(handler2.supports(PostType.QNA)).willReturn(true);

        //when
        postService.createPost(postDTO);

        //then
        verify(handler1, never()).handle(any(), any());
        verify(handler2).handle(any(Post.class), eq(postDTO));
    }

    @Test
    @DisplayName("createPost - 필수 값 누락 시 예외 발생")
    void throwExceptionWhenRequiredFieldsMissing() {
        //given
        postDTO.setTitle(null);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> postService.createPost(postDTO));
    }

    @Test
    @DisplayName("createPost - 핸들러 실행 내용 post에 반영")
    void handlerModificationsInSavedPost() {
        //given
        postDTO.setType(PostType.PLAY);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
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
        Post result = postService.createPost(postDTO);

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
    void updatePostTest() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(handler1.supports(any())).willReturn(false);
        given(handler2.supports(any())).willReturn(false);

        // when
        Post updatedPost = postService.updatePost(1L, postDTO);

        // then
        assertEquals("test title", updatedPost.getTitle());
        assertEquals("test content", updatedPost.getContent());
        assertEquals(PostType.FREE, updatedPost.getType());
        verify(postRepository).save(updatedPost);
    }

    @Test
    @DisplayName("updatePost - 게시글 없을 시 예외")
    void throwExceptionWhenPostNotFound() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> postService.updatePost(1L, postDTO));
    }

    @Test
    @DisplayName("updatePost - 지원하는 handler만 handle() 메서드 실행")
    void onlyCallSupportingHandlersUpdate() {
        //given
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(handler1.supports(PostType.FREE)).willReturn(false);
        given(handler2.supports(PostType.FREE)).willReturn(true);

        //when
        postService.updatePost(1L, postDTO);

        //then
        verify(handler1, never()).handle(any(), any());
        verify(handler2).handle(post, postDTO);
    }

    @Test
    @DisplayName("updatePost - 기존 Post의 정보 초기화")
    void shouldClearPreviousDetailFields() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(handler1.supports(any())).willReturn(false);
        given(handler2.supports(any())).willReturn(false);

        // when
        postService.updatePost(1L, postDTO);

        // then
        assertTrue(post.getPostTags().isEmpty());
        assertNull(post.getPostHangout());
        assertNull(post.getPostJob());
        assertNull(post.getPostQnA());
    }

    @Test
    @DisplayName("updatePost - 핸들러 실행 내용 post에 반영")
    void handlerModificationsInUpdatedPost() {
        // given
        postDTO.setType(PostType.PLAY);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
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
        Post result = postService.updatePost(1L, postDTO);

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


    /**
     * updateAnswerComplete test
     */
    @Test
    @DisplayName("updateAnswerComplete- 존재하지 않는 유저 또는 게시글 예외 발생")
    void throwWhenUserOrPostNotFoundAnswer() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class,
                () -> postService.updateAnswerComplete(1L, 1L));

        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
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
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
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
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> postService.updateAnswerComplete(1L, 1L));
    }

    @Test
    @DisplayName("updateAnswerComplete- isAnswerComplete 토글 확인")
    void toggleIsAnswerComplete() {
        // given
        post.setType(PostType.QNA);
        post.setUser(user);
        post.getPostQnA().setAnswerComplete(false);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when & then
        postService.updateAnswerComplete(1L, 1L);
        assertTrue(post.getPostQnA().isAnswerComplete());

        postService.updateAnswerComplete(1L, 1L);
        assertFalse(post.getPostQnA().isAnswerComplete());
    }

    @Test
    @DisplayName("updateAnswerComplete- PostQnA가 null이면 토글 안 함")
    void doNothingIfPostQnAIsNull() {
        // given
        post.setType(PostType.QNA);
        post.setUser(user);
        post.setPostQnA(null);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        postService.updateAnswerComplete(1L, 1L);

        // then
        assertNull(post.getPostQnA());
    }
}
