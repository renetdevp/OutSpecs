package com.percent99.OutSpecs.controller;

import com.percent99.OutSpecs.dto.ProfileDTO;
import com.percent99.OutSpecs.entity.Profile;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.security.CustomUserPrincipal;
import com.percent99.OutSpecs.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 오픈(profile) 관련 뷰를 처리하는 컨트롤러 클래스
 */
@Controller
@RequestMapping("/users/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * 현재 로그인한 사용자의 프로필을 조회합니다.<br>
     * 프로필이 존재하면 화면에 표시하고, 없으면 null로 전달합니다.
     *
     * @param principal 인증된 사용자 정보가 담긴 CustomUserPrincipal 객체
     * @param model     뷰에 전달할 속성을 저장할 Model 객체
     * @return "profile/profile_list" 뷰 이름
     */
    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserPrincipal principal,
                       Model model){

        Long userId = principal.getUser().getId();

        Profile opt = profileService.getProfileByUserId(userId)
                .orElse(null);
        if(opt == null){
            return "redirect:/users/profiles/new";
        }
        model.addAttribute("profile", opt);
        return "profile/profile_detail";
    }

    /**
     * 프로필 생성 폼을 보여줍니다.<br>
     * 이미 프로필이 존재하면 해당 프로필 상세 화면으로 리다이렉트합니다.
     *
     * @param principal 인증된 사용자 정보가 담긴 CustomUserPrincipal 객체
     * @param model     뷰에 전달할 속성을 저장할 Model 객체
     * @return "profile/profile_form" 뷰 이름 또는 리다이렉트 경로
     */
    @GetMapping("/new")
    public String createForm(@AuthenticationPrincipal CustomUserPrincipal principal,
                             Model model){
        Long userId = principal.getUser().getId();
        User user = profileService.getUserById(userId);

        if(profileService.getProfileByUserId(userId).isPresent()){
            return "redirect:/users/profiles/" + userId;
        }
        model.addAttribute("user", user);
        model.addAttribute("profileDTO", new ProfileDTO());
        return "profile/profile";
    }

    /**
     * 프로필 생성 요청을 처리합니다.<br>
     * 입력 검증에 실패하면 폼으로 다시 돌아가고, <br>
     * 성공 시 상세 화면으로 리다이렉트합니다.
     *
     * @param principal     인증된 사용자 정보가 담긴 CustomUserPrincipal 객체
     * @param profileDTO    폼에서 전달된 프로필 데이터 DTO
     * @param file          업로드된 프로필 이미지 파일(MultipartFile)
     * @param model         뷰에 전달할 에러 메시지를 저장할 Model 객체
     * @param bindingResult 검증 결과를 담은 BindingResult 객체
     * @return 상세 화면 리다이렉트 또는 폼 뷰 이름
     */
    @PostMapping
    public String create(@AuthenticationPrincipal CustomUserPrincipal principal,
                         @Valid ProfileDTO profileDTO,
                         @RequestParam(value = "file", required = false) MultipartFile file,
                         Model model,
                         BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return "profile/profile";
        }

        try{
            Profile created = profileService.registerProfile(
                    principal.getUser().getId(),
                    profileDTO,
                    file
            );
            return "redirect:/users/profiles/" + created.getUser().getId();
        }catch (IOException e){
            model.addAttribute("error", e.getMessage());
            return "profile/profile";
        }
    }

    /**
     * 주어진 userId의 프로필 상세를 조회합니다.<br>
     * 프로필이 없으면 생성 폼으로 리다이렉트합니다. <br>
     *
     * @param userId 프로필을 조회할 사용자 ID
     * @param model  뷰에 전달할 속성을 저장할 Model 객체
     * @return "profile/profile_detail" 뷰 이름 또는 생성 폼 리다이렉트
     */
    @GetMapping("/{userId}")
    public String detail(@PathVariable Long userId, Model model){

        Optional<Profile> profile = profileService.getProfileByUserId(userId);
        User user = profileService.getUserById(userId);

        if(profile.isEmpty()){
            return "redirect:/users/profiles/new";
        }
        Profile p = profile.get();
        model.addAttribute("profile",p);
        model.addAttribute("user", user);

        List<String> stacks = Optional.ofNullable(p.getStacks())
                        .map(s -> Arrays.stream(s.split(","))
                                .map(String::trim)
                                .filter(t -> !t.isEmpty())
                                .toList())
                                .orElseGet(List::of);
        model.addAttribute("stacks", stacks);
        return "profile/profile_detail";
    }


    /**
     * 주어진 userId의 프로필 수정 폼을 보여줍니다.<br>
     * 본인 ID가 아니면 상세 화면으로 리다이렉트합니다.
     *
     * @param principal 인증된 사용자 정보가 담긴 CustomUserPrincipal 객체
     * @param userId    수정할 프로필의 사용자 ID
     * @param model     뷰에 전달할 속성을 저장할 Model 객체
     * @return "profile/profile_form" 뷰 이름 또는 상세 화면 리다이렉트
     */
    @GetMapping("/{userId}/edit")
    public String editForm(@AuthenticationPrincipal CustomUserPrincipal principal,
                           @PathVariable Long userId,
                           Model model){
        if(!principal.getUser().getId().equals(userId)){
            return "redirect:/users/profiles/" + userId;
        }

        Optional<Profile> profile = profileService.getProfileByUserId(userId);
        User user = profileService.getUserById(userId);
        if(profile.isEmpty()){
            return "redirect:/users/profiles/new";
        }
        Profile p = profile.get();
        model.addAttribute("profileDTO", p);
        model.addAttribute("user",user);
        return "profile/profile";
    }

    /**
     * 프로필 수정 요청을 처리합니다. <br>
     * 입력 검증에 실패하면 폼으로 다시 돌아가고, <br>
     * 성공 시 상세 화면으로 리다이렉트합니다.
     *
     * @param principal     인증된 사용자 정보가 담긴 CustomUserPrincipal 객체
     * @param userId        수정할 프로필의 사용자 ID
     * @param profileDTO    폼에서 전달된 수정된 프로필 데이터 DTO
     * @param bindingResult 검증 결과를 담은 BindingResult 객체
     * @param file          업로드된 새 프로필 이미지 파일(MultipartFile)
     * @param model         뷰에 전달할 에러 메시지를 저장할 Model 객체
     * @return 상세 화면 리다이렉트 또는 폼 뷰 이름
     */
    @PostMapping("/{userId}")
    public String update(@AuthenticationPrincipal CustomUserPrincipal principal,
                         @PathVariable Long userId,
                         @Valid ProfileDTO profileDTO,
                         BindingResult bindingResult,
                         @RequestParam(value = "file",required = false) MultipartFile file,
                         Model model){
        if(bindingResult.hasErrors()){
            return "profile/profile";
        }
        if(!principal.getUser().getId().equals(userId)){
            return "redirect:/users/profiles/" + userId;
        }
        try{
            profileService.updateProfile(userId,profileDTO);
            if(file != null && !file.isEmpty()){
                profileService.updateProfileImage(userId,file);
            }
            return "redirect:/users/profiles/" + userId;
        }catch (IOException e){
            model.addAttribute("error",e.getMessage());
            return "redirect:/users/profiles/" + userId;
        }
    }

    /**
     * 주어진 userId의 프로필을 삭제합니다.<br>
     * 본인 ID가 아니면 상세 화면으로 리다이렉트합니다. <br>
     *
     * @param principal 인증된 사용자 정보가 담긴 CustomUserPrincipal 객체
     * @param userId    삭제할 프로필의 사용자 ID
     * @return 목록 화면("/users/profiles") 리다이렉트
     */
    @PostMapping("/{userId}/delete")
    public String delete(@AuthenticationPrincipal CustomUserPrincipal principal,
                         @PathVariable Long userId){
        if(!principal.getUser().getId().equals(userId)){
            return "redirect:/users/profiles/" + userId;
        }
        profileService.deleteProfileByUserId(userId);
        return "redirect:/users/profiles";
    }
}