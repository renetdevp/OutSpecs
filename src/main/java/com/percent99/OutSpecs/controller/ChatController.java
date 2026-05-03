package com.percent99.OutSpecs.controller;

import com.percent99.OutSpecs.dto.ChatMessageDTO;
import com.percent99.OutSpecs.dto.ChatRoomResponseDTO;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.security.CustomUserPrincipal;
import com.percent99.OutSpecs.service.ChatMessageService;
import com.percent99.OutSpecs.service.ChatRoomService;
import com.percent99.OutSpecs.service.ChatViewService;
import com.percent99.OutSpecs.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/chats")
public class ChatController {
  private final ChatRoomService chatRoomService;
  private final ChatMessageService chatMessageService;
  private final ChatViewService chatViewService;
  private final UserService userService;

  private static final Long DEFAULT_MESSAGES_LIMIT = 30L;

  @Value("${websocket.SERVER_URL}")
  private String webSocketServerUrl;

  @GetMapping
  public String chatRoomList(@AuthenticationPrincipal CustomUserPrincipal customUserPrincipal, Model model) {
    Long userId = customUserPrincipal.getUser().getId();
    User user = userService.getUserById(userId);

    List<ChatRoomResponseDTO> chatRoomResponseDTOList = chatViewService.getChatRoomsWithDetails(userId);

    model.addAttribute("chatrooms", chatRoomResponseDTOList);
    model.addAttribute("userId", userId);
    model.addAttribute("user", user);
    model.addAttribute("webSocketServerUrl", webSocketServerUrl);

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
    Long userId = customUserPrincipal.getUser().getId();

    if (!chatRoomService.isChatRoomParticipant(chatRoomId, userId)){
      throw new IllegalStateException("채팅방 참여자가 아닙니다.");
    }

    return chatViewService.getChatRoomWithDetails(chatRoomId);
  }

  @GetMapping("/{chatRoomId}/messages")
  @ResponseBody
  public List<ChatMessageDTO> getChatMessages(@AuthenticationPrincipal CustomUserPrincipal customUserPrincipal,
                                              @PathVariable("chatRoomId") Long chatRoomId,
                                              @RequestParam(name = "firstCreatedAt", required = false) LocalDateTime firstCreatedAt){
    Long userId = customUserPrincipal.getUser().getId();

    if (firstCreatedAt == null) firstCreatedAt = LocalDateTime.now();

    return chatMessageService.getRecentChatMessageDTO(chatRoomId, userId, firstCreatedAt, DEFAULT_MESSAGES_LIMIT);
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
