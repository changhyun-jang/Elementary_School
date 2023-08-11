package thisisus.school.member.security.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import thisisus.school.member.security.OAuthAttributes;
import thisisus.school.member.domain.Member;
import thisisus.school.member.repository.MemberRepository;
import thisisus.school.member.security.etc.OAuthProcessingException;
import thisisus.school.member.security.util.AuthProvider;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {


        // DefaultOAuth2UserService 객체를 성공정보를 바탕으로 만듦
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        // 생성된 객체로부터 User를 받는다
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 받은 User로부터 정보를 받는다
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = null;

        // 요청된 곳에 따라 다르게 작업
        if (provider.equals("google")) {
            log.info("구글 로그인 요청");
             attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());
        } else if (provider.equals("kakao")) {
            log.info("카카오 로그인 요청");
            attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());
        }

        Member member = null;
        Optional<Member> memberEntity = memberRepository.findByEmail(attributes.getEmail());


        if (memberEntity.isEmpty()) {
            member = attributes.toEntity();
            memberRepository.save(member);
        } else {
            member = memberUpdate(attributes);

        }


//        Member member = saveOrUpdate(attributes);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRoleKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey()
        );
    }

    private Member memberUpdate(OAuthAttributes attributes) {
        Member member = memberRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getName()))
                .get();
        member.setLastLogin(LocalDateTime.now());

        return memberRepository.save(member);
    }


}
