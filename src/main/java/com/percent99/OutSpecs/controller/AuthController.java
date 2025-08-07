package com.percent99.OutSpecs.controller;

import com.percent99.OutSpecs.dto.UserDTO;
import com.percent99.OutSpecs.security.CustomUserPrincipal;
import com.percent99.OutSpecs.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class AuthController {

    private final UserService  userService;

    @GetMapping("/signup")
    public String showRegister(Model model,
                               @AuthenticationPrincipal CustomUserPrincipal principal){
        if(principal!= null){
            return "redirect:/";
        }

        return "";
    }

    @PostMapping("/signup")
    public String postRegister(@ModelAttribute("user") @Valid UserDTO userDTO, Model model, BindingResult bindingResult){

        if(bindingResult.hasErrors()){
            return "";
        }

        if(userService.findByUsername(userDTO.getUsername()).isPresent()){
            model.addAttribute("", "이미 존재하는 회원입니다.");
            return "";
        }

        userService.registerUser(userDTO);
        return "";
    }

    @GetMapping("/login")
    public String showLogin(@AuthenticationPrincipal CustomUserPrincipal principal){
        if(principal != null){
            return "redirect:/";
        }
        return "";
    }
}