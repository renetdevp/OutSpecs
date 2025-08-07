package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.entity.Notification;
import com.percent99.OutSpecs.entity.NotificationType;
import com.percent99.OutSpecs.entity.Profile;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.repository.NotificationRepository;
import com.percent99.OutSpecs.repository.ProfileRepository;
import com.percent99.OutSpecs.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 게시글 및 댓글 좋아요, 유저 팔로우, 팀 모집 신청/수락/거절 시 알림을 보내기 위한 service
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private NotificationRepository notificationRepository;
    private UserRepository userRepository;
    private final ProfileRepository profileRepository;

    /**
     * 알림 보내기
     * @param sender 알림 발생시키는 유저
     * @param receiver 알림을 받는 유저
     * @param targetType 알림 type
     * @param targetId 알림이 발생하게된 target(게시물, 댓글, 반응 등)의 Id
     */
    public void sendNotification(User sender, User receiver, NotificationType targetType, Long targetId) {
        if (!userRepository.existsById(sender.getId())) {
            throw new EntityNotFoundException("해당 유저는 존재하지 않습니다.");
        }
        if (!userRepository.existsById(receiver.getId())) {
            throw new EntityNotFoundException("해당 유저는 존재하지 않습니다.");
        }

        String message = getMessage(targetType, sender);
        Notification notification = new Notification();
        notification.setSenderId(sender);
        notification.setReceiverId(receiver);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setMessage(message);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    /**
     * 알림 타입별 메세지 설정
     * @param type 알림 타입
     * @param sender 보내는 사람
     * @return 타입별 메세지
     */
    private String getMessage(NotificationType type, User sender) {
        Profile profile = profileRepository.findById(sender.getId())
                .orElseThrow(()-> new EntityNotFoundException("해당 유저의 프로필이 존재하지 않습니다."));

        return switch (type) {
            case APPLY -> profile.getNickname() + "님이 팀 모집에 신청했습니다.";
            case ACCEPTED -> "팀 모집 신청이 수락되었습니다.";
            case REJECTED -> "팀 모집 신청이 거절되었습니다.";
            case FOLLOW -> profile.getNickname() + "님이 당신을 팔로우했습니다.";
            case LIKE_POST -> profile.getNickname() + "님이 게시글을 좋아했습니다.";
            case LIKE_COMMENT -> profile.getNickname() + "님이 댓글을 좋아했습니다.";
            default -> "새로운 알림이 도착했습니다.";
        };
    }
}
