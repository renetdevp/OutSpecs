package com.percent99.OutSpecs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 팀모집 게시판 정보를 담는 entity <br>
 * <ul> 팀 모집 게시판
 *      <li>status : 모집공고 상태</li>
 *      <li>capacity : 모집 인원 수</li>
 * </ul>
 */

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "posts_team_information")
public class PostTeamInformation {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('OPEN', 'CLOSED')")
    private PostStatus status;

    @Column(nullable = false)
    private Integer capacity;
}
