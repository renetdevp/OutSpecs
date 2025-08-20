package com.percent99.OutSpecs.controller;

import com.percent99.OutSpecs.dto.ChatMessageDTO;
import com.percent99.OutSpecs.dto.ChatRoomResponseDTO;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.security.CustomUserPrincipal;
import com.percent99.OutSpecs.service.AlanService;
import com.percent99.OutSpecs.service.ChatMessageService;
import com.percent99.OutSpecs.service.ChatRoomService;
import com.percent99.OutSpecs.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
@RequestMapping("/chats")
public class ChatController {
  private final ChatRoomService chatRoomService;
  private final ChatMessageService chatMessageService;
  private final AlanService alanService;
  private final UserService userService;

  @GetMapping
  public String chatRoomList(@AuthenticationPrincipal CustomUserPrincipal customUserPrincipal, Model model){
    Long userId = customUserPrincipal.getUser().getId();
    User user = userService.getUserById(userId);

    List<ChatRoomResponseDTO> chatRoomResponseDTOList = chatRoomService.getChatRoomResponseDTOListByUserId(userId);
    chatRoomResponseDTOList = chatMessageService.loadChatMessagesIntoChatRoomResponseDTOList(chatRoomResponseDTOList, userId);

    model.addAttribute("chatrooms", chatRoomResponseDTOList);
    model.addAttribute("userId", userId);
    model.addAttribute("user", user);

    return "chat/chatrooms";
  }

  @PostMapping
  public String createChatRoom(@AuthenticationPrincipal CustomUserPrincipal customUserPrincipal, @RequestParam("targetId") Long targetId){
    Long userId = customUserPrincipal.getUser().getId();

    chatRoomService.createChatRoom(userId, targetId);

    return "redirect:/chats";
  }

  @GetMapping("/{chatRoomId}")
  @ResponseBody
  public ChatRoomResponseDTO getChatRoomInfo(@PathVariable("chatRoomId") Long chatRoomId,
                                             @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal){
    return chatRoomService.getChatRoomResponseDTOById(chatRoomId);
  }

  @GetMapping("/alan")
  @ResponseBody
  public Map<String, String> getAlanResponse(@AuthenticationPrincipal CustomUserPrincipal customUserPrincipal,
                                             @RequestParam("question") String question,
                                             @RequestParam("questionType") String questionType){
    Long userId = customUserPrincipal.getUser().getId();

    return alanService.question(questionType, question, userId);
  }

  @GetMapping("/{chatRoomId}/messages")
  @ResponseBody
  public List<ChatMessageDTO> getChatMessages(@AuthenticationPrincipal CustomUserPrincipal customUserPrincipal,
                                              @PathVariable("chatRoomId") Long chatRoomId,
                                              @RequestParam(name = "firstCreatedAt", required = false) LocalDateTime firstCreatedAt){
    Long userId = customUserPrincipal.getUser().getId();

    if (firstCreatedAt == null) firstCreatedAt = LocalDateTime.now();

    Pageable pageable = PageRequest.of(0, 15, Sort.by("createdAt").descending());

    return chatMessageService.getChatMessageDTOByChatRoomId(chatRoomId, userId, firstCreatedAt, pageable);
  }

  @MessageMapping("/chats/{chatRoomId}")
  public void sendMessage(@DestinationVariable Long chatRoomId,
                          @Payload @Valid ChatMessageDTO chatMessageDTO,
                          @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal) {
    Long userId = customUserPrincipal.getUser().getId();

    chatMessageService.createChatMessage(chatRoomId, chatMessageDTO, userId);

    chatMessageService.sendMessage(chatRoomId, userId, chatMessageDTO);
  }
}
