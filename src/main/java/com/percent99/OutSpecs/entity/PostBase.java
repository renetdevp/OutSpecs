package com.percent99.OutSpecs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 자유게시판, Q&A게시판 등 기본 게시판 정보를 담는 entity <br>
 *
 * <ul> 자유, Q&A 게시판
 *      <li>tags : 태그 카테고리</li>
 * </ul>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "posts_base")
public class PostBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(length = 255, nullable = false)
    private String tags;
}