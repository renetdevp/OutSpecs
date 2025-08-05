package com.percent99.OutSpecs.security;

import com.percent99.OutSpecs.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 *
 */
public class CustomUserPrincipal implements OAuth2User, UserDetails {

    private final User user;
    private final Map<String, Object> attributes;

    /**
     * form 로그인용
     * @param user 폼 로그인한 사용자 정보
     */
    public CustomUserPrincipal(User user) {
        this.user = user;
        this.attributes = Collections.emptyMap();
    }

    /**
     * 소셜 로그인용
     * @param user 로그인한 사용자 정보
     * @param attributes 소셜 로그인 정보 (구글 내 고유 사용자 ID, 이름, 성 등등)
     */
    public CustomUserPrincipal(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    // ----- OAuth2 메서드 -----
    @Override
    public Map<String, Object> getAttributes() { return attributes; }

    @Override
    public String getName() { return attributes.getOrDefault("sub", user.getProviderId()).toString(); }

    // ----- UserDetails 메서드 -----

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}