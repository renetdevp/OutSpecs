package com.percent99.OutSpecs.controller;

import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.security.CustomUserPrincipal;
import com.percent99.OutSpecs.service.PostQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/list")
@RequiredArgsConstructor
public class PostListController {

    private final PostQueryService postQueryService;

    @GetMapping("/free")
    public String freePostlist(@AuthenticationPrincipal CustomUserPrincipal principal,
                               Model model) {
        User user = principal.getUser();
        model.addAttribute("user", user);
        model.addAttribute("popularPosts", postQueryService.getViewCountPosts(PostType.FREE, 10));
        model.addAttribute("recentPosts", postQueryService.getRecentPosts(PostType.FREE, 20));
        model.addAttribute("postType", "FREE");
        return "post/list";
    }

    @GetMapping("/qna")
    public String qnaPostlist(@AuthenticationPrincipal CustomUserPrincipal principal,
                               Model model) {
        User user = principal.getUser();
        model.addAttribute("user", user);
        model.addAttribute("popularPosts", postQueryService.getViewCountPosts(PostType.QNA, 10));
        model.addAttribute("recentPosts", postQueryService.getRecentPosts(PostType.QNA, 20));
        model.addAttribute("postType", "QNA");
        return "post/list";
    }

    @GetMapping("/team")
    public String teamPostlist(@AuthenticationPrincipal CustomUserPrincipal principal,
                              Model model, @ModelAttribute("errorMessage") String errorMessage) {
        User user = principal.getUser();
        model.addAttribute("user", user);
        model.addAttribute("popularPosts", postQueryService.getViewCountPosts(PostType.TEAM, 10));
        model.addAttribute("recentPosts", postQueryService.getRecentPosts(PostType.TEAM, 20));
        model.addAttribute("postType", "TEAM");
        model.addAttribute("errorMessage", errorMessage);
        return "post/team-list";
    }

    @GetMapping("/recruit")
    public String recruitPostlist(@AuthenticationPrincipal CustomUserPrincipal principal,
                               Model model) {
        User user = principal.getUser();
        model.addAttribute("user", user);
        model.addAttribute("popularPosts", postQueryService.getViewCountPosts(PostType.RECRUIT, 10));
        model.addAttribute("recentPosts", postQueryService.getRecentPosts(PostType.RECRUIT, 20));
        model.addAttribute("postType", "RECRUIT");
        return "post/list";
    }

    @GetMapping("/play")
    public String playPostlist(@AuthenticationPrincipal CustomUserPrincipal principal,
                               Model model) {
        User user = principal.getUser();
        model.addAttribute("user", user);
        model.addAttribute("popularPosts", postQueryService.getViewCountPosts(PostType.PLAY, 10));
        model.addAttribute("recentPosts", postQueryService.getRecentPosts(PostType.PLAY, 20));
        model.addAttribute("postType", "PLAY");
        return "post/list";
    }

}
