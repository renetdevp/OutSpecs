package com.percent99.OutSpecs.security;

import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.entity.UserRoleType;
import com.percent99.OutSpecs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
     *
     * @param req
     * @param ou
     * @return
     */
    private OAuth2User process(OAuth2UserRequest req, OAuth2User ou){
        String regId = req.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = ou.getAttributes();

        String email = Objects.toString(attributes.get("email"), "");

        if(email == null || email.isEmpty()){
            throw new OAuth2AuthenticationException("해당 이메일이 존재하지 않습니다.");
        }

        String socialId = regId.equals("google")
                ? (String) attributes.get("sub")
                : (String) attributes.get("id");

        String name = regId.equals("google")
                ? (String) attributes.get("name")
                : (String) attributes.get("login");


        Optional<User> userOptional = userRepository.findByUsername(email);
        User user;
        if(userOptional.isPresent()){
            user = userOptional.get();

            if(user.getProviderId() != null && !user.getProviderId().isBlank()){
                return new CustomUserPrincipal(user,attributes);
            }

            throw new OAuth2AuthenticationException(
                    "이미 폼 로그인으로 가입된 이메일입니다. 아이디/비밀번호로 로그인해주세요.");

        }else{

            String randomPassword = generateSecureRandomPassword();

            user = new User();
            user.setUsername(generateUniqueUsername(email));
            user.setProviderId(socialId);
            user.setRole(UserRoleType.USER);
            user.setPassword(passwordEncoder.encode(randomPassword));

            userRepository.save(user);
            log.info("신규 OAuth2 사용자 생성 : {}",email);
            return new CustomUserPrincipal(user,attributes);
        }
    }

    /**
     * UUID + 타임스탬프로 예측 불가능한 랜덤 문자열 생성
     * @return UUID-현재밀리언초
     */
    private String generateSecureRandomPassword(){
        return UUID.randomUUID() + "-" + System.currentTimeMillis();
    }

    /**
     * 
     * @param email
     * @return
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