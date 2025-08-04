package com.percent99.OutSpecs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

/**
 * 사용자 오픈프로필 Entity 클래스<br><br>
 *
 * userId : user_id 랑 매핑을 위해 @MapsId 사용 <br>
 * imageUrl : 이미지 URL <br>
 * s3Key : AWS S3 키 저장
 */
@Entity
@Getter
@Setter
@Table(name = "profiles")
@NoArgsConstructor
public class Profile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "stacks", nullable = false)
    private String stacks;

    @Column(name = "experience", nullable = false)
    private String experience;

    @Column(name = "self_info" ,nullable = false )
    private String selfInfo;

    @Column(name = "allow_company_access", nullable = false)
    private Boolean allowCompanyAccess;

    @Column(name = "nickname", unique = true, nullable = false)
    private String nickname;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "s3_key")
    private String s3Key;

    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;
}