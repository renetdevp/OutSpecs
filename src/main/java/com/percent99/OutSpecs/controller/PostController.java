package com.percent99.OutSpecs.controller;

import com.percent99.OutSpecs.dto.CommentDTO;
import com.percent99.OutSpecs.dto.ParticipationDTO;
import com.percent99.OutSpecs.dto.PostDTO;
import com.percent99.OutSpecs.dto.PostResponseDTO;
import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.security.CustomUserPrincipal;
import com.percent99.OutSpecs.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostQueryService postQueryService;
    private final CommentService commentService;
    private final ReactionService reactionService;
    private final ParticipationService participationService;

    @GetMapping("/write")
    public String postForm(@AuthenticationPrincipal CustomUserPrincipal principal,
                           Model model) {
        User user = principal.getUser();
        List<String> selectedTags = new ArrayList<>();
        model.addAttribute("postDTO", new PostDTO());
        model.addAttribute("selectedTags", selectedTags);
        model.addAttribute("isEdit", false);
        model.addAttribute("user", user);
        return "post/write";
    }

    @PostMapping("/write")
    public String createPost(@AuthenticationPrincipal CustomUserPrincipal principal,
                             @ModelAttribute PostDTO dto) {
        dto.setUserId(principal.getUser().getId());
        Post post = postService.createPost(dto);
        return "redirect:/post/" + post.getId();
    }

    @GetMapping("/{postId}")
    public String detailPost(@AuthenticationPrincipal CustomUserPrincipal principal,
                             @PathVariable Long postId, Model model,
                             @ModelAttribute("errorMessage") String errorMessage) {
        Post post = postQueryService.getPostAndIncreaseViewCount(postId);
        User user = principal.getUser();
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        List<Participation> participations = participationService.getParticipationByPostId(postId);
        PostResponseDTO reactions = postQueryService.getPostReactionDetail(postId, user);

        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("participations", participations);
        model.addAttribute("reactions", reactions);
        model.addAttribute("commentDTO", new CommentDTO());
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("user", user);
        return "post/detail";
    }

    @GetMapping("/{postId}/edit")
    public String editFreePostForm(@PathVariable Long postId, Model model) {
        PostDTO postDTO = postQueryService.getPostDTOById(postId);
        if (postDTO == null) {
            return "redirect:/board";
        }
        List<String> selectedTags = new ArrayList<>();

        if (postDTO.getTagsInfo() != null && postDTO.getTagsInfo().getTags() != null) {
            selectedTags = Arrays.asList(postDTO.getTagsInfo().getTags().split(","));
        }else if (postDTO.getJobInfo()!= null && postDTO.getJobInfo().getTechniques() != null) {
            selectedTags = postDTO.getJobInfo().getTechniques();
        }
        model.addAttribute("postId", postId);
        model.addAttribute("postDTO", postDTO);
        model.addAttribute("selectedTags", selectedTags);
        model.addAttribute("isEdit", true);
        return "post/write";
    }

    @PostMapping("/{postId}/edit")
    public String updatePost(@AuthenticationPrincipal CustomUserPrincipal principal,
                             @PathVariable Long postId, @ModelAttribute PostDTO dto) {
        dto.setUserId(principal.getUser().getId());
        Post post = postService.updatePost(postId, dto);
        return "redirect:/post/" + post.getId();
    }

    @PostMapping("/{postId}/delete")
    public String deletePost(@AuthenticationPrincipal CustomUserPrincipal principal,
                             @PathVariable Long postId) {
        Long userId = principal.getUser().getId();
        postService.deletedPost(userId, postId);
        return "redirect:/board/";
    }

    @PostMapping("/{postId}/comment")
    public String addCommentPost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                 @PathVariable Long postId, @ModelAttribute CommentDTO dto) {
        dto.setUserId(principal.getUser().getId());
        commentService.createComment(dto);
        return "redirect:/post/" + postId;
    }

    @PostMapping("/{postId}/comment/{commentId}")
    public String deleteCommentPost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                    @PathVariable Long postId,
                                    @PathVariable Long commentId) {
        Long userId = principal.getUser().getId();
        commentService.deletedComment(userId, commentId);
        return "redirect:/post/" + postId;
    }

    @PostMapping("/{postId}/comment/{commentId}/edit")
    public String editCommentPost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                  @PathVariable Long postId,
                                  @PathVariable Long commentId,
                                  @ModelAttribute CommentDTO dto) {
        dto.setUserId(principal.getUser().getId());
        commentService.updateComment(commentId, dto);
        return "redirect:/post/" + postId;
    }

    @PostMapping("/{postId}/like")
    public String addLikePost(@AuthenticationPrincipal CustomUserPrincipal principal,
                              @PathVariable Long postId,
                              RedirectAttributes redirectAttributes) {
        try {
            User user = principal.getUser();
            reactionService.addReaction(user, TargetType.POST, postId, ReactionType.LIKE);
        } catch (RuntimeException e) {
            System.out.println("Exception message = " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/post/" + postId;
    }

    @PostMapping("/{postId}/bookmark")
    public String addBookMarkPost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                  @PathVariable Long postId,
                                  RedirectAttributes redirectAttributes) {
        try {
            User user = principal.getUser();
            reactionService.addReaction(user, TargetType.POST, postId, ReactionType.BOOKMARK);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/post/" + postId;
    }

    @PostMapping("/{postId}/report")
    public String addReportPost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                @PathVariable Long postId,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = principal.getUser();
            reactionService.addReaction(user, TargetType.POST, postId, ReactionType.REPORT);
            redirectAttributes.addFlashAttribute("errorMessage", "신고가 접수되었습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/post/" + postId;
    }

    @PostMapping("/{postId}/team")
    public String participationTeam(@AuthenticationPrincipal CustomUserPrincipal principal,
                                    @PathVariable Long postId,
                                    @ModelAttribute ParticipationDTO dto,
                                    RedirectAttributes redirectAttributes) {
        try {
            participationService.createParticipation(dto);
            redirectAttributes.addFlashAttribute("errorMessage", "팀 신청이 완료되었습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/post/" + postId;
    }

}
