package com.percent99.OutSpecs.controller;

import com.percent99.OutSpecs.dto.CommentDTO;
import com.percent99.OutSpecs.dto.PostDTO;
import com.percent99.OutSpecs.entity.Comment;
import com.percent99.OutSpecs.entity.CommentType;
import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.PostType;
import com.percent99.OutSpecs.security.CustomUserPrincipal;
import com.percent99.OutSpecs.service.CommentService;
import com.percent99.OutSpecs.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/qna")
@RequiredArgsConstructor
public class QnaController {

    private final PostService postService;
    private final CommentService commentService;

    // 전체 Q&A 리스트
    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserPrincipal principal,
                       Model model) {
        List<Post> posts = postService.getPostsByType(PostType.QNA);
        model.addAttribute("posts", posts);
        return "qna/qna_list";
    }

    // 질문 작성 폼
    @GetMapping("/new")
    public String form(@AuthenticationPrincipal CustomUserPrincipal principal,
                       Model model) {
        PostDTO postDTO = new PostDTO();
        postDTO.setUserId(principal.getUser().getId());
        postDTO.setType(PostType.QNA);
        model.addAttribute("postDto", postDTO);
        return "qna/qna_form";
    }

    // 질문 등록 처리
    @PostMapping
    public String create(
            @ModelAttribute("postDto") @Valid PostDTO dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserPrincipal principal
        ) {

        if(bindingResult.hasErrors()){
            return "qna/qna_form";
        }

        dto.setType(PostType.QNA);
        dto.setUserId(principal.getUser().getId());
        Post created = postService.createPost(dto);
        return "redirect:/qna/" + created.getId();
    }

    // 질문 상세 조회
    @GetMapping("/{postId}")
    public String detail(
            @PathVariable Long postId,
            Model model
    ) {
        Post post = postService.getPostById(postId);

        List<Comment> answers = commentService.getCommentsByPostId(postId)
                        .stream()
                        .filter(c -> c.getType() == CommentType.ANSWER)
                        .toList();

        // 2) 댓글(1단계) 맵
        Map<Long, List<Comment>> commentMap = new HashMap<>();
        for (Comment ans : answers) {
            List<Comment> comments = commentService.getCommentsByPostId(ans.getId())
                    .stream()
                    .filter(c -> c.getType() == CommentType.COMMENT)
                    .toList();
            commentMap.put(ans.getId(), comments);
        }

        // 3) 대댓글(2단계) 맵
        Map<Long, List<Comment>> replyMap = new HashMap<>();
        // commentMap.values() 중 각 댓글 리스트를 순회하며 대댓글 채우기
        for (List<Comment> comments : commentMap.values()) {
            for (Comment comment : comments) {
                List<Comment> replies = commentService.getCommentsByPostId(comment.getId())
                        .stream()
                        .filter(c -> c.getType() == CommentType.REPLY)
                        .toList();
                replyMap.put(comment.getId(), replies);
            }
        }

        model.addAttribute("post", post);
        model.addAttribute("answers",answers);
        model.addAttribute("commentMap", commentMap);
        model.addAttribute("replyMap", replyMap);
        return "qna/qna_detail";
    }

    // 질문 수정 폼
    @GetMapping("/{postId}/edit")
    public String editForm(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long postId,
            Model model
    ) {
        Post post = postService.getPostById(postId);

        model.addAttribute("post",post);
        // DTO에 기존 값 세팅
        PostDTO dto = new PostDTO();
        dto.setPostId(postId);
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setUserId(principal.getUser().getId());

        model.addAttribute("postDto", dto);

        return "qna/qna_form";
    }

    // 질문 수정 처리
    @PostMapping("/{postId}")
    public String update(
            @PathVariable Long postId,
            @ModelAttribute("postDto") @Valid PostDTO dto
    ) {
        dto.setType(PostType.QNA);
        postService.updatePost(postId, dto);
        return "redirect:/qna/" + postId;
    }

    @PostMapping("/{postId}/delete")
    public String delete(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long postId
    ) {
        Long userId = principal.getUser().getId();
        postService.deletedPost(userId, postId);
        return "redirect:/qna";
    }


    // ────────────────────────────────────────
    // 답변(Answer) 등록/삭제
    // ────────────────────────────────────────

    @PostMapping("/{postId}/answers")
    public String addAnswer(@PathVariable Long postId,
                            @AuthenticationPrincipal CustomUserPrincipal principal,
                            @RequestParam String content) {
        log.info("=========  /qna/{postId}/answers ===== 들어옴");
        CommentDTO dto = new CommentDTO();
        dto.setUserId(principal.getUser().getId());
        dto.setParentId(postId);
        dto.setType(CommentType.ANSWER);
        dto.setContent(content);
        commentService.createComment(dto);
        return "redirect:/qna/" + postId;
    }

    @PostMapping("/answers/{answerId}/delete")
    public String deleteAnswer(@PathVariable Long answerId,
                               @AuthenticationPrincipal CustomUserPrincipal principal) {

        Comment comment = commentService.getCommentById(answerId);
        Long postId = comment.getParentId();

        commentService.deletedComment(principal.getUser().getId(), answerId);
        return "redirect:/qna/" + postId;
    }

    // ────────────────────────────────────────
    // 댓글(Comment) 등록/수정/삭제
    // ────────────────────────────────────────

    @PostMapping("/{postId}/answers/{answerId}/comments")
    public String addComment(@PathVariable Long postId,
                             @PathVariable Long answerId,
                             @AuthenticationPrincipal CustomUserPrincipal principal,
                             @RequestParam String content) {
        CommentDTO dto = new CommentDTO();
        dto.setUserId(principal.getUser().getId());
        dto.setParentId(answerId);
        dto.setType(CommentType.COMMENT);
        dto.setContent(content);
        commentService.createComment(dto);
        return "redirect:/qna/" + postId;
    }

    @PostMapping("/{postId}/answers/{answerId}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long postId,
                                @PathVariable Long answerId,
                                @PathVariable Long commentId,
                                @AuthenticationPrincipal CustomUserPrincipal principal) {
        commentService.deletedComment(principal.getUser().getId(), commentId);
        return "redirect:/qna/" + postId;
    }

    // ────────────────────────────────────────
    // 대댓글(Reply) 등록/수정/삭제
    // ────────────────────────────────────────

    @PostMapping("/{postId}/answers/{answerId}/comments/{commentId}/replies")
    public String addReply(@PathVariable Long postId,
                           @PathVariable Long answerId,
                           @PathVariable Long commentId,
                           @AuthenticationPrincipal CustomUserPrincipal principal,
                           @RequestParam String content) {
        CommentDTO dto = new CommentDTO();
        dto.setUserId(principal.getUser().getId());
        dto.setParentId(commentId);
        dto.setType(CommentType.REPLY);
        dto.setContent(content);
        commentService.createComment(dto);
        return "redirect:/qna/" + postId;
    }

    @PostMapping("/{postId}/answers/{answerId}/comments/{commentId}/replies/{replyId}/delete")
    public String deleteReply(@PathVariable Long postId,
                              @PathVariable Long answerId,
                              @PathVariable Long commentId,
                              @PathVariable Long replyId,
                              @AuthenticationPrincipal CustomUserPrincipal principal) {
        commentService.deletedComment(principal.getUser().getId(), replyId);
        return "redirect:/qna/" + postId;
    }

    // 예: 답변(Edit Answer) 폼
    @GetMapping("/{postId}/answers/{answerId}/edit")
    public String editAnswerForm(
            @PathVariable Long postId,
            @PathVariable Long answerId,
            Model model
    ) {
        Comment answer = commentService.getCommentById(answerId);
        model.addAttribute("pageTitle", "답변 수정");
        model.addAttribute("postId", postId);
        model.addAttribute("actionUrl", "/qna/answers/" + answerId);
        model.addAttribute("content", answer.getContent());
        return "qna/edit_form";
    }

    // 예: 댓글(Edit Comment) 폼
    @GetMapping("/{postId}/answers/{answerId}/comments/{commentId}/edit")
    public String editCommentForm(
            @PathVariable Long postId,
            @PathVariable Long answerId,
            @PathVariable Long commentId,
            Model model
    ) {
        Comment comment = commentService.getCommentById(commentId);
        model.addAttribute("pageTitle", "댓글 수정");
        model.addAttribute("postId", postId);
        model.addAttribute("actionUrl",
                "/qna/" + postId + "/answers/" + answerId + "/comments/" + commentId);
        model.addAttribute("content", comment.getContent());
        return "qna/edit_form";
    }

    // 예: 대댓글(Edit Reply) 폼
    @GetMapping("/{postId}/answers/{answerId}/comments/{commentId}/replies/{replyId}/edit")
    public String editReplyForm(
            @PathVariable Long postId,
            @PathVariable Long answerId,
            @PathVariable Long commentId,
            @PathVariable Long replyId,
            Model model
    ) {
        Comment reply = commentService.getCommentById(replyId);
        model.addAttribute("pageTitle", "대댓글 수정");
        model.addAttribute("postId", postId);
        model.addAttribute("actionUrl",
                "/qna/" + postId
                        + "/answers/" + answerId
                        + "/comments/" + commentId
                        + "/replies/" + replyId);
        model.addAttribute("content", reply.getContent());
        return "qna/edit_form";
    }


    @PostMapping("/{postId}/answers/{answerId}")
    public String updateAnswer(
            @PathVariable Long postId,
            @PathVariable Long answerId,
            @RequestParam String content
    ) {
        CommentDTO dto = new CommentDTO();
        dto.setContent(content);
        commentService.updateComment(answerId, dto);
        // 항상 질문 상세로 redirect
        return "redirect:/qna/" + postId;
    }

    // ────────────────────────────────────────
    // 댓글 수정 처리
    // ────────────────────────────────────────
    @PostMapping("/{postId}/answers/{answerId}/comments/{commentId}")
    public String updateComment(
            @PathVariable Long postId,
            @PathVariable Long answerId,
            @PathVariable Long commentId,
            @RequestParam String content
    ) {
        CommentDTO dto = new CommentDTO();
        dto.setContent(content);
        commentService.updateComment(commentId, dto);
        return "redirect:/qna/" + postId;
    }

    // ────────────────────────────────────────
    // 대댓글 수정 처리
    // ────────────────────────────────────────
    @PostMapping("/{postId}/answers/{answerId}/comments/{commentId}/replies/{replyId}")
    public String updateReply(
            @PathVariable Long postId,
            @PathVariable Long replyId,
            @RequestParam String content
    ) {
        CommentDTO dto = new CommentDTO();
        dto.setContent(content);
        commentService.updateComment(replyId, dto);
        return "redirect:/qna/" + postId;
    }

}