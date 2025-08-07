package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.entity.ChatRoom;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.repository.ChatRoomRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatRoomServiceTest {
  @Mock private ChatRoomRepository chatRoomRepository;
  @Mock private UserRepository userRepository;
  @InjectMocks private ChatRoomService chatRoomService;

  private User user1;
  private User user2;
  private ChatRoom chatRoom;

  @BeforeEach
  void setup(){
    user1 = new User();
    user2 = new User();

    user1.setId(1L);
    user2.setId(2L);

    chatRoom = new ChatRoom();

    chatRoom.setId(1L);
    chatRoom.setUser1(user1);
    chatRoom.setUser2(user2);
  }

  @Test
  @DisplayName("ChatRoomService.createChatRoom failed when user not found")
  void createChatRoomFailedWhenUserNotFound(){
    // given
    when(userRepository.findById(user1.getId())).thenReturn(Optional.empty());
    when(userRepository.findById(user2.getId())).thenReturn(Optional.empty());

    // when
    Object result1 = chatRoomService.createChatRoom(user1.getId(), user2.getId());
    Object result2 = chatRoomService.createChatRoom(user2.getId(), user1.getId());

    // then
    assertThat(result1).isEqualTo(null);
    assertThat(result2).isEqualTo(null);
  }

  @Test
  @DisplayName("ChatRoomService.createChatRoom failed when chatroom already exists")
  void createChatRoomFailedWhenChatRoomAlreadyExists(){
    // given
    when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
    when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));

    when(chatRoomRepository.existsByUser1AndUser2(user1, user2)).thenReturn(true);
    when(chatRoomRepository.existsByUser1AndUser2(user2, user1)).thenReturn(true);

    // when
    Object result1 = chatRoomService.createChatRoom(user1.getId(), user2.getId());
    Object result2 = chatRoomService.createChatRoom(user2.getId(), user1.getId());

    // then
    assertThat(result1).isNull();
    assertThat(result2).isNull();
  }

  @Test
  @DisplayName("ChatRoomService.createChatRoom success")
  void createChatRoomSuccess(){
    // given
    when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
    when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));

    when(chatRoomRepository.existsByUser1AndUser2(user1, user2)).thenReturn(false);
    when(chatRoomRepository.existsByUser1AndUser2(user2, user1)).thenReturn(false);

    when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);

    // when
    ChatRoom result = chatRoomService.createChatRoom(user1.getId(), user2.getId());

    // then
    assertThat(result).isEqualTo(chatRoom);
  }

  @Test
  @DisplayName("ChatRoomService.findChatRoomById failed")
  void findChatRoomByIdFailed(){
    // given & when
    Optional<ChatRoom> result = chatRoomService.findChatRoomById(null);

    // then
    assertThat(result).isEqualTo(Optional.empty());
  }

  @Test
  @DisplayName("ChatRoomService.findChatRoomById success")
  void findChatRoomByIdSuccess(){
    // given
    when(chatRoomRepository.findById(chatRoom.getId())).thenReturn(Optional.of(chatRoom));

    // when
    Optional<ChatRoom> result = chatRoomService.findChatRoomById(chatRoom.getId());

    // then
    assertThat(result).isEqualTo(Optional.of(chatRoom));
  }

  @Test
  @DisplayName("ChatRoomService.updateChatRoomById failed when chatroomId and userId is null")
  void updateChatRoomByIdFailedWhenChatRoomIdAndUserIdIsNull(){
    // given & when
    ChatRoom result = chatRoomService.updateChatRoomById(null, null);

    // then
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("ChatRoomService.updateChatRoomById failed when user not in chatroom")
  void updateChatRoomByIdFailedWhenUserNotInChatRoom(){
    // given
    when(chatRoomRepository.existsByIdAndUserId(chatRoom.getId(), user1.getId())).thenReturn(false);

    // when
    ChatRoom result = chatRoomService.updateChatRoomById(chatRoom, user1.getId());

    // then
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("ChatRoomService.updateChatRoomById success")
  void updateChatRoomByIdSuccess(){
    // given
    when(chatRoomRepository.existsByIdAndUserId(chatRoom.getId(), user1.getId())).thenReturn(true);
    when(chatRoomRepository.save(chatRoom)).thenReturn(chatRoom);

    // when
    ChatRoom result = chatRoomService.updateChatRoomById(chatRoom, user1.getId());

    // then
    assertThat(result).isEqualTo(chatRoom);
  }

  @Test
  @DisplayName("ChatRoomService.deleteChatRoomById failed when chatroomId and userId is null")
  void deleteChatRoomByIdFailedWhenChatRoomIdAndUserIdIsNull(){
    // given & when
    chatRoomService.deleteChatRoomById(null, null);

    // then
    verify(chatRoomRepository, never()).deleteById(chatRoom.getId());
  }

  @Test
  @DisplayName("ChatRoomService.deleteChatRoomById failed when user not in chatroom")
  void deleteChatRoomByIdFailedWhenUserNotInChatRoom(){
    // given
    when(chatRoomRepository.existsByIdAndUserId(chatRoom.getId(), user1.getId())).thenReturn(false);

    // when
    chatRoomService.deleteChatRoomById(chatRoom.getId(), user1.getId());

    // then
    verify(chatRoomRepository, never()).deleteById(chatRoom.getId());
  }

  @Test
  @DisplayName("ChatRoomService.deleteChatRoomById success")
  void deleteChatRoomByIdSuccess(){
    // given
    when(chatRoomRepository.existsByIdAndUserId(chatRoom.getId(), user1.getId())).thenReturn(true);

    // when
    chatRoomService.deleteChatRoomById(chatRoom.getId(), user1.getId());

    // then
    verify(chatRoomRepository, times(1)).deleteById(chatRoom.getId());
  }
}
