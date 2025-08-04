package com.percent99.OutSpecs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 이미지 정보를 담는  entity <br>
 */

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "images")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;
}
