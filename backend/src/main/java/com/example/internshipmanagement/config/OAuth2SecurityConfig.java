package com.example.internshipmanagement.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Separate security configuration for OAuth2 (Google) login.
 * Only activates when the Google OAuth2 client-id is configured.
 * When disabled, the main SecurityConfig handles everything.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.security.oauth2.client.registration.google.client-id")
@RequiredArgsConstructor
public class OAuth2SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    /**
     * This filter chain runs BEFORE the main SecurityConfig chain
     * when OAuth2 is enabled, adding Google OAuth2 login support.
     */
    @Bean
    @Order(0)
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/oauth2/**", "/login/oauth2/**")
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService))
                .successHandler(oAuth2AuthenticationSuccessHandler)
            );

        log.info("Google OAuth2 login is ENABLED");

        return http.build();
    }
}
