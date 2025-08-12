package com.percent99.OutSpecs.controller;

import com.percent99.OutSpecs.entity.UserRoleType;
import com.percent99.OutSpecs.security.CustomUserPrincipal;
import com.percent99.OutSpecs.service.AdminService;
import com.percent99.OutSpecs.service.PostService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final PostService postService;

    @GetMapping
    public String showAdmin(@AuthenticationPrincipal CustomUserPrincipal principal,
                            Model model){
        UserRoleType role = principal.getUser().getRole();
        if(!role.name().equals( "ADMIN")){
            return "redirect:/";
        }
        model.addAttribute("users",adminService.findAllUsers());
        model.addAttribute("posts",adminService.findReportedPosts());
        return "admin/admin";
    }

    @PostMapping("/users/{userId}/role")
    public String changeUserRole(@PathVariable Long userId,
                                 @RequestParam("role") UserRoleType role){
        try {
            adminService.changeUserRole(userId, role);
        }catch (IllegalStateException e){
            return "redirect:/admin?error=" + e.getMessage();
        }
        return "redirect:/admin";
    }

    @PostMapping("posts/{postId}/delete")
    public String deletePost(@AuthenticationPrincipal CustomUserPrincipal principal,
                             @PathVariable Long postId){
        Long meId = principal.getUser().getId();
        try{
            postService.deletedPost(meId, postId);
        } catch (EntityNotFoundException e){
            return "redirect:/admin?error=" + e.getMessage();
        }
        return "redirect:/admin";
    }
}