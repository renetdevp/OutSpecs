package com.percent99.OutSpecs.interceptor;

import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.repository.ChatRoomRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@RequiredArgsConstructor
@Component
public class ChatInterceptor implements ChannelInterceptor {
  private final UserRepository userRepository;
  private final ChatRoomRepository chatRoomRepository;

  /**
   * 사용자가 채팅 채널을 subscribe하거나 채널에 메시지를 send할 때 사용자가 해당 채팅방에 존재하는 유저인지 확인하고, 존재하지 않는다면 null을 반환함으로써 거부
   * @param message
   * @param channel
   * @return
   */
  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    if (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) || StompCommand.SEND.equals(accessor.getCommand())){
      try {
        Long chatRoomId = Long.parseLong(accessor.getFirstNativeHeader("chatRoomId"));
        Principal principal = accessor.getUser();
        User user = userRepository.findByUsername(principal.getName()).orElse(null);

        if (!chatRoomRepository.existsByIdAndUserId(chatRoomId, user.getId())){
          return null;
        }
      }catch (NullPointerException e){
        e.printStackTrace();
        return null;
      }
    }

    return message;
  }
}
