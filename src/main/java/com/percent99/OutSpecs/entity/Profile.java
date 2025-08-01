package com.percent99.OutSpecs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 오픈프로필 Entity 클래스<br><br>
 *
 * userId : user_id 랑 매핑을 위해 @MapsId 사용 <br>
 * image : Json 형식으로 이미지 받을 예정임으로 jsonb 사용
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

    @Column(name = "image", columnDefinition = "jsonb", nullable = false)
    private String image;

    @Column(name = "nickname", unique = true, nullable = false)
    private String nickname;
}