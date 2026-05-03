package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.annotation.HasProfile;
import com.percent99.OutSpecs.dto.ChatRoomResponseDTO;
import com.percent99.OutSpecs.repository.ChatRoomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ChatViewService {
  private final ChatRoomRepository chatRoomRepository;

  @Transactional(readOnly = true)
  @HasProfile
  public ChatRoomResponseDTO getChatRoomWithDetails(Long chatRoomId){
    ChatRoomResponseDTO chatRoomDTO = chatRoomRepository.findByIdWithDetails(chatRoomId);

    if (chatRoomDTO == null){
      throw new EntityNotFoundException("채팅방이 존재하지 않습니다.");
    }

    return chatRoomDTO;
  }

  @Transactional(readOnly = true)
  @HasProfile
  public List<ChatRoomResponseDTO> getChatRoomsWithDetails(Long userId){
    List<ChatRoomResponseDTO> chatRoomDTOs = chatRoomRepository.findAllChatRoomWithDetails(userId);

    if (chatRoomDTOs == null){
      return List.of();
    }

    return chatRoomDTOs;
  }
}
