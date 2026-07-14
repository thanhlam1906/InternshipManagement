package com.example.internshipmanagement.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Separate security configuration for OAuth2 (Google) login.
 * Only activates when the Google OAuth2 client-id is configured.
 * When disabled, the main SecurityConfig handles everything.
 */
@Slf4j
@Configuration
@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${spring.security.oauth2.client.registration.google.client-id:}')")
@RequiredArgsConstructor
public class OAuth2SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;

    /**
     * Custom resolver that adds prompt=select_account to force Google
     * to show the account picker on every login, preventing auto-login
     * with a previously authorized Google session.
     */
    private OAuth2AuthorizationRequestResolver authorizationRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver defaultResolver =
            new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");

        return request -> {
            OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
            if (authorizationRequest == null) return null;
            return customizeAuthorizationRequest(authorizationRequest);
        };
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest) {
        Map<String, Object> additionalParameters =
            new HashMap<>(authorizationRequest.getAdditionalParameters());
        // Force Google to show the account picker on every login attempt
        additionalParameters.put("prompt", "select_account");
        return OAuth2AuthorizationRequest.from(authorizationRequest)
            .additionalParameters(additionalParameters)
            .build();
    }

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
                .authorizationEndpoint(authz -> authz
                    .authorizationRequestResolver(authorizationRequestResolver()))
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService))
                .successHandler(oAuth2AuthenticationSuccessHandler)
            );

        log.info("Google OAuth2 login is ENABLED (prompt=select_account)");

        return http.build();
    }
}
