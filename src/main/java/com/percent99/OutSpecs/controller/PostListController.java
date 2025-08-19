package com.percent99.OutSpecs.controller;

import com.percent99.OutSpecs.dto.PostListViewDTO;
import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.security.CustomUserPrincipal;
import com.percent99.OutSpecs.service.PostQueryService;
import com.percent99.OutSpecs.service.ProfileService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/free")
    public String freePostList(@AuthenticationPrincipal CustomUserPrincipal principal,
                               Model model) {
        User user = null;
        if(principal != null) {
            Long userId = principal.getUser().getId();
            user = profileService.getUserById(userId); }

        List<PostListViewDTO> popular = postQueryService.toViews(
                postQueryService.getViewCountPosts(PostType.FREE, 10), true,false);
        List<PostListViewDTO> recent = postQueryService.toViews(
                postQueryService.getRecentPosts(PostType.FREE, 20), true, false);


        model.addAttribute("user", user);
        model.addAttribute("popularPosts", popular);
        model.addAttribute("recentPosts", recent);
        model.addAttribute("postType", PostType.FREE);
        return "post/list";
    }

    @GetMapping("/free/filter")
    public String freeFilterList(@RequestParam(required = false) List<String> tags,
                                 @AuthenticationPrincipal CustomUserPrincipal principal,
                                 Model model) {
        if(tags == null) return "redirect:/list/free";
        User user = null;
        if(principal != null) {
            Long userId = principal.getUser().getId();
            user = profileService.getUserById(userId); }

        List<PostListViewDTO> popular = postQueryService.toViews(
                postQueryService.getViewCountPosts(PostType.FREE, 10), true,false);
        List<PostListViewDTO> recent = postQueryService.toViews(postQueryService.getTagPosts(PostType.FREE, tags), true, false);

        model.addAttribute("user", user);
        model.addAttribute("popularPosts", popular);
        model.addAttribute("recentPosts", recent);
        model.addAttribute("postType", PostType.FREE);
        model.addAttribute("selectedTags", tags);
        return "post/list";
    }

    @GetMapping("/qna")
    public String qnaPostList(@AuthenticationPrincipal CustomUserPrincipal principal,
                               Model model) {
        User user = null;
        if(principal != null) {
            Long userId = principal.getUser().getId();
            user = profileService.getUserById(userId); }

        List<PostListViewDTO> popular = postQueryService.toViews(
                postQueryService.getViewCountPosts(PostType.QNA, 10), true,false);
        List<PostListViewDTO> recent = postQueryService.toViews(
                postQueryService.getRecentPosts(PostType.QNA, 20), true, false);

        model.addAttribute("user", user);
        model.addAttribute("popularPosts",popular);
        model.addAttribute("recentPosts", recent);
        model.addAttribute("postType", PostType.QNA);
        return "post/list";
    }

    @GetMapping("/qna/filter")
    public String qnaFilterList(@RequestParam(required = false) List<String> tags,
                                 @AuthenticationPrincipal CustomUserPrincipal principal,
                                 Model model) {
        if(tags == null) return "redirect:/list/qna";
        User user = null;
        if(principal != null) {
            Long userId = principal.getUser().getId();
            user = profileService.getUserById(userId); }

        List<PostListViewDTO> popular = postQueryService.toViews(
                postQueryService.getViewCountPosts(PostType.QNA, 10), true,false);
        List<PostListViewDTO> recent = postQueryService.toViews(postQueryService.getTagPosts(PostType.QNA ,tags), true, false);

        model.addAttribute("user", user);
        model.addAttribute("popularPosts", popular);
        model.addAttribute("recentPosts", recent);
        model.addAttribute("postType", PostType.QNA);
        model.addAttribute("selectedTags", tags);
        return "post/list";
    }

    @GetMapping("/team")
    public String teamPostList(@AuthenticationPrincipal CustomUserPrincipal principal,
                              Model model, @ModelAttribute("errorMessage") String errorMessage) {
        User user = null;
        if(principal != null) {
            Long userId = principal.getUser().getId();
            user = profileService.getUserById(userId); }

        List<PostListViewDTO> popular = postQueryService.toViews(
                postQueryService.getViewCountPosts(PostType.TEAM, 10), true,false);
        List<PostListViewDTO> recent = postQueryService.toViews(
                postQueryService.getRecentPosts(PostType.TEAM, 20), true, false);

        model.addAttribute("user", user);
        model.addAttribute("popularPosts", popular);
        model.addAttribute("recentPosts", recent);
        model.addAttribute("postType", PostType.TEAM);
        model.addAttribute("errorMessage", errorMessage);
        return "post/team-list";
    }

    @GetMapping("/recruit")
    public String recruitPostList(@AuthenticationPrincipal CustomUserPrincipal principal,
                               Model model) {
        User user = null;
        if(principal != null) {
            Long userId = principal.getUser().getId();
            user = profileService.getUserById(userId); }

        List<PostListViewDTO> popular = postQueryService.toViews(
                postQueryService.getViewCountPosts(PostType.RECRUIT, 10), true,false);
        List<PostListViewDTO> recent = postQueryService.toViews(
                postQueryService.getRecentPosts(PostType.RECRUIT, 20), true, false);

        model.addAttribute("user", user);
        model.addAttribute("popularPosts", popular);
        model.addAttribute("recentPosts", recent);
        model.addAttribute("postType", PostType.RECRUIT);
        return "post/list";
    }

    @GetMapping("/recruit/filter")
    public String recruitFilterList(@RequestParam(required = false) List<String> tags,
                                @AuthenticationPrincipal CustomUserPrincipal principal,
                                Model model) {
        if(tags == null) return "redirect:/list/recruit";
        User user = null;
        if(principal != null) {
            Long userId = principal.getUser().getId();
            user = profileService.getUserById(userId); }

        List<PostListViewDTO> popular = postQueryService.toViews(
                postQueryService.getViewCountPosts(PostType.RECRUIT, 10), true,false);
        List<PostListViewDTO> recent = postQueryService.toViews(postQueryService.getTechPosts(tags), true, false);

        model.addAttribute("user", user);
        model.addAttribute("popularPosts", popular);
        model.addAttribute("recentPosts", recent);
        model.addAttribute("postType", PostType.RECRUIT);
        model.addAttribute("selectedTags", tags);
        return "post/list";
    }

    @GetMapping("/play")
    public String playPostList(@AuthenticationPrincipal CustomUserPrincipal principal,
                               Model model) {
        User user = null;
        if(principal != null) {
            Long userId = principal.getUser().getId();
            user = profileService.getUserById(userId); }

        List<PostListViewDTO> popular = postQueryService.toViews(
                postQueryService.getViewCountPosts(PostType.PLAY, 10), true,false);
        List<PostListViewDTO> recent = postQueryService.toViews(
                postQueryService.getRecentPosts(PostType.PLAY, 20), true, false);

        model.addAttribute("user", user);
        model.addAttribute("popularPosts", popular);
        model.addAttribute("recentPosts", recent);
        model.addAttribute("postType", PostType.PLAY);
        return "post/list";
    }

    @GetMapping("/play/filter")
    public String playFilterList(@RequestParam(required = false) String tags,
                                @AuthenticationPrincipal CustomUserPrincipal principal,
                                Model model) {
        if(tags == null) return "redirect:/list/play";
        User user = null;
        if(principal != null) {
            Long userId = principal.getUser().getId();
            user = profileService.getUserById(userId); }

        List<PostListViewDTO> popular = postQueryService.toViews(
                postQueryService.getViewCountPosts(PostType.PLAY, 10), true,false);
        List<PostListViewDTO> recent = postQueryService.toViews(postQueryService.getPlacePosts(tags), true, false);

        model.addAttribute("user", user);
        model.addAttribute("popularPosts", popular);
        model.addAttribute("recentPosts", recent);
        model.addAttribute("postType", PostType.PLAY);
        model.addAttribute("selectedTags", tags);
        return "post/list";
    }

}