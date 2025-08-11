package com.percent99.OutSpecs.controller;

import com.percent99.OutSpecs.dto.CommentDTO;
import com.percent99.OutSpecs.dto.PostDTO;
import com.percent99.OutSpecs.entity.Comment;
import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.PostType;
import com.percent99.OutSpecs.security.CustomUserPrincipal;
import com.percent99.OutSpecs.service.CommentService;
import com.percent99.OutSpecs.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/free-board")
@RequiredArgsConstructor
public class FreePostController {

    private final PostService postService;
    private final CommentService commentService;

    @GetMapping
    public String freePostForm(Model model) {
        model.addAttribute("postDTO", new PostDTO());
        model.addAttribute("isEdit", false);
        return "post/free-board-write";
    }

    @PostMapping
    public String createFreePost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                 @ModelAttribute PostDTO dto) {
        dto.setUserId(principal.getUser().getId());
        dto.setType(PostType.FREE);
        Post post = postService.createPost(dto);
        return "redirect:/free-board/" + post.getId();
    }

    @GetMapping("/latest")
    public String latestFreePost(Model model) {
        model.addAttribute("posts", postService.getRecentPosts(PostType.FREE, 20));
        return "post/free-board-list";
    }

    @GetMapping("/popular")
    public String popularFreePost(Model model) {
        model.addAttribute("posts", postService.getViewCountPosts(PostType.FREE, 10));
        return "post/free-board-list";
    }

    @GetMapping("/{postId}/edit")
    public String editFreePostForm(@PathVariable Long postId, Model model) {
        PostDTO postDTO = postService.getPostDTOById(postId);
        if (postDTO == null) {
            return "redirect:/free-board/latest";
        }
        model.addAttribute("postId", postId);
        model.addAttribute("postDTO", postDTO);
        model.addAttribute("isEdit", true);
        return "post/free-board-write";
    }

    @PostMapping("/{postId}/edit")
    public String updateFreePost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                 @PathVariable Long postId, @ModelAttribute PostDTO dto) {
        dto.setType(PostType.FREE);
        dto.setUserId(principal.getUser().getId());
        Post post = postService.updatePost(postId, dto);
        return "redirect:/free-board/" + post.getId();
    }

    @PostMapping("/{postId}/delete")
    public String deleteFreePost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                 @PathVariable Long postId) {
        Long userId = principal.getUser().getId();
        postService.deletedPost(userId, postId);
        return "redirect:/free-board/latest";
    }

    @GetMapping("/{postId}")
    public String detailFreePost(@PathVariable Long postId, Model model) {
        Post post = postService.getPostById(postId);
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("commentDTO", new CommentDTO());
        return "post/free-board-detail";
    }

    @PostMapping("/{postId}/comment")
    public String addCommentPost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                 @PathVariable Long postId, @ModelAttribute CommentDTO dto) {
        dto.setUserId(principal.getUser().getId());
        commentService.createComment(dto);
        return "redirect:/free-board/" + postId;
    }

    @PostMapping("/{postId}/comment/{commentId}")
    public String deleteCommentFreePost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                 @PathVariable Long postId,
                                 @PathVariable Long commentId) {
        Long userId = principal.getUser().getId();
        commentService.deletedComment(userId, commentId);
        return "redirect:/free-board/" + postId;
    }

    @PostMapping("/{postId}/comment/{commentId}/edit")
    public String editCommentFreePost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                      @PathVariable Long postId,
                                      @PathVariable Long commentId,
                                      @ModelAttribute CommentDTO dto) {
        dto.setUserId(principal.getUser().getId());
        commentService.updateComment(commentId, dto);
        return "redirect:/free-board/" + postId;
    }
}
