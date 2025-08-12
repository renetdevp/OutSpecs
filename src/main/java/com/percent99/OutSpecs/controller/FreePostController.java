package com.percent99.OutSpecs.controller;

import com.percent99.OutSpecs.dto.CommentDTO;
import com.percent99.OutSpecs.dto.PostDTO;
import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.security.CustomUserPrincipal;
import com.percent99.OutSpecs.service.CommentService;
import com.percent99.OutSpecs.service.PostQueryService;
import com.percent99.OutSpecs.service.PostService;
import com.percent99.OutSpecs.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/free-board")
@RequiredArgsConstructor
public class FreePostController {

    private final PostService postService;
    private final PostQueryService postQueryService;
    private final CommentService commentService;
    private final ReactionService reactionService;

    @GetMapping("/write")
    public String freePostForm(Model model) {
        List<String> selectedTags = new ArrayList<>();
        model.addAttribute("postDTO", new PostDTO());
        model.addAttribute("selectedTags", selectedTags);
        model.addAttribute("isEdit", false);
        return "post/free-board-write";
    }

    @PostMapping("/write")
    public String createFreePost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                 @ModelAttribute PostDTO dto) {
        dto.setUserId(principal.getUser().getId());
        dto.setType(PostType.FREE);
        Post post = postService.createPost(dto);
        return "redirect:/free-board/" + post.getId();
    }

    @GetMapping
    public String freePostlist(Model model) {
        model.addAttribute("posts", postQueryService.getViewCountPosts(PostType.FREE, 10));
        model.addAttribute("posts", postQueryService.getRecentPosts(PostType.FREE, 20));
        return "post/free-board-list";
    }

    @GetMapping("/{postId}/edit")
    public String editFreePostForm(@PathVariable Long postId, Model model) {
        PostDTO postDTO = postQueryService.getPostDTOById(postId);
        if (postDTO == null) {
            return "redirect:/free-board/latest";
        }
        List<String> selectedTags = new ArrayList<>();

        if (postDTO.getTagsInfo() != null && postDTO.getTagsInfo().getTags() != null) {
            selectedTags = Arrays.asList(postDTO.getTagsInfo().getTags().split(","));
        }
        model.addAttribute("postId", postId);
        model.addAttribute("postDTO", postDTO);
        model.addAttribute("selectedTags", selectedTags);
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
    public String detailFreePost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                 @PathVariable Long postId, Model model) {
        Post post = postQueryService.getPostById(postId);
        User user = principal.getUser();
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        int commentsCount = commentService.countByTypeAndPostId(CommentType.COMMENT, postId);
        int likesCount = reactionService.countReactions(TargetType.POST, postId, ReactionType.LIKE);
        boolean isLiked = reactionService.isReactionExists(user, TargetType.POST, postId, ReactionType.LIKE);
        boolean isBookmarked = reactionService.isReactionExists(user, TargetType.POST, postId, ReactionType.BOOKMARK);
        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("commentsCount", commentsCount);
        model.addAttribute("likesCount", likesCount);
        model.addAttribute("isLiked", isLiked);
        model.addAttribute("isBookmarked", isBookmarked);
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

    @PostMapping("/{postId}/like")
    public String addLikeFreePost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                  @PathVariable Long postId) {
        User user = principal.getUser();
        reactionService.addReaction(user, TargetType.POST, postId, ReactionType.LIKE);
        return "redirect:/free-board/" + postId;
    }

    @PostMapping("/{postId}/bookmark")
    public String addBookMarkFreePost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                  @PathVariable Long postId) {
        User user = principal.getUser();
        reactionService.addReaction(user, TargetType.POST, postId, ReactionType.BOOKMARK);
        return "redirect:/free-board/" + postId;
    }

    @PostMapping("/{postId}/report")
    public String addReportFreePost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                      @PathVariable Long postId) {
        User user = principal.getUser();
        reactionService.addReaction(user, TargetType.POST, postId, ReactionType.REPORT);
        return "redirect:/free-board/" + postId;
    }
}
