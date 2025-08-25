package com.percent99.OutSpecs.controller;

import com.percent99.OutSpecs.security.CustomUserPrincipal;
import com.percent99.OutSpecs.service.AlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@RequiredArgsConstructor
@Controller
@RequestMapping("/alan")
public class AlanController {
  private final AlanService alanService;

  @GetMapping
  @ResponseBody
  public Map<String, String> getAlanResponse(@AuthenticationPrincipal CustomUserPrincipal customUserPrincipal,
                                             @RequestParam("question") String question,
                                             @RequestParam("questionType") String questionType){
    Long userId = customUserPrincipal.getUser().getId();

    return alanService.question(questionType, question, userId);
  }
}
