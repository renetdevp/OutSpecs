package com.percent99.OutSpecs.controller;

import com.percent99.OutSpecs.dto.CommentDTO;
import com.percent99.OutSpecs.dto.ParticipationDTO;
import com.percent99.OutSpecs.dto.PostDTO;
import com.percent99.OutSpecs.dto.PostResponseDTO;
import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.security.CustomUserPrincipal;
import com.percent99.OutSpecs.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/post")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;
    private final PostQueryService postQueryService;
    private final CommentService commentService;
    private final ReactionService reactionService;
    private final ParticipationService participationService;
    private final ProfileService profileService;
    private final UserService userService;

    @GetMapping("/write")
    public String postForm(@AuthenticationPrincipal CustomUserPrincipal principal,
                           @RequestParam(required = false) PostType type,
                           @RequestParam(required = false) String title,
                           Model model) {
        if(principal == null){
            return "redirect:/users/login";
        }
        User user = profileService.getUserById(principal.getUser().getId());
        if(user == null) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다.");
        }
        if(user.getProfile() == null) {
            return "redirect:/users/profiles/new";
        }

        List<String> selectedTags = new ArrayList<>();

        PostDTO dto = new PostDTO();
        if(type != null) dto.setType(type);
        if(title != null) dto.setTitle(title);

        model.addAttribute("postDTO", dto);
        model.addAttribute("selectedTags", selectedTags);
        model.addAttribute("isEdit", false);
        model.addAttribute("user", user);
        return "post/write";
    }

    @PostMapping("/write")
    public String createPost(@AuthenticationPrincipal CustomUserPrincipal principal                             ,
                             @ModelAttribute PostDTO dto,
                             @RequestParam(value = "files", required = false)List<MultipartFile> files,
                             RedirectAttributes ra) {
        try{
            dto.setUserId(principal.getUser().getId());
            if(files == null){
                files = new ArrayList<>();
            }
            Post post = postService.createPost(dto, files);
            return "redirect:/post/" + post.getId();
        }catch (IOException e){
            log.error("게시글 생성 중 이미지 업로드 실패", e);
            ra.addFlashAttribute("errorMessage","이미지 업로드에 실패했습니다");
            return "redirect:/post/write";
        } catch (Exception e){
            log.error("게시글 생성 실패", e);
            ra.addFlashAttribute("errorMessage", "게시글 작성에 실패했습니다");
            return "redirect:/post/write";
        }
    }

    @GetMapping("/{postId}")
    public String detailPost(@AuthenticationPrincipal CustomUserPrincipal principal,
                             @PathVariable Long postId, Model model,
                             @ModelAttribute("errorMessage") String errorMessage) {
        User user = userService.getUserById(principal.getUser().getId());
        Post post = postQueryService.getPostAndIncreaseViewCount(postId);
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
    public String editPostForm(@AuthenticationPrincipal CustomUserPrincipal principal,
                                   @PathVariable Long postId, Model model) {
        User user = profileService.getUserById(principal.getUser().getId());
        PostDTO postDTO = postQueryService.getPostDTOById(postId);
        if (postDTO == null) {
            return "redirect:/post/" + postId;
        }
        List<String> selectedTags = new ArrayList<>();

        if (postDTO.getTagsInfo() != null && postDTO.getTagsInfo().getTags() != null) {
            selectedTags = Arrays.asList(postDTO.getTagsInfo().getTags().split(","));
        }else if (postDTO.getJobInfo()!= null && postDTO.getJobInfo().getTechniques() != null) {
            selectedTags = postDTO.getJobInfo().getTechniques();
        }

        model.addAttribute("user",user);
        model.addAttribute("postId", postId);
        model.addAttribute("postDTO", postDTO);
        model.addAttribute("selectedTags", selectedTags);
        model.addAttribute("isEdit", true);
        model.addAttribute("user", user);
        return "post/write";
    }

    @PostMapping("/{postId}/edit")
    public String updatePost(@AuthenticationPrincipal CustomUserPrincipal principal,
                             @PathVariable Long postId, @ModelAttribute PostDTO dto,
                             @RequestParam(value = "files", required = false)List<MultipartFile> files,
                             RedirectAttributes ra) {

        try{
            dto.setUserId(principal.getUser().getId());
            if(files == null){
                files = new ArrayList<>();
            }
            List<MultipartFile> newFiles = files.stream()
                    .filter(f->f!= null && !f.isEmpty())
                    .toList();

            Post post = postService.updatePost(postId, dto,newFiles);
            return "redirect:/post/" + post.getId();
        }catch (IOException e){
            log.error("게시글 수정 중 이미지 업로드 실패", e);
            ra.addFlashAttribute("errorMessage","이미지 업로드에 실패했습니다");
            return "redirect:/post/" + postId + "/edit";
        }catch (Exception e){
            log.error("게시글 수정 실패", e);
            ra.addFlashAttribute("errorMessage","게시글 수정에 실패했습니다"+ e.getMessage());
            return "redirect:/post/" + postId + "/edit";
        }
    }

    @PostMapping("/{postId}/delete")
    public String deletePost(@AuthenticationPrincipal CustomUserPrincipal principal,
                             @PathVariable Long postId) {
        Post post = postQueryService.getPostById(postId);
        String postType = post.getType().pathPrefix();
        Long userId = principal.getUser().getId();
        postService.deletedPost(userId, postId);

        return "redirect:/list/" + postType;
    }

    @PostMapping("/{postId}/comment")
    public String addCommentPost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                 @PathVariable Long postId, @ModelAttribute CommentDTO dto) {
        if(principal == null){
            return "redirect:/users/login";
        }
        User user = profileService.getUserById(principal.getUser().getId());
        if(user == null) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다.");
        }
        if(user.getProfile() == null) {
            return "redirect:/users/profiles/new";
        }
        dto.setUserId(principal.getUser().getId());
        commentService.createComment(dto);
        return "redirect:/post/" + postId;
    }

    @PostMapping("/{postId}/comment/{commentId}")
    public String deleteCommentPost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                    @PathVariable Long postId,
                                    @PathVariable Long commentId) {
        if(principal == null){
            return "redirect:/users/login";
        }
        User user = profileService.getUserById(principal.getUser().getId());
        if(user == null) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다.");
        }
        if(user.getProfile() == null) {
            return "redirect:/users/profiles/new";
        }
        Long userId = principal.getUser().getId();
        commentService.deletedComment(userId, commentId);
        return "redirect:/post/" + postId;
    }

    @PostMapping("/{postId}/comment/{commentId}/edit")
    public String editCommentPost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                  @PathVariable Long postId,
                                  @PathVariable Long commentId,
                                  @ModelAttribute CommentDTO dto) {
        if(principal == null){
            return "redirect:/users/login";
        }
        User user = profileService.getUserById(principal.getUser().getId());
        if(user == null) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다.");
        }
        if(user.getProfile() == null) {
            return "redirect:/users/profiles/new";
        }
        dto.setUserId(principal.getUser().getId());
        commentService.updateComment(commentId, dto);
        return "redirect:/post/" + postId;
    }

    @PostMapping("/{postId}/like")
    public String addLikePost(@AuthenticationPrincipal CustomUserPrincipal principal,
                              @PathVariable Long postId) {
        User user = principal.getUser();
        reactionService.addReaction(user, TargetType.POST, postId, ReactionType.LIKE);
        return "redirect:/post/" + postId;
    }

    @PostMapping("/{postId}/bookmark")
    public String addBookMarkPost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                  @PathVariable Long postId) {
        User user = principal.getUser();
        reactionService.addReaction(user, TargetType.POST, postId, ReactionType.BOOKMARK);
        return "redirect:/post/" + postId;
    }

    @PostMapping("/{postId}/report")
    public String addReportPost(@AuthenticationPrincipal CustomUserPrincipal principal,
                                @PathVariable Long postId) {
        User user = principal.getUser();
        reactionService.addReaction(user, TargetType.POST, postId, ReactionType.REPORT);
        return "redirect:/post/" + postId;
    }

    @PostMapping("/{postId}/team")
    public String participationTeam(@AuthenticationPrincipal CustomUserPrincipal principal,
                                    @PathVariable Long postId,
                                    @ModelAttribute ParticipationDTO dto) {
            participationService.createParticipation(dto);
        return "redirect:/post/" + postId;
    }
}