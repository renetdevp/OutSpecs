package com.percent99.OutSpecs.config;


import com.percent99.OutSpecs.security.CustomOAuth2UserService;
import com.percent99.OutSpecs.security.CustomUserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 애플리케이션의 보안 설정을 담당하는 Configuration 클래스
 *
 * <p>
 *     이 클래스는 보안 설정을 담당합니다.
 * </p>
 * <ul>
 *  <li>CSRF 보호 비활성화</li>
 *  <li>정적 리소스 및 공개 경로에 대한 접근 허용</li>
 *  <li>폼 로그인 및 OAuth2 로그인 설정</li>
 *  <li>로그인 성공/실패 핸들러 설정</li>
 *  <li>로그아웃 시 쿠키 삭제 및 리다이렉트 설정</li>
 * </ul>
 *
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomUserDetailService customUserDetailService;

    /**
     * password 암호화를 위해 bean으로 등록
     * @return password를 BCryptPasswordEncoder로 암호화
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /**
     * DAO 기반 인증 처리를 위한 AuthenticationProvider 빈을 생성한다.
     * <p>
     *     커스텀 UserDetailsService와 PasswordEncoder를 사용하여 <br>
     *     폼 로그인 시 전달된 아이디·비밀번호를 검증한다.
     * </p>
     *
     * @return userDetailsService 및 passwordEncoder를 설정된 DaoAuthenticationProvider 객체
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(customUserDetailService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    /**
     * HTTP 요청에 대한 보안 설정 구성, 빈을 생성하여 반환한다.
     *
     * @param http HttpSecurity 객체
     * @return 설정이 적용된 SecurityFilterChain 객체
     * @throws Exception 보안 설정중 예외 발생시 발생
     */
    @Bean
    public SecurityFilterChain filterChain (HttpSecurity http) throws Exception{
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/scripts/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/","/users/login", "/users/signup").permitAll()
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin    // H2 콘솔 내 <frame> 렌더링 허용
                        )
                )
                .formLogin(form -> form
                        .loginPage("/users/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureHandler((request, response, exception) -> {
                            String errorMsg = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
                            response.sendRedirect("/users/login?error=" + errorMsg);
                        })
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .deleteCookies("JSESSIONID")
                )
                .oauth2Login(oAuth2 -> oAuth2
                        .loginPage("/users/login")
                        .userInfoEndpoint(userInfo ->{
                            userInfo.userService(customOAuth2UserService);
                        })
                        .defaultSuccessUrl("/", true)
                        .failureHandler((request, response, exception) -> {
                            String errorMsg = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
                            response.sendRedirect("/users/login?error=" + errorMsg);
                        })
                );
        return http.build();
    }
}