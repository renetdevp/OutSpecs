package com.percent99.OutSpecs.security;

import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.entity.UserRoleType;
import com.percent99.OutSpecs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * OAuth2 로그인 사용자 정보를 관리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /**
     * OAuth2 인증 요청을 처리하고 사용자 정보를 로드한뒤 process 를 통해 CustomUserPrincipal로 반환한다.
     * @param userRequest OAuth2 클라이언트 설정 및 액세스 토큰을 포함한 요청객체
     * @return CustomUserPrincipal로 매핑된 OAuth2User 객체
     * @throws OAuth2AuthenticationException 이메일 누락 등 처리 예외가 발생 시
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
            OAuth2User oAuth2User = super.loadUser(userRequest);

            try {
                return process(userRequest, oAuth2User);
            }catch (OAuth2AuthenticationException ex){
                log.error(ex.getMessage());
                throw ex;
            }
    }

    /**
     * OAuth2 프로바이더별로 내려온 사용자 속성을 기반으로 다음을 수행한다.
     * <ul>
     *   <li>기존 사용자(email 기준)가 providerId를 갖고 있으면 기존 OAuth2 사용자로 간주하고 즉시 로그인 처리</li>
     *   <li>기존 사용자이지만 providerId가 없으면 폼 로그인 계정으로 간주하여 예외 발생</li>
     *   <li>신규 사용자인 경우 안전한 랜덤 비밀번호를 생성하고 User 엔티티를 저장한 뒤 로그인 처리</li>
     * </ul>
     * @param req OAuth2UserRequest 객체
     * @param ou DefaultOAuth2UserService가 로드한 원본 OAuth2User
     * @return CustomUserPrincipal로 매핑된 OAuth2User
     */
    private OAuth2User process(OAuth2UserRequest req, OAuth2User ou){

        String regId = req.getClientRegistration().getRegistrationId();
        if (!"google".equals(regId)) {
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 Provider: " + regId);
        }

        Map<String, Object> attributes = ou.getAttributes();
        String socialId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");

        if (socialId == null || socialId.isBlank()) {
            throw new OAuth2AuthenticationException("구글 계정 ID(sub)를 가져오지 못했습니다.");
        }
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("구글 계정 이메일 권한이 없습니다.");
        }

        Optional<User> userOptional = userRepository.findByProviderId(socialId);
        if(userOptional.isPresent()){
            return new CustomUserPrincipal(userOptional.get(),attributes);
        }

        userRepository.findByUsername(email).ifPresent(u -> {
            if(u.getProviderId() == null || u.getProviderId().isBlank()){
                throw new OAuth2AuthenticationException(
                        "이미 폼 로그인으로 가입된 이메일입니다. 아이디/비밀번호로 로그인해주세요.");
            }
        });

        String randomPassword = generateSecureRandomPassword();

        User user =  new User();
        user.setUsername(generateUniqueUsername(email));
        user.setProviderId(socialId);
        user.setRole(UserRoleType.USER);
        user.setPassword(randomPassword);
        user.setAiRateLimit(5);
        user.setCreatedAt(LocalDateTime.now());

        try{
            userRepository.save(user);
        }catch (DataIntegrityViolationException e) {
            // 동시성 위반으로 재조회후 로그인 처리
            user = userRepository.findByProviderId(socialId).orElseThrow(() -> e);
        }
        log.info("신규 OAuth2 사용자 생성 : {}",email);
        return new CustomUserPrincipal(user,attributes);
    }

    /**
     * UUID + 타임스탬프로 예측 불가능한 랜덤 문자열 생성
     * @return UUID-현재밀리언초
     */
    private String generateSecureRandomPassword(){
        return UUID.randomUUID() + "-" + System.currentTimeMillis();
    }

    /**
     * 이메일 주소 앞부분을 기반으로 중복되지 않는 username을 생성한다. <br>
     * 이미 같은 username이 존재할 경우 숫자를 붙여 충돌을 해소한다. <br><br>
     *
     * ex) <br>
     * test@naver.com / test@company.com 이런경우
     * @param email 사용자 이메일 주소
     * @return 중복없는 username
     */
    private String generateUniqueUsername(String email){
        String base = email.substring(0, email.indexOf("@"));
        String username = base;
        int idx = 1;
        while (userRepository.existsByUsername(username)){
            username = base + idx++;
        }
        return username;
    }
}