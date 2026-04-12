package com.percent99.OutSpecs.validator;

import com.percent99.OutSpecs.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatValidator {
  private final ChatRoomRepository chatRoomRepository;

  public void validateParticipant(long chatroomId, long userId){
    boolean result = chatRoomRepository.existsByIdAndUserId(chatroomId, userId);

    if (!result) throw new AccessDeniedException("User is not a participant in the chatroom.");
  }
}
