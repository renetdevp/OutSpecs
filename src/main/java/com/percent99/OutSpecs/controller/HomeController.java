package com.percent99.OutSpecs.controller;

import com.percent99.OutSpecs.entity.Profile;
import com.percent99.OutSpecs.security.CustomUserPrincipal;
import com.percent99.OutSpecs.service.ProfileService;
import com.percent99.OutSpecs.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;
    private final ProfileService profileService;

    @GetMapping
    public String showHome(@AuthenticationPrincipal CustomUserPrincipal principal,
                           Model model){
        if(principal != null){
            userService.findByUsername(principal.getUsername())
                    .ifPresent(user -> model.addAttribute("user", user));

            Profile profile = profileService
                    .getProfileByUserId(principal.getUser().getId())
                    .orElse(null);

            model.addAttribute("profile", profile);
        }
        return "home";
    }
}