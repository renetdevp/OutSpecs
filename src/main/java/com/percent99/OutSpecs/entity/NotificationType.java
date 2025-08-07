package com.percent99.OutSpecs.entity;

/**
 * 알림 발송을 위한 type enum
 * <ul>
 *     <li>APPLY : 팀 모집 공고 참여 신청</li>
 *     <li>ACCEPTED : 팀 참여 신청 수락</li>
 *     <li>REJECTED : 팀 참여 신청 거절</li>
 *     <li>FOLLOW : 유저 팔로우</li>
 *     <li>LIKE_POST : 게시글 좋아요</li>
 *     <li>LIKE_COMMENT : 댓글 좋아요</li>
 * </ul>
 */
public enum NotificationType {
    APPLY,
    ACCEPTED,
    REJECTED,
    FOLLOW,
    LIKE_POST,
    LIKE_COMMENT
}
