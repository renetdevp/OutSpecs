package com.percent99.OutSpecs.controller;

import com.percent99.OutSpecs.dto.PostListViewDTO;
import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.security.CustomUserPrincipal;
import com.percent99.OutSpecs.service.PostQueryService;
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

    @GetMapping("/free")
    public String freePostList(@AuthenticationPrincipal CustomUserPrincipal principal,
                               Model model) {
        User user = principal.getUser();

        List<PostListViewDTO> popular = postQueryService.toViews(
                postQueryService.getViewCountPosts(PostType.FREE, 10), true,false);
        List<PostListViewDTO> recent = postQueryService.toViews(
                postQueryService.getRecentPosts(PostType.FREE, 20), true, false);


        model.addAttribute("user", user);
        model.addAttribute("popularPosts", popular);
        model.addAttribute("recentPosts", recent);
        model.addAttribute("postType", "FREE");
        return "post/list";
    }

    @GetMapping("/qna")
    public String qnaPostList(@AuthenticationPrincipal CustomUserPrincipal principal,
                               Model model) {
        User user = principal.getUser();

        List<PostListViewDTO> popular = postQueryService.toViews(
                postQueryService.getViewCountPosts(PostType.QNA, 10), true,false);
        List<PostListViewDTO> recent = postQueryService.toViews(
                postQueryService.getRecentPosts(PostType.QNA, 20), true, false);

        model.addAttribute("user", user);
        model.addAttribute("popularPosts",popular);
        model.addAttribute("recentPosts", recent);
        model.addAttribute("postType", "QNA");
        return "post/list";
    }

    @GetMapping("/team")
    public String teamPostList(@AuthenticationPrincipal CustomUserPrincipal principal,
                              Model model, @ModelAttribute("errorMessage") String errorMessage) {
        User user = principal.getUser();

        List<PostListViewDTO> popular = postQueryService.toViews(
                postQueryService.getViewCountPosts(PostType.TEAM, 10), true,false);
        List<PostListViewDTO> recent = postQueryService.toViews(
                postQueryService.getRecentPosts(PostType.TEAM, 20), true, false);

        model.addAttribute("user", user);
        model.addAttribute("popularPosts", popular);
        model.addAttribute("recentPosts", recent);
        model.addAttribute("postType", "TEAM");
        model.addAttribute("errorMessage", errorMessage);
        return "post/team-list";
    }

    @GetMapping("/recruit")
    public String recruitPostList(@AuthenticationPrincipal CustomUserPrincipal principal,
                               Model model) {
        User user = principal.getUser();

        List<PostListViewDTO> popular = postQueryService.toViews(
                postQueryService.getViewCountPosts(PostType.RECRUIT, 10), true,false);
        List<PostListViewDTO> recent = postQueryService.toViews(
                postQueryService.getRecentPosts(PostType.RECRUIT, 20), true, false);

        model.addAttribute("user", user);
        model.addAttribute("popularPosts", popular);
        model.addAttribute("recentPosts", recent);
        model.addAttribute("postType", "RECRUIT");
        return "post/list";
    }

    @GetMapping("/play")
    public String playPostList(@AuthenticationPrincipal CustomUserPrincipal principal,
                               Model model) {
        User user = principal.getUser();

        List<PostListViewDTO> popular = postQueryService.toViews(
                postQueryService.getViewCountPosts(PostType.PLAY, 10), true,false);
        List<PostListViewDTO> recent = postQueryService.toViews(
                postQueryService.getRecentPosts(PostType.PLAY, 20), true, false);

        model.addAttribute("user", user);
        model.addAttribute("popularPosts", popular);
        model.addAttribute("recentPosts", recent);
        model.addAttribute("postType", "PLAY");
        return "post/list";
    }
}