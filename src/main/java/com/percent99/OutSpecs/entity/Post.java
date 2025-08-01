package com.percent99.OutSpecs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 게시글 정보를 담는 엔티티
 *
 * <p>
 *     <ul> 팀 모집 게시판
 *         <li>status : 모집공고 상태</li>
 *         <li>capacity : 모집 인원 수</li>
 *     </ul>
 *     <ul> 나가서놀기 게시판
 *          <li>placeName : 장소명</li>
 *     </ul>
 *     <ul> 자유, Q&A 게시판
 *         <li>category : 태그 카테고리</li>
 *     </ul>
 *     <ul> 채용공고 게시판
 *         <li>techStack : 요구기술스택</li>
 *         <li>career : 요구 경력</li>
 *     </ul>
 * </p>
 */

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(length = 255)
    private String Images;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    private Integer capacity;

    @Column(name = "place_name")
    private String placeName;

    @Column(length = 255)
    private String category;

    @Column(name = "tech_stack")
    private String techStack;

    private Integer career;
}
