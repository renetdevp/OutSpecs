package com.percent99.OutSpecs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 채용공고 게시판 정보를 담는 entity <br>
 *
 * <ul> 채용공고 게시판
 *      <li>techStack : 요구기술스택</li>
 *      <li>career : 요구 경력</li>
 * </ul>
 */

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "posts_job")
public class PostJob {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "tech_stack", nullable = false)
    private String techStack;

    @Column(nullable = false)
    private Integer career;
}
