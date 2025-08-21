package com.percent99.OutSpecs.config;

import com.percent99.OutSpecs.security.CustomOAuth2UserService;
import com.percent99.OutSpecs.security.CustomUserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

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
@EnableMethodSecurity
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
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/chats/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/scripts/**", "/csv/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/","/users/login", "/users/signup","/list/**").permitAll()
                        .requestMatchers(new RegexRequestMatcher("^/post/\\d+$", null)).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/users/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureHandler((request, response, exception) -> {
                            String msg;
                            if (exception instanceof org.springframework.security.authentication.LockedException) {
                                msg = "정지된 계정입니다.";
                            } else if (exception instanceof org.springframework.security.authentication.DisabledException) {
                                msg = "탈퇴(비활성화)된 계정입니다.";
                            } else {
                                msg = "로그인에 실패했습니다. 잠시 후 다시 시도해주세요.";
                            }
                            request.getSession().setAttribute("loginError", msg);
                            response.sendRedirect("/users/login");
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
                            String msg = "소셜 로그인에 실패했습니다. 잠시 후 다시 시도해주세요.";

                            if (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException oae) {
                                var err = oae.getError();
                                String code = (err != null) ? err.getErrorCode() : null;
                                String desc = (err != null && err.getDescription() != null) ? err.getDescription() : null;

                                if ("ACCOUNT_SUSPENDED".equals(code))      msg = (desc != null) ? desc : "정지된 계정입니다.";
                                else if ("ACCOUNT_DELETED".equals(code))   msg = (desc != null) ? desc : "탈퇴(비활성)된 계정입니다.";
                            }
                            request.getSession().setAttribute("loginError", msg);
                            response.sendRedirect("/users/login");
                        })
                );
        return http.build();
    }
}