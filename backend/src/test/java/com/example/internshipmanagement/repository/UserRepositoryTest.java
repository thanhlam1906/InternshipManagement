package com.example.internshipmanagement.repository;

import com.example.internshipmanagement.entity.User;
import com.example.internshipmanagement.entity.enums.AuthProvider;
import com.example.internshipmanagement.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository test voi H2 in-memory (profile test).
 * Dung Replace.NONE de giu nguyen H2 URL (MODE=PostgreSQL) tu application-test.properties.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        // Ghi de dialect PostgreSQL tu application.properties de dung H2
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class UserRepositoryTest {

    /**
     * App chinh co @EnableCaching nhung slice @DataJpaTest khong load
     * CacheAutoConfiguration → can cung cap CacheManager don gian cho test.
     */
    @TestConfiguration
    static class CacheTestConfig {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    private User localUser;
    private User googleUser;

    @BeforeEach
    void setUp() {
        localUser = em.persist(User.builder()
                .username("john")
                .passwordHash("hashed")
                .fullName("John Doe")
                .email("john@example.com")
                .role(Role.STUDENT)
                .provider(AuthProvider.LOCAL)
                .isActive(true)
                .build());

        googleUser = em.persist(User.builder()
                .username("jane")
                .fullName("Jane Smith")
                .email("jane@gmail.com")
                .role(Role.MENTOR)
                .provider(AuthProvider.GOOGLE)
                .providerId("google-uid-12345")
                .isActive(true)
                .build());

        em.flush();
        em.clear();
    }

    // ==================== findByUsername ====================

    @Test
    void findByUsername_ShouldReturnUser_WhenUsernameExists() {
        Optional<User> result = userRepository.findByUsername("john");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");
        assertThat(result.get().getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenUsernameNotFound() {
        Optional<User> result = userRepository.findByUsername("khong-ton-tai");

        assertThat(result).isEmpty();
    }

    // ==================== existsByUsername ====================

    @Test
    void existsByUsername_ShouldReturnTrue_WhenUsernameExists() {
        assertThat(userRepository.existsByUsername("john")).isTrue();
    }

    @Test
    void existsByUsername_ShouldReturnFalse_WhenUsernameNotFound() {
        assertThat(userRepository.existsByUsername("khong-ton-tai")).isFalse();
    }

    // ==================== findByProviderAndProviderId ====================

    @Test
    void findByProviderAndProviderId_ShouldReturnUser_WhenProviderAndIdMatch() {
        Optional<User> result =
                userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google-uid-12345");

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(googleUser.getUserId());
        assertThat(result.get().getUsername()).isEqualTo("jane");
    }

    @Test
    void findByProviderAndProviderId_ShouldReturnEmpty_WhenProviderIdNotFound() {
        Optional<User> result =
                userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "sai-provider-id");

        assertThat(result).isEmpty();
    }

    @Test
    void findByProviderAndProviderId_ShouldReturnEmpty_WhenProviderMismatch() {
        // providerId dung nhung provider la LOCAL thay vi GOOGLE
        Optional<User> result =
                userRepository.findByProviderAndProviderId(AuthProvider.LOCAL, "google-uid-12345");

        assertThat(result).isEmpty();
    }

    // ==================== existsByUsernameOrEmail ====================

    @Test
    void existsByUsernameOrEmail_ShouldReturnTrue_WhenOnlyUsernameMatches() {
        assertThat(userRepository.existsByUsernameOrEmail("john", "khac@example.com")).isTrue();
    }

    @Test
    void existsByUsernameOrEmail_ShouldReturnTrue_WhenOnlyEmailMatches() {
        assertThat(userRepository.existsByUsernameOrEmail("khac", "john@example.com")).isTrue();
    }

    @Test
    void existsByUsernameOrEmail_ShouldReturnFalse_WhenNeitherMatches() {
        assertThat(userRepository.existsByUsernameOrEmail("khac", "khac@example.com")).isFalse();
    }

    // ==================== existsByEmailAndUserIdNot ====================

    @Test
    void existsByEmailAndUserIdNot_ShouldReturnTrue_WhenEmailBelongsToAnotherUser() {
        assertThat(userRepository.existsByEmailAndUserIdNot(
                "john@example.com", googleUser.getUserId())).isTrue();
    }

    @Test
    void existsByEmailAndUserIdNot_ShouldReturnFalse_WhenEmailBelongsToSameUser() {
        assertThat(userRepository.existsByEmailAndUserIdNot(
                "john@example.com", localUser.getUserId())).isFalse();
    }
}
