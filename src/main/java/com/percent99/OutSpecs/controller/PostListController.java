package com.percent99.OutSpecs.controller;

import com.percent99.OutSpecs.dto.PostListViewDTO;
import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.security.CustomUserPrincipal;
import com.percent99.OutSpecs.service.PostQueryService;
import com.percent99.OutSpecs.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/list")
@RequiredArgsConstructor
public class PostListController {

    private final PostQueryService postQueryService;
    private final ProfileService profileService;

    @GetMapping("/{type}")
    public String postList(@PathVariable String type,
                           @AuthenticationPrincipal CustomUserPrincipal principal,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "5") int size,
                           @RequestParam(defaultValue = "false") boolean fragment,
                           Model model) {

        PostType postType = parsePostType(type);
        User user = getCurrentUser(principal);
      
        List<PostListViewDTO> popular = postQueryService.toViews(
                postQueryService.getLikePosts(postType, 10), true, false);
        Slice<Post> recentSlice = postQueryService.getRecentPosts(user, postType, page, size);
        List<PostListViewDTO> recent = postQueryService.toViews(recentSlice.getContent(), true, false);

        model.addAttribute("user", user);
        model.addAttribute("popularPosts", popular);
        model.addAttribute("recentPosts", recent);
        model.addAttribute("hasNext", recentSlice.hasNext());
        model.addAttribute("postType", postType);

        if(fragment) {
            return type.equals("team") ? "post/team-list :: postListFragment" : "post/list :: postListFragment";
        }

        return type.equals("team") ? "post/team-list" : "post/list";
    }


    @GetMapping("/{type}/filter")
    public String filterList(@PathVariable String type,
                             @RequestParam(required = false) List<String> tags,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "5") int size,
                             @RequestParam(defaultValue = "false") boolean fragment,
                             @AuthenticationPrincipal CustomUserPrincipal principal,
                             Model model) {

        PostType postType = parsePostType(type);
        if (tags == null) {
            return "redirect:/list/" + type;
        }
        User user = getCurrentUser(principal);

        List<PostListViewDTO> popular = postQueryService.toViews(
                postQueryService.getLikePosts(postType, 10), true, false);
        Slice<Post> recentSlice = postQueryService.getFilteredPosts(postType, tags, page, size);
        List<PostListViewDTO> recent = postQueryService.toViews(recentSlice.getContent(), true, false);

        model.addAttribute("user", user);
        model.addAttribute("popularPosts", popular);
        model.addAttribute("recentPosts", recent);
        model.addAttribute("hasNext", recentSlice.hasNext());
        model.addAttribute("postType", postType);
        model.addAttribute("selectedTags", tags);

        return fragment ? "post/list :: postListFragment" : "post/list";
    }

    // 헬퍼 메서드들
    private PostType parsePostType(String pathPrefix) {
        for (PostType postType : PostType.values()) {
            if (postType.pathPrefix().equals(pathPrefix)) {
                return postType;
            }
        }
        throw new IllegalArgumentException("Invalid post type path: " + pathPrefix);
    }

    private User getCurrentUser(CustomUserPrincipal principal) {
        if (principal == null) {
            return null;
        }
        Long userId = principal.getUser().getId();
        return profileService.getUserById(userId);
    }

}