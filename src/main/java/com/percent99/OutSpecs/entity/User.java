package com.percent99.OutSpecs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 정보 Entity 클래스 <br><br>
 * 
 * aiRateLimit : 앨런 AI 사용횟수 <br>
 * role : 기업회원, 일반회원, 관리자 (상태에 따른 Enum) <br>
 * reactions : 사용자가 반응한 엔티티 에대한 리액션 목록
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false , columnDefinition = "ENUM('ENTUSER','USER','ADMIN')")
    private UserRoleType role;

    @Column(name = "provider_id")
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "created_at" ,updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "ai_rate_limit")
    private Integer aiRateLimit;

    /* === EDK 연관 관계 === */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Reaction> reactions;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    private Profile profile;
}