package com.percent99.OutSpecs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * QnA 게시판 정보를 담는 엔티티 <br>
 *
 * <ul> QnA 게시판
 *     <li>answerComplete : 답변완료 시 true, 미완료시 false 기본값-false</li>
 * </ul>
 */

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "posts_qna")
public class PostQnA {

    @Id
    @Column(name = "post_id")
    private Long postId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "answer_complete", nullable = false)
    private boolean answerComplete = false;
}
