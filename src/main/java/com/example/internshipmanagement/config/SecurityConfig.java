package com.example.internshipmanagement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final LoggingFilter loggingFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .requestMatchers("/api/auth/me").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                    .requestMatchers("/api/auth/change-password").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/users", "/api/users/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/students").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/students/{id}").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/students/{id}").hasAnyRole("ADMIN", "STUDENT")
                    .requestMatchers(HttpMethod.GET, "/api/students/{id}").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                    .requestMatchers("/api/students", "/api/students/**").hasAnyRole("ADMIN", "MENTOR")
                    .requestMatchers(HttpMethod.GET, "/api/mentors/{id}").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                    .requestMatchers(HttpMethod.GET, "/api/mentors").hasAnyRole("ADMIN", "STUDENT")
                    .requestMatchers(HttpMethod.POST, "/api/mentors").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/mentors/{id}").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/mentors/{id}").hasAnyRole("ADMIN", "MENTOR")
                    .requestMatchers(HttpMethod.GET, "/api/internship-phases", "/api/internship-phases/**").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                    .requestMatchers("/api/internship-phases", "/api/internship-phases/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/evaluation-criteria", "/api/evaluation-criteria/**").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                    .requestMatchers("/api/evaluation-criteria", "/api/evaluation-criteria/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/assessment-rounds", "/api/assessment-rounds/**").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                    .requestMatchers("/api/assessment-rounds","/api/assessment-rounds/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/round_criteria", "/api/round_criteria/**").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                    .requestMatchers("/api/round_criteria", "/api/round_criteria/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/internship_assignments", "/api/internship_assignments/**").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                    .requestMatchers(HttpMethod.DELETE, "/api/internship_assignments/{id}").hasRole("ADMIN")
                    .requestMatchers("/api/internship_assignments", "/api/internship_assignments/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/assessment_results", "/api/assessment_results/**").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                    .requestMatchers(HttpMethod.DELETE, "/api/assessment_results/{id}").hasAnyRole("ADMIN", "MENTOR")
                    .requestMatchers("/api/assessment_results", "/api/assessment_results/**").hasRole("MENTOR")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
            )
            .addFilterBefore(loggingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
