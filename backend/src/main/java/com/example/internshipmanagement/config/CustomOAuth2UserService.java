package com.example.internshipmanagement.config;

import com.example.internshipmanagement.entity.User;
import com.example.internshipmanagement.entity.enums.AuthProvider;
import com.example.internshipmanagement.entity.enums.Role;
import com.example.internshipmanagement.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Custom OAuth2 user service for Google login.
 * Finds or creates a local User record for each Google-authenticated principal.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.security.oauth2.client.registration.google.client-id")
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final IUserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");
        String avatarUrl = oAuth2User.getAttribute("picture");

        if (email == null) {
            log.error("Google OAuth2 user has no email — cannot proceed");
            throw new OAuth2AuthenticationException("Email not provided by Google");
        }

        User user = findOrCreateUser(email, name, googleId, avatarUrl);

        // Add the local userId as a custom attribute so the success handler can reference it
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("localUserId", user.getUserId());
        attributes.put("localUsername", user.getUsername());
        attributes.put("localRole", user.getRole().name());

        log.info("OAuth2 login processed: email={}, userId={}, role={}", email, user.getUserId(), user.getRole());

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                "email" // nameAttributeKey — use email as the principal name
        );
    }

    private User findOrCreateUser(String email, String name, String googleId, String avatarUrl) {
        // Try to find by provider + providerId first
        Optional<User> existingByProvider = userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, googleId);
        if (existingByProvider.isPresent()) {
            User user = existingByProvider.get();
            // Update avatar URL if changed
            if (avatarUrl != null && !avatarUrl.equals(user.getAvatarUrl())) {
                user.setAvatarUrl(avatarUrl);
                userRepository.save(user);
            }
            return user;
        }

        // Try to find by email — link existing LOCAL account to Google
        Optional<User> existingByEmail = userRepository.findByEmail(email);
        if (existingByEmail.isPresent()) {
            User user = existingByEmail.get();
            user.setProvider(AuthProvider.GOOGLE);
            user.setProviderId(googleId);
            if (!user.getEmailVerified()) {
                user.setEmailVerified(true); // Google accounts have verified email
            }
            if (avatarUrl != null) {
                user.setAvatarUrl(avatarUrl);
            }
            User saved = userRepository.save(user);
            log.info("Linked existing user to Google: userId={}, email={}", saved.getUserId(), email);
            return saved;
        }

        // Create a brand-new user
        String username = generateUsername(email);
        User newUser = User.builder()
                .username(username)
                .passwordHash(null) // Google users have no password
                .fullName(name != null ? name : email)
                .email(email)
                .role(Role.STUDENT)
                .provider(AuthProvider.GOOGLE)
                .providerId(googleId)
                .emailVerified(true) // Google accounts are pre-verified
                .avatarUrl(avatarUrl)
                .isActive(true)
                .build();

        User saved = userRepository.save(newUser);
        log.info("Created new user from Google OAuth2: userId={}, email={}, username={}", saved.getUserId(), email, username);
        return saved;
    }

    /**
     * Generate a unique username from the email prefix.
     * If the preferred username is taken, appends a numeric suffix.
     */
    private String generateUsername(String email) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9_-]", "");
        if (base.length() < 3) {
            base = "user" + base;
        }

        if (!userRepository.existsByUsername(base)) {
            return base;
        }

        int suffix = 1;
        while (userRepository.existsByUsername(base + suffix)) {
            suffix++;
        }
        return base + suffix;
    }
}
