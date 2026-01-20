package com.percent99.OutSpecs.controller;

import com.percent99.OutSpecs.dto.HomePostDTO;
import com.percent99.OutSpecs.dto.PostListViewDTO;
import com.percent99.OutSpecs.entity.PostType;
import com.percent99.OutSpecs.entity.Profile;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.security.CustomUserPrincipal;
import com.percent99.OutSpecs.service.PostQueryService;
import com.percent99.OutSpecs.service.ProfileService;
import com.percent99.OutSpecs.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;
    private final ProfileService profileService;
    private final PostQueryService postQueryService;

    @GetMapping
    public String showHome(@AuthenticationPrincipal CustomUserPrincipal principal,
                           Model model){
      final long postLimit = 5L;

      Map<PostType, List<HomePostDTO>> dtos = postQueryService.getMostLikesPost(postLimit);

        if(principal != null){
             userService.findByUsername(principal.getUsername())
                     .ifPresent(user -> model.addAttribute("user",user));

             Profile profile = profileService.getProfileByUserId(principal.getUser().getId())
                     .orElse(null);

            model.addAttribute("profile",profile);
        }

      model.addAttribute("free", dtos.get(PostType.FREE));
      model.addAttribute("team", dtos.get(PostType.TEAM));
      model.addAttribute("qna", dtos.get(PostType.QNA));
      model.addAttribute("play", dtos.get(PostType.PLAY));

      return "home";
    }

    @GetMapping("/compose/route")
    public String routeCompose(
            @RequestParam String category,
            @RequestParam(name = "title", required = false, defaultValue = "") String rawTitle,
            RedirectAttributes ra
    ){
        String title = rawTitle.trim();
        final PostType type = postQueryService.resolvePostType(category);

        if(title.isEmpty()){
            ra.addAttribute("type", type.name());
            return "redirect:/post/write";
        }

        boolean exist = postQueryService.existsByTypeAndTitleLike(type,title);
        if(exist){
            String q = UriUtils.encode(title, StandardCharsets.UTF_8);
            return "redirect:/search?type=" + type.name() + "&q=" + q;
        }

        ra.addAttribute("type", type.name());
        ra.addAttribute("title", title);
        return "redirect:/post/write";
    }

    @GetMapping("/search")
    public String search(@AuthenticationPrincipal CustomUserPrincipal principal,
                         @RequestParam(required = false) PostType type,
                         @RequestParam String q,
                         Model model) {

        User user = userService.getUserById(principal.getUser().getId());
        if(user.getProfile() == null) {
            return "redirect:/users/profiles/new";
        }

        String queryStr = q.trim();
        List<PostListViewDTO> results = queryStr.isEmpty()
                ? List.of()
                : postQueryService.search(type, queryStr);

        model.addAttribute("user", user);
        model.addAttribute("q", queryStr);
        model.addAttribute("type", type);
        model.addAttribute("results", results);
        model.addAttribute("total", results.size());
        return "search/search";
    }
}