package com.percent99.OutSpecs.security;

import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 스프링 시큐리티 인증 처리 시 필요한 정보를
 * DB에 조회하여 UserDetails로 반환하는 클래스
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 전달된 username 기준으로 DB에 사용자 정보를 조회합니다.
     * @param username 인증에 사용할 이름
     * @return 인증정보가 담긴 객체
     * @throws UsernameNotFoundException 조회된 사용자가 없을 경우 발생
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("존재하지않는 회원입니다."));

        return new CustomUserPrincipal(user);
    }
}