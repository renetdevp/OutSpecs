package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.ChatMessageDTO;
import com.percent99.OutSpecs.entity.ChatMessage;
import com.percent99.OutSpecs.entity.ChatRoom;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.repository.ChatMessageRepository;
import com.percent99.OutSpecs.repository.ChatRoomRepository;
import com.percent99.OutSpecs.repository.ProfileRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatMessageServiceTest {
  @Mock private ChatMessageRepository chatMessageRepository;
  @Mock private ChatRoomService chatRoomService;
  @Mock private ChatRoomRepository chatRoomRepository;
  @Mock private UserRepository userRepository;
  @Mock private ProfileRepository profileRepository;
  @InjectMocks private ChatMessageService chatMessageService;

  private ChatMessage chatMessage;
  private ChatMessageDTO chatMessageDTO;
  private User user;
  private ChatRoom chatRoom;

  @BeforeEach
  void setup(){
    user = new User();
    user.setId(1L);

    chatMessage = new ChatMessage();
    chatMessage.setId(1L);
    chatMessage.setSender(user);

    chatMessageDTO = new ChatMessageDTO();
    chatMessageDTO.setChatRoomId(chatMessage.getId());

    chatRoom = new ChatRoom();
    chatRoom.setId(1L);
  }

  @Test
  @DisplayName("ChatMessageService.createChatMessage failed when chatroom not found")
  void createChatMessageFailedWhenChatRoomNotFound(){
    // give
    when(chatRoomService.findChatRoomById(chatMessageDTO.getChatRoomId())).thenReturn(Optional.empty());
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

    // when
    chatMessageService.createChatMessage(chatMessageDTO, user.getId());

    // then
    verify(chatMessageRepository, never()).save(chatMessage);
    verify(chatRoomService, never()).updateChatRoomById(chatRoom, user.getId());
  }

  @Test
  @DisplayName("ChatMessageService.createChatMessage failed when user not found")
  void createChatMessageFailedWhenUserNotFound(){
    // give
    when(chatRoomService.findChatRoomById(chatMessageDTO.getChatRoomId())).thenReturn(Optional.of(chatRoom));
    when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

    // when
    chatMessageService.createChatMessage(chatMessageDTO, user.getId());

    // then
    verify(chatMessageRepository, never()).save(chatMessage);
    verify(chatRoomService, never()).updateChatRoomById(chatRoom, user.getId());
  }

  @Test
  @DisplayName("ChatMessageService.createChatMessage failed when user has no profile")
  void createChatMessageFailedWhenUserHasNoProfile(){
    // give
    when(chatRoomService.findChatRoomById(chatMessageDTO.getChatRoomId())).thenReturn(Optional.of(chatRoom));
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(profileRepository.existsByUserId(user.getId())).thenReturn(false);

    // when
    chatMessageService.createChatMessage(chatMessageDTO, user.getId());

    // then
    verify(chatMessageRepository, never()).save(chatMessage);
    verify(chatRoomService, never()).updateChatRoomById(chatRoom, user.getId());
  }

  @Test
  @DisplayName("ChatMessageService.createChatMessage failed when user not in chatroom")
  void createChatMessageFailedWhenUserNotInChatRoom(){
    // give
    when(chatRoomService.findChatRoomById(chatMessageDTO.getChatRoomId())).thenReturn(Optional.of(chatRoom));
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(profileRepository.existsByUserId(user.getId())).thenReturn(true);
    when(chatRoomRepository.existsByIdAndUserId(chatRoom.getId(), user.getId())).thenReturn(false);

    // when
    chatMessageService.createChatMessage(chatMessageDTO, user.getId());

    // then
    verify(chatMessageRepository, never()).save(any(ChatMessage.class));
    verify(chatRoomService, never()).updateChatRoomById(chatRoom, user.getId());
  }

  @Test
  @DisplayName("ChatMessageService.createChatMessage success")
  void createChatMessageSuccess(){
    // give
    when(chatRoomService.findChatRoomById(chatMessageDTO.getChatRoomId())).thenReturn(Optional.of(chatRoom));
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(profileRepository.existsByUserId(user.getId())).thenReturn(true);
    when(chatRoomRepository.existsByIdAndUserId(chatRoom.getId(), user.getId())).thenReturn(true);
    when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(chatMessage);
    when(chatRoomService.updateChatRoomById(chatRoom, user.getId())).thenReturn(chatRoom);

    // when
    chatMessageService.createChatMessage(chatMessageDTO, user.getId());

    // then
    verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
    verify(chatRoomService, times(1)).updateChatRoomById(chatRoom, user.getId());
  }

  @Test
  @DisplayName("ChatMessageService.findAllByChatRoomId failed when user not in chatroom")
  void findAllByChatRoomIdFailedWhenUserNotInChatRoom(){
    // given
    when(chatRoomRepository.existsByIdAndUserId(chatRoom.getId(), user.getId())).thenReturn(false);

    // when
    List<ChatMessage> result = chatMessageService.findAllByChatRoomId(chatRoom.getId(), user.getId());

    // then
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("ChatMessageService.findAllByChatRoomId success")
  void findAllByChatRoomIdSuccess(){
    // given
    when(chatRoomRepository.existsByIdAndUserId(chatRoom.getId(), user.getId())).thenReturn(true);
    when(chatMessageRepository.findAllByChatRoomId(chatRoom.getId())).thenReturn(List.of(chatMessage));

    // when
    List<ChatMessage> result = chatMessageService.findAllByChatRoomId(chatRoom.getId(), user.getId());

    // then
    assertThat(result).isEqualTo(List.of(chatMessage));
  }

  @Test
  @DisplayName("ChatMessageService.updateChatMessage failed when user is not message sender")
  void updateChatMessageFailedWhenUserIsNotMessageSender(){
    // given
    User tempUser = new User();
    tempUser.setId(2L);

    chatMessage.setSender(tempUser);

    // when
    ChatMessage result = chatMessageService.updateChatMessage(chatMessage, user.getId());

    // then
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("ChatMessageService.updateChatMessage Success")
  void updateChatMessageSuccess(){
    // given
    when(chatMessageRepository.save(chatMessage)).thenReturn(chatMessage);

    // when
    ChatMessage result = chatMessageService.updateChatMessage(chatMessage, user.getId());

    // then
    assertThat(result).isEqualTo(chatMessage);
  }

  @Test
  @DisplayName("ChatMessageService.deleteAllChatMessages failed when user not in chatroom")
  void deleteAllChatMessagesFailedWhenUserNotInChatRoom(){
    // given
    when(chatRoomRepository.existsByIdAndUserId(chatRoom.getId(), user.getId())).thenReturn(false);

    // when
    chatMessageService.deleteAllChatMessages(chatRoom.getId(), user.getId());

    // then
    verify(chatMessageRepository, never()).deleteAllByChatRoomIdAndUserId(chatRoom.getId(), user.getId());
  }

  @Test
  @DisplayName("ChatMessageService.deleteAllChatMessages success")
  void deleteAllChatMessagesSuccess(){
    // given
    when(chatRoomRepository.existsByIdAndUserId(chatRoom.getId(), user.getId())).thenReturn(true);

    // when
    chatMessageService.deleteAllChatMessages(chatRoom.getId(), user.getId());

    // then
    verify(chatMessageRepository, times(1)).deleteAllByChatRoomIdAndUserId(chatRoom.getId(), user.getId());
  }

  @Test
  @DisplayName("ChatMessageService.deleteChatMessage failed when chatMessage not found")
  void deleteChatMessageFailedWhenChatMessageNotFound(){
    // given
    when(chatMessageRepository.findById(chatMessage.getId())).thenReturn(Optional.empty());

    // when
    chatMessageService.deleteChatMessage(user.getId(), chatMessage.getId());

    // then
    verify(chatMessageRepository, never()).deleteAllByChatRoomIdAndUserId(chatRoom.getId(), user.getId());
  }

  @Test
  @DisplayName("ChatMessageService.deleteChatMessage failed when user is not sender")
  void deleteChatMessageFailedWhenUserIsNotSender(){
    // given
    User tempUser = new User();
    tempUser.setId(2L);

    chatMessage.setSender(tempUser);

    when(chatMessageRepository.findById(chatMessage.getId())).thenReturn(Optional.of(chatMessage));

    // when
    chatMessageService.deleteChatMessage(user.getId(), chatMessage.getId());

    // then
    verify(chatMessageRepository, never()).deleteAllByChatRoomIdAndUserId(chatRoom.getId(), user.getId());
  }

  @Test
  @DisplayName("ChatMessageService.deleteChatMessage success")
  void deleteChatMessagesSuccess(){
    // given
    when(chatMessageRepository.findById(chatMessage.getId())).thenReturn(Optional.of(chatMessage));

    // when
    chatMessageService.deleteChatMessage(user.getId(), chatMessage.getId());

    // then
    verify(chatMessageRepository, times(1)).deleteById(chatMessage.getId());
  }
}
