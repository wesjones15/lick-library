package org.jones.licklibrary.core.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Configuration
public class OAuth2Config {

    @Bean
    @Qualifier("googleOAuth2Delegate")
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> googleOAuth2Delegate() {
        return new DefaultOAuth2UserService();
    }
}
