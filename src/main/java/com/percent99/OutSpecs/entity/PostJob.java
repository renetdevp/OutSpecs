package com.percent99.OutSpecs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 채용공고 게시판 정보를 담는 entity <br>
 *
 * <ul> 채용공고 게시판
 *      <li>techniques : 요구기술스택</li>
 *      <li>career : 요구 경력</li>
 * </ul>
 */

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "posts_job")
public class PostJob {

    @Id
    @Column(name = "post_id")
    private Long postId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @OneToMany(mappedBy = "postJob",cascade = CascadeType.ALL,
            orphanRemoval = true,fetch = FetchType.LAZY )
    private List<Techniques> techniques = new ArrayList<>();

    @Column(nullable = false)
    private Integer career;
}