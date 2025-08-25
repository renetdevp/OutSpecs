package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.entity.NotificationType;
import com.percent99.OutSpecs.entity.Profile;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.repository.NotificationRepository;
import com.percent99.OutSpecs.repository.ProfileRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User sender;
    private User receiver;
    private Profile senderProfile;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(1L);

        receiver = new User();
        receiver.setId(2L);

        senderProfile = new Profile();
        senderProfile.setNickname("테스터");
    }

    @Test
    @DisplayName("sendNotification - 알림 보내기 성공")
    void sendNotificationTest() {
        // given
        given(userRepository.existsById(sender.getId())).willReturn(true);
        given(userRepository.existsById(receiver.getId())).willReturn(true);
        given(profileRepository.findById(sender.getId())).willReturn(Optional.of(senderProfile));

        // when
        notificationService.sendNotification(sender, receiver, NotificationType.FOLLOW, 100L);

        // then
        then(notificationRepository).should().save(argThat(notification ->
                notification.getSenderId().equals(sender) &&
                        notification.getReceiverId().equals(receiver) &&
                        notification.getTargetType() == NotificationType.FOLLOW &&
                        notification.getTargetId().equals(100L) &&
                        notification.getMessage().equals("테스터님이 당신을 팔로우했습니다.") &&
                        !notification.getIsRead() &&
                        notification.getCreatedAt() != null
        ));
    }

    @Test
    @DisplayName("sendNotification - 보내는 유저 없을 시 예외")
    void notExistsSender() {
        // given
        given(userRepository.existsById(sender.getId())).willReturn(false);

        // when & then
        assertThrows(EntityNotFoundException.class, () ->
                notificationService.sendNotification(sender, receiver, NotificationType.FOLLOW, 100L)
        );
    }

    @Test
    @DisplayName("sendNotification - 받는 유저 없을 시 예외")
    void notExistsReceiver() {
        // given
        given(userRepository.existsById(sender.getId())).willReturn(true);
        given(userRepository.existsById(receiver.getId())).willReturn(false);

        // when & then
        assertThrows(EntityNotFoundException.class, () ->
                notificationService.sendNotification(sender, receiver, NotificationType.FOLLOW, 100L)
        );
    }

    @Test
    @DisplayName("sendNotification - 유저 프로필 없을 시 예외")
    void notExistsProfile() {
        // given
        given(userRepository.existsById(sender.getId())).willReturn(true);
        given(userRepository.existsById(receiver.getId())).willReturn(true);
        given(profileRepository.findById(sender.getId())).willReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () ->
                notificationService.sendNotification(sender, receiver, NotificationType.FOLLOW, 100L)
        );
    }
}