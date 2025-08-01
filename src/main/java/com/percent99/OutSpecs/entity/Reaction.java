package com.percent99.OutSpecs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 사용자 반응에 따른 entity 클래스
 *
 * targetType : user , post, comment 구분 <br>
 * targetId : 타입에 따른 pk id <br>
 * reactionType : 좋아요, 북마크, 팔로우, 신고 <br><br>
 *
 * uniqueConstraints : 복합 UNIQUE 제약 추가 <br>
 * inexes : 복합 INDEX 추가 <br>
 *
 * 복합 컬럼에대한 고유 제약 조건을 생성후 자주 조회할 컬럼에 대해 DB 인덱스 생성
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "reactions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_target_reaction",
                columnNames = {"user_id", "target_type", "target_id", "reaction_type"}
        ),
        indexes = @Index(
                name = "idx_target",
                columnList = "target_type,target_id,reaction_type"
        )
)
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, columnDefinition = "ENUM('POST','USER')")
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false,
            columnDefinition = "ENUM('LIKE','BOOKMARK','FOLLOW','REPORT')")
    private ReactionType reactionType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}