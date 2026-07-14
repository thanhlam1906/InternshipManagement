package com.example.internshipmanagement.config;

import com.example.internshipmanagement.entity.User;
import com.example.internshipmanagement.repository.IUserRepository;
import com.example.internshipmanagement.ulti.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * After a successful OAuth2 login (Google), generates a JWT for the user
 * and redirects back to the frontend with the token as a query parameter.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.security.oauth2.client.registration.google.client-id")
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final IUserRepository userRepository;

    @Value("${app.oauth2.redirect-uri:http://localhost:5173/oauth2/callback}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Retrieve the local user ID we stashed in CustomOAuth2UserService
        Object localUserIdObj = oAuth2User.getAttribute("localUserId");
        String email = oAuth2User.getAttribute("email");

        if (localUserIdObj == null) {
            log.error("OAuth2 success but no localUserId in attributes — cannot generate JWT");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "User not linked");
            return;
        }

        Integer userId = (Integer) localUserIdObj;
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            log.error("OAuth2 success but user not found in DB: userId={}", userId);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "User not found");
            return;
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("token", token)
                .queryParam("userId", user.getUserId())
                .queryParam("username", user.getUsername())
                .queryParam("fullName", user.getFullName())
                .queryParam("role", user.getRole().name())
                .build()
                .toUriString();

        log.info("OAuth2 success: userId={}, email={}, redirecting to frontend", userId, email);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
