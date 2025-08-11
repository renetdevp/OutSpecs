package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.ChatRoomResponseDTO;
import com.percent99.OutSpecs.entity.ChatRoom;
import com.percent99.OutSpecs.entity.Profile;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.repository.ChatRoomRepository;
import com.percent99.OutSpecs.repository.ProfileRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 사용자들이 채팅을 주고받는 채팅방을 관리하는 service 객체.<br>
 */
@RequiredArgsConstructor
@Service
public class ChatRoomService {
  private final ChatRoomRepository chatRoomRepository;
  private final UserRepository userRepository;
  private final ProfileRepository profileRepository;

  @Transactional
  public ChatRoom createChatRoom(Long userId, Long targetId){
    User user1 = userRepository.findById(userId).orElse(null);
    User user2 = userRepository.findById(targetId).orElse(null);

    if (user1==null || user2==null) return null;

    if (!profileRepository.existsByUserId(userId)) return null;
    if (!profileRepository.existsByUserId(targetId)) return null;

    if (chatRoomRepository.existsByUser1AndUser2(user1, user2)) return null;
    if (chatRoomRepository.existsByUser1AndUser2(user2, user1)) return null;

    ChatRoom chatRoom = new ChatRoom();

//    if (targetId.equals(CHATBOT_USER_ID)) chatRoom.setIsChatbot(true);
//    else chatRoom.setIsChatbot(false);
    chatRoom.setUser1(user1);
    chatRoom.setUser2(user2);
    chatRoom.setChatbot(false);
    chatRoom.setLastMessageId(null);

    return chatRoomRepository.save(chatRoom);
  }

  @Transactional(readOnly = true)
  public List<ChatRoom> findChatRoomByUsername(String username){
    if (username==null || username.isBlank()) return List.of();

    return chatRoomRepository.findByUsername(username);
  }

  public List<ChatRoomResponseDTO> getChatRoomResponseDTOListByUsername(String username){
    if (username==null || username.isBlank()) return List.of();

    List<ChatRoom> chatRooms = this.findChatRoomByUsername(username);

    return convertChatRoomListToDTOList(chatRooms);
  }

  /**
   * 입력받은 chatRoomId 값과 같은 id 값을 가진 채팅방을 반한
   * @param chatRoomId 검색하고자 하는 채팅방 id 값
   * @return chatRoomId 값과 일치하는 id 값을 가진 채팅방
   */
  @Transactional(readOnly = true)
  public Optional<ChatRoom> findChatRoomById(Long chatRoomId){
    if (chatRoomId == null) return Optional.empty();

    return chatRoomRepository.findById(chatRoomId);
  }

  public ChatRoomResponseDTO getChatRoomResponseDTOById(Long chatRoomId){
    if (chatRoomId == null) return null;

    ChatRoom chatRoom = this.findChatRoomById(chatRoomId).orElse(null);

    if (chatRoom == null) return null;

    return convertChatRoomToDTO(chatRoom);
  }

  @Transactional
  public ChatRoom updateChatRoomById(ChatRoom chatRoom, Long userId){
    if (chatRoom==null || userId==null) return null;
    if (!chatRoomRepository.existsByIdAndUserId(chatRoom.getId(), userId)) return null;

    return chatRoomRepository.save(chatRoom);
  }

  @Transactional
  public void deleteChatRoomById(Long chatRoomId, Long userId){
    if (chatRoomId==null || userId==null) return;
    if (!chatRoomRepository.existsByIdAndUserId(chatRoomId, userId)) return;

    chatRoomRepository.deleteById(chatRoomId);
  }

  /**
   * ChatRoom List를 ChatRoomResponseDTO List로 변환하는 메소드
   * @param chatRooms 변환할 ChatRoom List
   * @return 변환된 ChatRoomResponseDTO List를 반환
   */
  @Transactional(readOnly = true)
  private List<ChatRoomResponseDTO> convertChatRoomListToDTOList(List<ChatRoom> chatRooms){
    List<ChatRoomResponseDTO> result = new ArrayList<>();
    Map<Long, Profile> userProfiles = new HashMap<>();
    Set<Long> userIdSet = new HashSet<>();

    for (ChatRoom chatRoom: chatRooms){
      userIdSet.add(chatRoom.getUser1().getId());
      userIdSet.add(chatRoom.getUser2().getId());
    }

    List<Profile> profiles = profileRepository.findByUserIdIn(userIdSet);

    for (Profile profile: profiles) userProfiles.put(profile.getUserId(), profile);

    for (ChatRoom chatRoom: chatRooms){
      Long user1Id = chatRoom.getUser1().getId();
      Long user2Id = chatRoom.getUser2().getId();

      result.add(convertChatRoomToDTO(chatRoom, userProfiles.get(user1Id), userProfiles.get(user2Id)));
    }

    return result;
  }

  /**
   * ChatRoom을 ChatRoomResponseDTO로 변환하는 메소드
   * @param chatRoom 변환할 ChatRoom
   * @param user1Profile user1의 profile
   * @param user2Profile user2의 profile
   * @return ChatRoom을 ChatRoomResponseDTO로 변환
   */
  private ChatRoomResponseDTO convertChatRoomToDTO(ChatRoom chatRoom, Profile user1Profile, Profile user2Profile){
    if (chatRoom == null) return null;

    ChatRoomResponseDTO result = new ChatRoomResponseDTO();

    result.setChatRoomId(chatRoom.getId());
    result.setChatRoomIsChatBot(chatRoom.isChatbot());
    result.setChatRoomLastMessageId(chatRoom.getLastMessageId());

    if (user1Profile != null) {
      result.setUser1Id(user1Profile.getUserId());
      result.setUser1Nickname(user1Profile.getNickname());
      result.setUser1ImageUrl(user1Profile.getImageUrl());
    }

    if (user2Profile != null) {
      result.setUser2Id(user2Profile.getUserId());
      result.setUser2Nickname(user2Profile.getNickname());
      result.setUser2ImageUrl(user2Profile.getImageUrl());
    }

    return result;
  }

  /**
   * ChatRoom을 ChatRoomResponseDTO로 변환하는 메소드
   * @param chatRoom 변환할 ChatRoom
   * @return ChatRoom을 ChatRoomResponseDTO로 변환
   */
  private ChatRoomResponseDTO convertChatRoomToDTO(ChatRoom chatRoom){
    if (chatRoom == null) return null;

    Profile user1Profile = profileRepository.findByUserId(chatRoom.getUser1().getId()).orElse(null);
    Profile user2Profile = profileRepository.findByUserId(chatRoom.getUser2().getId()).orElse(null);

    if (user1Profile==null || user2Profile==null) return null;

    return this.convertChatRoomToDTO(chatRoom, user1Profile, user2Profile);
  }

  public boolean isChatRoomParticipant(Long chatRoomId, Long userId){
    return chatRoomRepository.existsByIdAndUserId(chatRoomId, userId);
  }
}
