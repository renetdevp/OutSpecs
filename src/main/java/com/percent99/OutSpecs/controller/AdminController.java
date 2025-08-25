package com.percent99.OutSpecs.controller;

import com.percent99.OutSpecs.entity.UserRoleType;
import com.percent99.OutSpecs.security.CustomUserPrincipal;
import com.percent99.OutSpecs.service.AdminService;
import com.percent99.OutSpecs.service.PostService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public String showAdmin(@AuthenticationPrincipal CustomUserPrincipal principal,
                            Model model){
        model.addAttribute("users",adminService.findAllUsers());
        model.addAttribute("posts",adminService.findReportedPosts());
        model.addAttribute("user",principal.getUser());
        return "admin/admin";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{userId}/role")
    public String changeUserRole(@AuthenticationPrincipal CustomUserPrincipal principal,
                                 @PathVariable Long userId,
                                 @RequestParam("role") UserRoleType role){
        try {
            adminService.changeUserRole(userId, role);
        }catch (IllegalStateException e){
            return "redirect:/admin?error=" + e.getMessage();
        }
        return "redirect:/admin";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/posts/{postId}/delete")
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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{userId}/ban")
    public String banUserByAdmin(@PathVariable Long userId){

        try {
            adminService.banUser(userId);
        }catch (Exception e){
            return "redirect:/admin?error=" + e.getMessage();
        }
        return "redirect:/admin";
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{userId}/unban")
    public String unbanUserByAdmin(@PathVariable Long userId){

        try {
            adminService.unBanUser(userId);
        }catch (Exception e){
            return "redirect:/admin?error=" + e.getMessage();
        }
        return "redirect:/admin";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{userId}/delete")
    public String deleteUserByAdmin(@PathVariable Long userId){

        try{
            adminService.deleteUser(userId);
        }catch (EntityNotFoundException e){
            return "redirect:/admin?error=" + e.getMessage();
        }
        return "redirect:/admin";
    }
}